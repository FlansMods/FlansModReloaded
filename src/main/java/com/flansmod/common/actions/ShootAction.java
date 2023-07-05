package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.particle.GunshotHitBlockParticle;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.*;
import com.flansmod.common.types.elements.ShotDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ShootAction extends Action
{
	private GunshotCollection results;
	@Nullable
	private CachedGunStats GunStatCache = null;

	public ShotDefinition ShotDef() { return actionDef.shootStats[0]; }

	public ShootAction(ActionDefinition def, EActionInput inputType)
	{
		super(def, inputType);
		results = null;
	}

	@Override
	public boolean PropogateToServer(ActionContext context) { return true; }
	@Override
	public boolean ShouldFallBackToReload(ActionContext context)
	{
		if(FindLoadedAmmo(context).isEmpty())
			return true;

		return false;
	}
	@Override
	public boolean CanStart(ActionContext context)
	{
		if(!context.Gun().IsValid())
			return false;
		if(!context.Shooter().IsValid())
			return false;
		if(!context.GunDef().IsValid())
			return false;
		if(context.ActionStack().IsReloading())
			return false;
		if(context.ActionStack().GetShotCooldown() > 0.0f)
			return false;
		if(FindLoadedAmmo(context).isEmpty())
			return false;

		if(!actionDef.canActUnderwater)
		{
			if(context.Shooter().Entity().level.isWaterAt(new BlockPos(context.Shooter().GetShootOrigin().PositionVec3())))
				return false;
		}
		if(!actionDef.canActUnderOtherLiquid)
		{
			if(context.Shooter().Entity().level.isFluidAtPosition(new BlockPos(context.Shooter().GetShootOrigin().PositionVec3()), (fluidState) -> { return !fluidState.isEmpty() && !fluidState.isSourceOfType(Fluids.WATER); }))
				return false;
		}

		return super.CanStart(context);
	}

	public boolean VerifyServer(ActionContext context, GunshotCollection shots)
	{
		results = shots;



		return true;
	}

	public ItemStack FindLoadedAmmo(ActionContext context)
	{
		switch(context.GunDef().AmmoConsumeMode)
		{
			case RoundRobin -> {
				int currentChamber = context.GetCurrentChamber();
				return context.GetBulletStack(currentChamber);
			}
			case FirstNonEmpty -> {
				for(int i = 0; i < context.GunDef().numBullets; i++)
				{
					ItemStack stack = context.GetBulletStack(i);
					if(!stack.isEmpty())
						return stack;
				}
			}
			case LastNonEmpty -> {
				for(int i = context.GunDef().numBullets - 1; i >= 0; i--)
				{
					ItemStack stack = context.GetBulletStack(i);
					if(!stack.isEmpty())
						return stack;
				}
			}
			case Simultaneous -> {

			}
		}
		return ItemStack.EMPTY;
	}

	public int TryConsumeAmmo(ActionContext context, int count)
	{
		switch(context.GunDef().AmmoConsumeMode)
		{
			case RoundRobin -> {
				int currentChamber = context.GetCurrentChamber();
				return TryConsumeAmmo(context, currentChamber, count);
			}
			case FirstNonEmpty -> {
				for(int i = 0; i < context.GunDef().numBullets; i++)
				{
					ItemStack stack = context.GetBulletStack(i);
					if(!stack.isEmpty())
					{
						return TryConsumeAmmo(context, i, count);
					}
				}
			}
			case LastNonEmpty -> {
				for(int i = context.GunDef().numBullets - 1; i >= 0; i--)
				{
					ItemStack stack = context.GetBulletStack(i);
					if(!stack.isEmpty())
					{
						return TryConsumeAmmo(context, i, count);
					}
				}
			}
			case Simultaneous -> {
				int countConsumed = 0;
				for(int i = 0; i < context.GunDef().numBullets; i++)
				{
					ItemStack stack = context.GetBulletStack(i);
					if(!stack.isEmpty())
					{
						int countStillToConsume = Maths.Max(count - countConsumed, 0);
						countConsumed += TryConsumeAmmo(context, i, countStillToConsume);
					}
				}
				return countConsumed;
			}
		}
		return 0;
	}

	public int TryConsumeAmmo(ActionContext context, int slotIndex, int amountToConsume)
	{
		ItemStack bulletStack = context.GetBulletStack(slotIndex);

		if(bulletStack.isDamageableItem())
		{
			int amountRemaining = bulletStack.getMaxDamage() - bulletStack.getDamageValue();
			amountToConsume = Maths.Min(amountRemaining, amountToConsume);
			bulletStack.setDamageValue(bulletStack.getDamageValue() + amountToConsume);
			if(bulletStack.getDamageValue() == bulletStack.getMaxDamage())
				bulletStack.setCount(0);
			context.SetBulletStack(slotIndex, bulletStack);
			return amountToConsume;
		}
		else
		{
			int amountRemaining = bulletStack.getCount();
			amountToConsume = Maths.Min(amountRemaining, amountToConsume);
			bulletStack.setCount(amountRemaining - amountToConsume);
			context.SetBulletStack(slotIndex, bulletStack);
			return amountToConsume;
		}
	}


	public void SetResults(GunshotCollection shots)
	{
		results = shots;
	}

	public boolean ValidateAndSetResults(GunContext context, GunshotCollection shots)
	{
		results = shots;

		// TODO: Verify that these shots could definitely come from this context

		return true;
	}

	public GunshotCollection GetResults()
	{
		return results;
	}

	private static final double RAYCAST_LENGTH = 500.0d;

	public void Calculate(ActionContext context)
	{
		if(results == null)
		{
			results = new GunshotCollection()
				.FromAction(context.InputType())
				.WithOwner(context.Owner())
				.WithShooter(context.Entity())
				.WithGun(context.GunDef());
		}

		CachedGunStats stats = GetGunStats(context);

		// If we are firing something faster than 1200rpm, that is more than 1 per tick
		// We are now handling repeat actions at the Action level, so ShootAction will just get multiple triggers
		int shotsFired = 1; //context.ActionStack().TryShootMultiple(stats.TimeToNextShot());

		// We want to shoot {shotsFired} many, but check against and now consume ammo durability
		shotsFired = TryConsumeAmmo(context, shotsFired);

		for(int j = 0; j < shotsFired; j++)
		{
			// Multiplier from https://github.com/FlansMods/FlansMod/blob/71ba7ed065d906d48f34ca471bbd0172b5192f6b/src/main/java/com/flansmod/common/guns/ShotHandler.java#L93
			float bulletSpread = 0.0025f * stats.Spread;
			for (int i = 0; i < stats.BulletCount; i++)
			{
				Transform randomizedDirection = RandomizeVectorDirection(
					context.Shooter().Entity().level.random,
					context.Shooter().GetShootOrigin(),
					bulletSpread,
					stats.SpreadPattern);

				float penetrationPower = stats.PenetrationPower;

				List<HitResult> hits = new ArrayList<HitResult>(8);
				Raytracer.ForLevel(context.Shooter().Entity().level).CastBullet(
					context.Shooter().Entity(),
					randomizedDirection.PositionVec3(),
					randomizedDirection.ForwardVec3().scale(RAYCAST_LENGTH),
					penetrationPower,
					penetrationPower,
					hits
				);

				HitResult[] hitArray = new HitResult[hits.size()];
				hits.toArray(hitArray);
				results.AddShot(new Gunshot()
					.WithOrigin(randomizedDirection.PositionVec3())
					.WithTrajectory(randomizedDirection.ForwardVec3().scale(RAYCAST_LENGTH))
					.WithHits(hitArray));
			}
		}
	}

	private Transform RandomizeVectorDirection(RandomSource rand, Transform aim, float spread, ESpreadPattern spreadPattern)
	{
		Transform result = aim.copy();
		Vector3d yAxis = aim.Up();
		Vector3d xAxis = aim.Right();
		float xComponent;
		float yComponent;

		switch (spreadPattern)
		{
			case Circle, FilledCircle ->
			{
				float theta = rand.nextFloat() * Maths.TauF;
				float radius = (spreadPattern == ESpreadPattern.Circle ? 1.0f : rand.nextFloat()) * spread;
				xComponent = radius * Maths.SinF(theta);
				yComponent = radius * Maths.CosF(theta);
			}
			case Horizontal ->
			{
				xComponent = spread * (rand.nextFloat() * 2f - 1f);
				yComponent = 0.0f;
			}
			case Vertical ->
			{
				xComponent = 0.0f;
				yComponent = spread * (rand.nextFloat() * 2f - 1f);
			}
			case Triangle ->
			{
				// Random square, then fold the corners
				xComponent = rand.nextFloat() * 2f - 1f;
				yComponent = rand.nextFloat() * 2f - 1f;

				if (xComponent > 0f)
				{
					if (yComponent > 1.0f - xComponent * 2f)
					{
						yComponent = -yComponent;
						xComponent = 1f - xComponent;
					}
				} else
				{
					if (yComponent > xComponent * 2f + 1f)
					{
						yComponent = -yComponent;
						xComponent = -1f - xComponent;
					}
				}
			}
			default -> {
				xComponent = 0.0f;
				yComponent = 0.0f;
			}
		}

		float yaw = Maths.AtanF(xComponent);
		float pitch = Maths.AtanF(yComponent);

		result = result.RotateLocalYaw(yaw * Maths.RadToDegF);
		result = result.RotateLocalPitch(pitch * Maths.RadToDegF);

		return result;
	}

	@Override
	protected void OnTriggerServer(ActionContext context)
	{
		if(!context.Shooter().IsValid())
		{
			SetFinished();
			return;
		}

		Level level = context.Shooter().Entity().level;
		CachedGunStats stats = GetGunStats(context);

		if(results != null)
		{
			for(Gunshot shot : results.shots)
			{
				for(HitResult hit : shot.hits)
				{
					// Apply damage etc
					switch(hit.getType())
					{
						case BLOCK ->
						{
							if(actionDef.shootStats[0].breaksMaterials.length > 0)
							{
								BlockHitResult blockHit = (BlockHitResult) hit;
								BlockState stateHit = level.getBlockState(blockHit.getBlockPos());
								if(actionDef.shootStats[0].BreaksMaterial(stateHit.getMaterial()))
								{
									level.destroyBlock(blockHit.getBlockPos(), true, context.Shooter().Entity());
								}
							}
						}
						case ENTITY ->
						{
							Entity entity = null;
							EPlayerHitArea hitArea = EPlayerHitArea.BODY;
							if(hit instanceof UnresolvedEntityHitResult unresolvedHit)
							{
								entity = level.getEntity(unresolvedHit.EntityID());
								hitArea = unresolvedHit.HitboxArea();
							}
							else if(hit instanceof PlayerHitResult playerHit)
							{
								entity = playerHit.getEntity();
								hitArea = playerHit.GetHitbox().area;
							}
							else if(hit instanceof EntityHitResult entityHit)
							{
								entity = entityHit.getEntity();
							}

							// Damage can be applied to anything living, with special multipliers if it was a player
							float damage = stats.BaseDamage;
							if(entity instanceof Player player)
							{
								damage *= stats.MultiplierVsPlayers;
								damage *= hitArea.DamageMultiplier();

								// TODO: Shield item damage multipliers

								player.hurt(context.Gun().CreateDamageSource(), damage);
								// We override the immortality cooldown when firing bullets, as it is too slow
								player.hurtTime = 0;
								player.hurtDuration = 0;
							}
							else if(entity instanceof LivingEntity living)
							{
								living.hurt(context.Gun().CreateDamageSource(), damage);
								living.hurtTime = 0;
								living.hurtDuration = 0;
							}

							// Fire and similar can be apllied to all entities
							if(entity != null)
							{
								entity.setSecondsOnFire(Maths.Floor(stats.SetFireToTarget * 20.0f));
							}
						}
					}

					// Apply other impact effects to the surrounding area

				}
			}
		}
	}

	@Override
	protected void OnTriggerClient(ActionContext context)
	{
		if(context.Shooter().IsLocalPlayerOwner())
		{
			Calculate(context);
		}

		if(results != null)
		{
			boolean hitEntity = false;
			boolean hitMLG = false;
			for(Gunshot shot : results.shots)
			{
				// Create a bullet trail render
				duration = FlansModClient.SHOT_RENDERER.AddTrail(shot.origin, shot.Endpoint());

				for(HitResult hit : shot.hits)
				{
					if(hit.getType() == HitResult.Type.ENTITY)
					{
						hitEntity = true;
						if(((EntityHitResult)hit).getEntity() instanceof EnderDragon dragon)
						{
							float damage = BaseDamage(context);
							damage = damage / 4.0F + Math.min(damage, 1.0F);
							if(dragon.getHealth() <= damage)
								hitMLG = true;
						}
						else if(((EntityHitResult)hit).getEntity() instanceof EnderDragonPart part)
						{
							float damage = BaseDamage(context);
							if(part != part.parentMob.head)
								damage = damage / 4.0F + Math.min(damage, 1.0F);
							if(part.parentMob.getHealth() <= damage)
								hitMLG = true;
						}
					}
				}
			}

			// If this was my shot, and it hit, hit marker me
			if(hitEntity && context.Shooter().IsLocalPlayerOwner())
			{
				FlansModClient.CLIENT_OVERLAY_HOOKS.ApplyHitMarker(hitMLG ? 100.0f : 10.0f, hitMLG);
			}
		}
	}

	@Override
	public void OnTickClient(ActionContext context)
	{
		int tickBefore = GetProgressTicks();
		super.OnTickClient(context);
		int tickAfter = GetProgressTicks();

		boolean playedASoundThisTick = false;

		ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
		ActionDefinition shootActionDef = context.GetShootActionDefinition();

		for(Gunshot shot : results.shots)
		{
			for (HitResult hit : shot.hits)
			{
				// Check if this hit should be processed on this frame
				double t = Maths.CalculateParameter(shot.origin, shot.Endpoint(), hit.getLocation()) * GetDurationTicks();
				if(tickBefore <= t && t < tickAfter)
				{
					// Create hit particles
					switch (hit.getType())
					{
						case BLOCK ->
						{
							ClientLevel level = Minecraft.getInstance().level;
							BlockHitResult blockHit = (BlockHitResult)hit;
							if(shootActionDef != null && shootActionDef.shootStats[0].impact.decal != null && shootActionDef.shootStats[0].impact.decal.length() > 0)
							{
								FlansModClient.DECAL_RENDERER.AddDecal(
									ResourceLocation.tryParse(shootActionDef.shootStats[0].impact.decal).withPrefix("textures/"),
									blockHit.getLocation(),
									blockHit.getDirection(),
									level.random.nextFloat() * 360.0f,
									1000);
							}

							Vec3[] motions = new Vec3[3];
							motions[0] = Maths.Reflect(shot.trajectory.normalize(), blockHit.getDirection());
							Vec3i normal = blockHit.getDirection().getNormal();
							for(int i = 1; i < motions.length; i++)
							{
								motions[i] = new Vec3(
									normal.getX() + level.random.nextGaussian() * 0.2d,
									normal.getY() + level.random.nextGaussian() * 0.2d,
									normal.getZ() + level.random.nextGaussian() * 0.2d);
								motions[i] = motions[i].normalize().scale(0.3d);
							}

							for(int i = 0; i < motions.length; i++)
							{
								BlockState state = level.getBlockState(blockHit.getBlockPos());
								particleEngine.add(new GunshotHitBlockParticle(
									level,
									hit.getLocation().x,
									hit.getLocation().y,
									hit.getLocation().z,
									motions[i].x,
									motions[i].y,
									motions[i].z,
									state,
									blockHit.getBlockPos())
									.updateSprite(state, blockHit.getBlockPos())
									.scale(0.5f));
							}
						}
						case ENTITY ->
						{
							Vec3 shotMotion = shot.trajectory.normalize().scale(GetDurationTicks());
							particleEngine.createParticle(
								ParticleTypes.DAMAGE_INDICATOR,
								hit.getLocation().x,
								hit.getLocation().y,
								hit.getLocation().z,
								shotMotion.x,
								shotMotion.y,
								shotMotion.z);
						}
					}

					// Play a sound, only once per tick to avoid audio overload
					if(!playedASoundThisTick && actionDef.shootStats[0].impact.hitSounds != null)
					{
						playedASoundThisTick = true;
						//Minecraft.getInstance().getSoundManager().play(actionDef.ShootStats.Impact.HitSound);
					}
				}
			}
		}
	}

	public Vec3 GetPlayerMuzzlePosition(ActionContext context, int nTicksAgo)
	{
		if(context.Shooter().Entity() instanceof Player player)
		{
			PlayerSnapshot snapshot = Raytracer.ForLevel(player.level).GetSnapshot(player, nTicksAgo);
			snapshot.GetMuzzlePosition();
		}
		else if(context.Shooter().Entity() instanceof LivingEntity living)
		{
			return living.getEyePosition();
		}
		return context.Shooter().Entity().getEyePosition();

		/*
		ItemStack itemstack = hand == EnumHand.OFF_HAND ? player.getHeldItemOffhand() : player.getHeldItemMainhand();

		if(itemstack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)itemstack.getItem()).GetType();
			AttachmentType barrelType = gunType.getBarrel(itemstack);

			return Vector3f.add(new Vector3f(player.posX, player.posY, player.posZ), snapshot.GetMuzzleLocation(gunType, barrelType, hand), null);
		}
		 */
	}

	@Nonnull
	private CachedGunStats GetGunStats(ActionContext context)
	{
		if(GunStatCache == null)
			GunStatCache = context.BuildGunStatCache(actionDef);
		return GunStatCache;
	}
	public float VerticalRecoil(ActionContext context) { return GetGunStats(context).VerticalRecoil; }
	public float HorizontalRecoil(ActionContext context) { return GetGunStats(context).HorizontalRecoil; }
	public float Spread(ActionContext context) { return GetGunStats(context).Spread; }
	public float Speed(ActionContext context) { return GetGunStats(context).Speed; }
	public int Count(ActionContext context) { return GetGunStats(context).BulletCount; }
	public float PenetrationPower(ActionContext context) { return GetGunStats(context).PenetrationPower; }

	public float BaseDamage(ActionContext context) { return GetGunStats(context).BaseDamage; }
	public float Knockback(ActionContext context) { return GetGunStats(context).Knockback; }
	public float MultiplierVsPlayers(ActionContext context) { return GetGunStats(context).MultiplierVsPlayers; }
	public float MultiplierVsVehicles(ActionContext context) { return GetGunStats(context).MultiplierVsVehicles; }
	public float SplashDamageRadius(ActionContext context) { return GetGunStats(context).SplashDamageRadius; }
	public float SplashDamageFalloff(ActionContext context) { return GetGunStats(context).SplashDamageFalloff; }
	public float SetFireToTarget(ActionContext context) { return GetGunStats(context).SetFireToTarget; }
	public float FireSpreadRadius(ActionContext context) { return GetGunStats(context).FireSpreadRadius; }
	public float FireSpreadAmount(ActionContext context) { return GetGunStats(context).FireSpreadAmount; }
	public ESpreadPattern SpreadPattern(ActionContext context) { return GetGunStats(context).SpreadPattern; }
}
