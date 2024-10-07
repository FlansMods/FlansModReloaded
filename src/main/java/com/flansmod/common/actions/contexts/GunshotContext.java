package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.abilities.AbilityEffectApplyDamage;
import com.flansmod.common.gunshots.EPlayerHitArea;
import com.flansmod.common.gunshots.Gunshot;
import com.flansmod.common.gunshots.PlayerHitResult;
import com.flansmod.common.gunshots.UnresolvedEntityHitResult;
import com.flansmod.common.projectiles.BulletGuidance;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityEffect;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.bullets.elements.EProjectileResponseType;
import com.flansmod.common.types.bullets.elements.HitscanDefinition;
import com.flansmod.common.types.bullets.elements.ImpactDefinition;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.flansmod.common.types.Constants.*;


public class GunshotContext
{
	public static final GunshotContext INVALID = new GunshotContext(ActionGroupContext.INVALID, BulletDefinition.INVALID, false, 0)
	{
		@Override
		public boolean IsValid() { return false; }
	};

	public final ActionGroupContext ActionGroup;
	public final BulletDefinition Bullet;
	public final boolean IsProjectile;
	public final int DefIndex;


	@Nullable
	public ProjectileDefinition GetProjectileDef()
	{
		if(IsProjectile && DefIndex < Bullet.projectiles.length)
			return Bullet.projectiles[DefIndex];
		return null;
	}
	@Nullable
	public HitscanDefinition GetHitscanDef()
	{
		if(!IsProjectile && DefIndex < Bullet.hitscans.length)
			return Bullet.hitscans[DefIndex];
		return null;
	}
	@Nonnull
	public static GunshotContext of(@Nonnull GunshotContext other)
	{
		return new GunshotContext(other.ActionGroup, other.Bullet, other.IsProjectile, other.DefIndex);
	}
	@Nonnull
	public static GunshotContext empty(@Nonnull ActionGroupContext actionGroupContext)
	{
		return new GunshotContext(actionGroupContext, BulletDefinition.INVALID, false, 0);
	}
	@Nonnull
	public static GunshotContext hitscan(@Nonnull ActionGroupContext actionGroupContext, @Nonnull BulletDefinition bulletFired, int hitscanIndex)
	{
		return new GunshotContext(actionGroupContext, bulletFired, false, hitscanIndex);
	}
	@Nonnull
	public static GunshotContext projectile(@Nonnull ActionGroupContext actionGroupContext, @Nonnull BulletDefinition bulletFired, int projectileIndex)
	{
		return new GunshotContext(actionGroupContext, bulletFired, true, projectileIndex);
	}
	@Nonnull
	public static GunshotContext of(@Nonnull ActionGroupContext actionGroupContext, @Nonnull Gunshot hitscanData)
	{
		return new GunshotContext(actionGroupContext, hitscanData.bulletDef, false, hitscanData.fromShotDefIndex);
	}
	@Nonnull
	public static GunshotContext[] forBullet(@Nonnull ActionGroupContext actionGroupContext, @Nonnull BulletDefinition bulletFired)
	{
		GunshotContext[] gunshots = new GunshotContext[bulletFired.hitscans.length + bulletFired.projectiles.length];
		for(int i = 0; i < bulletFired.hitscans.length; i++)
		{
			gunshots[i] = hitscan(actionGroupContext, bulletFired, i);
		}
		for(int j = 0; j < bulletFired.projectiles.length; j++)
		{
			gunshots[bulletFired.hitscans.length + j] = projectile(actionGroupContext, bulletFired, j);
		}
		return gunshots;
	}

	private GunshotContext(@Nonnull ActionGroupContext actionGroupContext, @Nonnull BulletDefinition bullet, boolean isProjectile, int defIndex)
	{
		ActionGroup = actionGroupContext;
		Bullet = bullet;
		IsProjectile = isProjectile;
		DefIndex = defIndex;
	}

	public boolean IsValid() { return ActionGroup.IsValid() && Bullet.IsValid(); }

	public boolean IsHitscan() { return !IsProjectile; }

	public void ProcessShot(@Nonnull Gunshot shotData)
	{
		for (HitResult hit : shotData.hits)
		{
			ProcessImpact(hit);
		}
	}

	public void ProcessImpact(@Nonnull HitResult hit)
	{
		TriggerContext impactContext = GetImpactContext(hit);

		// Apply damage etc
		ApplyImpactEffects(impactContext);

		// Send a trigger event to the Gun
		switch (hit.getType())
		{
			case BLOCK -> {
				ActionGroup.Gun.GetActionStack().EvaluateTrigger(EAbilityTrigger.ShotBlock, ActionGroup, impactContext);
			}
			case ENTITY -> {
				if (hit instanceof PlayerHitResult playerHit && playerHit.GetHitbox().area == EPlayerHitArea.HEAD)
					ActionGroup.Gun.GetActionStack().EvaluateTrigger(EAbilityTrigger.ShotHeadshot, ActionGroup, impactContext);
				else
					ActionGroup.Gun.GetActionStack().EvaluateTrigger(EAbilityTrigger.ShotEntity, ActionGroup, impactContext);
			}
		}
	}

	@Nonnull
	public TriggerContext GetImpactContext(@Nonnull HitResult hit)
	{
		Entity targetEntity = null;
		Level level = ActionGroup.Gun.GetLevel();

		if(level != null)
		{
			HitResult toProcess = hit;
			if (hit instanceof UnresolvedEntityHitResult unresolved)
			{
				Entity entity = level.getEntity(unresolved.EntityID());
				if (entity != null)
				{
					toProcess = new EntityHitResult(entity);
				}
			}

			// Calculate who we are going to splash
			List<Entity> splashedEntities;
			Vec3 center = toProcess.getLocation();
			float splashRadius = SplashDamageRadius();
			if (splashRadius > 0.0f)
			{
				Vec3 halfExtents = new Vec3(splashRadius, splashRadius, splashRadius);
				splashedEntities = level.getEntities(targetEntity, new AABB(center.subtract(halfExtents), center.add(halfExtents)));
			} else splashedEntities = new ArrayList<>();

			return TriggerContext.hitWithSplash(ActionGroup, toProcess, splashedEntities);
		}
		return TriggerContext.empty();
	}

	public void ApplyImpactEffects(@Nonnull TriggerContext impactContext)
	{
		// Iterate all impact effects.
		// Generally one would expect to see at least one effect applied to the hit entity
		for(ImpactDefinition impact : GetAllImpactEffects())
		{
			if(impactContext.CanTriggerFor(impact.targetType))
			{
				TargetsContext targets = TargetsContext.of(impactContext, impact.targetType);
				for(AbilityEffectDefinition effectDef : impact.impactEffects)
				{
					switch(ActionGroup.Gun.GetShooter().GetSide())
					{
						case Client -> {
							effectDef.GetEffectProcessor().TriggerClient(ActionGroup, impactContext, targets, null);
						}
						case Server -> {
							effectDef.GetEffectProcessor().TriggerServer(ActionGroup, impactContext, targets, null);
						}
					}
				}
			}
		}
	}

	// --------------------------------------------------------------------------
	// STAT CACHE
	// --------------------------------------------------------------------------
	public float ModifyFloat(@Nonnull String stat, float defaultValue) { return ActionGroup.ModifyFloat(stat).apply(defaultValue); }
	@Nonnull
	public String ModifyString(@Nonnull String stat, @Nonnull String defaultValue)  { return ActionGroup.ModifyString(stat, defaultValue); }
	public boolean ModifyBoolean(@Nonnull String stat, boolean defaultValue) { return ActionGroup.ModifyBoolean(stat, defaultValue); }
	@Nonnull
	public <T extends Enum<T>> Enum<T> ModifyEnum(@Nonnull String stat, @Nonnull T defaultValue, @Nonnull Class<T> clazz) { return ActionGroup.ModifyEnum(stat, defaultValue, clazz); }

	// ----------------------------------------------------------------------------------------------------
	// Shared shot settings
	public int BulletCount() 				{ return Maths.Ceil(ModifyFloat(STAT_SHOOT_SHOT_COUNT, BaseBulletCount())); }
	private float BaseBulletCount()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		HitscanDefinition hitscanDef = GetHitscanDef();
		return projectileDef != null ? projectileDef.shotCount : (hitscanDef != null ? hitscanDef.shotCount : 0f);
	}
	public float SplashDamageRadius() 		{ return ModifyFloat(STAT_SHOOT_SPLASH_RADIUS, BaseSplashRadius()); }
	private float BaseSplashRadius()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		HitscanDefinition hitscanDef = GetHitscanDef();
		return projectileDef != null ? projectileDef.splashRadius : (hitscanDef != null ? hitscanDef.splashRadius : 0f);
	}
	// ----------------------------------------------------------------------------------------------------


	// ----------------------------------------------------------------------------------------------------
	// Hitscan Settings
	public float PenetrationPower() 		{ return ModifyFloat(STAT_SHOT_PENETRATION_POWER, BasePenetrationPower()); }
	private float BasePenetrationPower() {
		HitscanDefinition hitscanDef = GetHitscanDef();
		return hitscanDef != null ? hitscanDef.penetrationPower : 0.0f;
	}
	// ----------------------------------------------------------------------------------------------------


	// ----------------------------------------------------------------------------------------------------
	// Projectile Settings
	public float FuseTimeSeconds()			{ return ModifyFloat(STAT_PROJECTILE_FUSE_TIME, BaseFuseTime()); }
	public float LaunchSpeed()				{ return ModifyFloat(STAT_PROJECTILE_LAUNCH_SPEED, BaseLaunchSpeed()); }
	public float Acceleration()				{ return ModifyFloat(STAT_PROJECTILE_ACCELERATION, BaseAcceleration()); }
	public float MaxSpeed()					{ return ModifyFloat(STAT_PROJECTILE_MAX_SPEED, BaseMaxSpeed()); }
	public float GravityFactor()			{ return ModifyFloat(STAT_PROJECTILE_GRAVITY_FACTOR, BaseGravityFactor()); }
	@Nonnull public EProjectileResponseType ResponseToBlock() { return (EProjectileResponseType)ModifyEnum(STAT_PROJECTILE_RESPONSE_TO_BLOCK, BaseResponseToBlock(), EProjectileResponseType.class); }
	@Nonnull public EProjectileResponseType ResponseToEntity() { return (EProjectileResponseType)ModifyEnum(STAT_PROJECTILE_RESPONSE_TO_ENTITY, BaseResponseToEntity(), EProjectileResponseType.class); }
	@Nonnull public EProjectileResponseType ResponseToVehicle() { return (EProjectileResponseType)ModifyEnum(STAT_PROJECTILE_RESPONSE_TO_VEHICLE, BaseResponseToVehicle(), EProjectileResponseType.class); }
	public float LockRange()					{ return ModifyFloat(STAT_PROJECTILE_LOCK_RANGE, BaseLockTime()); }
	public float LockTime()					{ return ModifyFloat(STAT_PROJECTILE_LOCK_TIME, BaseLockTime()); }
	public float LockCone()					{ return ModifyFloat(STAT_PROJECTILE_LOCK_CONE, BaseLockCone()); }
	public float TrackCone()				{ return ModifyFloat(STAT_PROJECTILE_TRACK_CONE, BaseTrackCone()); }
	public float TurnRate()					{ return ModifyFloat(STAT_PROJECTILE_TURN_RATE, BaseTurnRate()); }
	public float DragInWater()				{ return ModifyFloat(STAT_PROJECTILE_DRAG_IN_WATER, BaseDragInWater()); }
	public float DragInAir()				{ return ModifyFloat(STAT_PROJECTILE_DRAG_IN_AIR, BaseDragInAir()); }

	public BulletGuidance.GuidanceType GuidanceType(){ //Bit stinky
		ProjectileDefinition projectileDef = GetProjectileDef();
		if(projectileDef == null) return BulletGuidance.GuidanceType.NONE;

		return switch (projectileDef.guidanceType) {
			case "beam_riding" -> BulletGuidance.GuidanceType.BEAM_RIDING;
			case "lock_on" -> BulletGuidance.GuidanceType.LOCKON_SIMPLE;
			case "lock_on_predictive" -> BulletGuidance.GuidanceType.LOCKON_LEADING;
			default -> BulletGuidance.GuidanceType.NONE;
		};
	}

	private float BaseFuseTime() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.fuseTime : 0.0f;
	}
	private float BaseLaunchSpeed()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.launchSpeed : 0.0f;
	}
	private float BaseAcceleration()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.acceleration : 0.0f;
	}
	private float BaseMaxSpeed()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.maxSpeed : 0.0f;
	}
	private float BaseGravityFactor()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.gravityFactor : 0.0f;
	}
	@Nonnull
	private EProjectileResponseType BaseResponseToBlock() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.responseToBlock : EProjectileResponseType.Detonate;
	}
	@Nonnull
	private EProjectileResponseType BaseResponseToEntity() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.responseToEntity : EProjectileResponseType.Detonate;
	}
	@Nonnull
	private EProjectileResponseType BaseResponseToVehicle() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.responseToVehicle : EProjectileResponseType.Detonate;
	}
	private float BaseLockRange()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.lockRange : 0.0f;
	}
	private float BaseLockTime()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.lockTime : 0.0f;
	}
	private float BaseLockCone()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.lockCone : 0.0f;
	}
	private float BaseTrackCone()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.trackCone : 0.0f;
	}
	private float BaseTurnRate()	{
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.turnRate : 0.0f;
	}
	private float BaseDragInWater() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.dragInWater : 0.0f;
	}
	private float BaseDragInAir() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? projectileDef.dragInAir : 0.0f;
	}
	@Nonnull
	public ResourceLocation WaterParticles() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? new ResourceLocation(projectileDef.waterParticles) : JsonDefinition.InvalidLocation;
	}
	@Nonnull
	public ResourceLocation AirParticles() {
		ProjectileDefinition projectileDef = GetProjectileDef();
		return projectileDef != null ? new ResourceLocation(projectileDef.airParticles) : JsonDefinition.InvalidLocation;
	}
	// ----------------------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------------------
	// Impact Settings
	public float EstimateImpactDamage(@Nonnull EAbilityTarget targetType)
	{
		for(ImpactDefinition impact : GetAllImpactEffects())
		{
			if(impact.targetType == targetType)
			{
				for(AbilityEffectDefinition effectDef : impact.impactEffects)
					if(effectDef.effectType == EAbilityEffect.ApplyDamage)
						if(effectDef.GetEffectProcessor() instanceof AbilityEffectApplyDamage dmgAbility)
							return dmgAbility.DamageAmount(ActionGroup, null);
			}
		}
		return 0.0f;
	}

	@Nonnull
	public List<ImpactDefinition> GetImpactEffects(@Nonnull EAbilityTarget targetType)
	{
		List<ImpactDefinition> matches = new ArrayList<>();
		for(ImpactDefinition impact : GetAllImpactEffects())
		{
			if(impact.targetType == targetType)
				matches.add(impact);
		}
		return matches;
	}

	@Nonnull
	private ImpactDefinition[] GetAllImpactEffects()
	{
		ProjectileDefinition projectile = GetProjectileDef();
		if (projectile != null)
			return projectile.impacts;
		HitscanDefinition hitscanDef = GetHitscanDef();
		if (hitscanDef != null)
			return hitscanDef.impacts;
		return new ImpactDefinition[0];
	}
	// ----------------------------------------------------------------------------------------------------


	// UTIL
	public void Save(CompoundTag tags)
	{
		CompoundTag actionGroupTags = new CompoundTag();
		ActionGroup.Save(actionGroupTags);
		tags.put("action", actionGroupTags);
		tags.putInt("bullet", Bullet.hashCode());
		tags.putBoolean("proj", IsProjectile);
		tags.putInt("index", DefIndex);
	}

	public static GunshotContext Load(CompoundTag tags, boolean client)
	{
		BulletDefinition bullet = FlansMod.BULLETS.ByHash(tags.getInt("bullet"));
		ActionGroupContext actionGroup = ActionGroupContext.Load(tags.getCompound("action"), client);
		boolean isProj = tags.getBoolean("proj");
		int defIndex = tags.getInt("index");

		if(actionGroup.IsValid())
			return new GunshotContext(actionGroup, bullet, isProj, defIndex);

		// If we don't have a action group context, there's no point making a bullet context
		return GunshotContext.INVALID;
	}
}

