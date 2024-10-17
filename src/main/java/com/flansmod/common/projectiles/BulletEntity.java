package com.flansmod.common.projectiles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.gunshots.Gunshot;
import com.flansmod.common.actions.contexts.GunshotContext;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.bullets.elements.EProjectileResponseType;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class BulletEntity extends Projectile
{
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_SHOOTER_UUID = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_GUN_ID = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<BulletDefinition> DATA_BULLET_DEF = SynchedEntityData.defineId(BulletEntity.class, FlansEntityDataSerializers.BULLET_DEF);
	private static final EntityDataAccessor<Integer> DATA_ACTION_GROUP_PATH_HASH = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SHOT_INDEX = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_LOCK_ID = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);

	@Nullable
	public Entity LockedOnTo = null;
	public float RemainingPenetratingPower = 0.0f;
	public int FuseRemaining = 0;
	public int lockId = 0;
	public boolean Stuck = false;
	public boolean Detonated = false;
	public final ActionStack Actions;

	public void SetOwnerID(@Nonnull UUID ownerID) { entityData.set(DATA_OWNER_UUID, Optional.of(ownerID)); }
	public void SetShooterID(@Nonnull UUID shooterID) { entityData.set(DATA_SHOOTER_UUID, Optional.of(shooterID)); }
	public void SetGunID(@Nonnull UUID gunID) { entityData.set(DATA_GUN_ID, Optional.of(gunID)); }
	public void SetActionGroupPathHash(int hash) { entityData.set(DATA_ACTION_GROUP_PATH_HASH, hash); }
	public void SetBulletDef(@Nonnull BulletDefinition bulletDef) { entityData.set(DATA_BULLET_DEF, bulletDef); }
	public void SetShotIndex(int index) { entityData.set(DATA_SHOT_INDEX, index); }
	public void SetLockID(int index) { entityData.set(DATA_LOCK_ID, index); }

	@Nonnull
	public UUID GetOwnerID() { return entityData.get(DATA_OWNER_UUID).orElse(ShooterContext.InvalidID); }
	@Nonnull
	public UUID GetShooterID() { return entityData.get(DATA_SHOOTER_UUID).orElse(ShooterContext.InvalidID); }
	@Nonnull
	public UUID GetGunID() { return entityData.get(DATA_GUN_ID).orElse(GunContext.INVALID.GetUUID()); }
	public int GetActionGroupPathHash() { return entityData.get(DATA_ACTION_GROUP_PATH_HASH); }
	@Nonnull
	public BulletDefinition GetBulletDef() { return entityData.get(DATA_BULLET_DEF); }
	public int GetShotIndex() { return entityData.get(DATA_SHOT_INDEX); }
	public int GetLockID() { return entityData.get(DATA_LOCK_ID); }
	@Nullable
	public ProjectileDefinition GetProjectileDef()
	{
		BulletDefinition bulletDef = GetBulletDef();
		int shotIndex = GetShotIndex();
		if(0 <= shotIndex && shotIndex < bulletDef.projectiles.length)
			return bulletDef.projectiles[shotIndex];
		return null;
	}
	@Nonnull
	public GunshotContext GetContext()
	{
		ShooterContext shooter = ShooterContext.of(GetShooterID(), GetOwnerID());
		GunContext gun = shooter.CreateContext(GetGunID());
		ActionGroupContext actionGroup = gun.GetActionGroupContextByHash(GetActionGroupPathHash());
		return GunshotContext.projectile(actionGroup, GetBulletDef(), GetShotIndex());
	}


	// TODO: These entity accessors need to check all entities
	@Nullable
	public Entity Owner() { return level().getPlayerByUUID(GetOwnerID()); }
	@Nullable
	public Entity GetShooter() { return level().getPlayerByUUID(GetOwnerID()); }


	public BulletEntity(EntityType<? extends BulletEntity> entityType, Level level)
	{

		super(entityType, level);
		Actions = new ActionStack(level.isClientSide);
	}

	@Override
	protected void defineSynchedData()
	{
		entityData.define(DATA_OWNER_UUID, Optional.empty());
		entityData.define(DATA_SHOOTER_UUID, Optional.empty());
		entityData.define(DATA_GUN_ID, Optional.empty());
		entityData.define(DATA_BULLET_DEF, BulletDefinition.INVALID);
		entityData.define(DATA_SHOT_INDEX, 0);
		entityData.define(DATA_LOCK_ID, -1);
		entityData.define(DATA_ACTION_GROUP_PATH_HASH, 0);

	}

	public void SetLockOnTarget(Entity e){
		LockedOnTo = e;
	}

	public void InitContext(@Nonnull GunshotContext context)
	{
		SetOwnerID(context.ActionGroup.Gun.GetShooter().OwnerUUID());
		SetShooterID(context.ActionGroup.Gun.GetShooter().EntityUUID());
		SetGunID(context.ActionGroup.Gun.GetUUID());
		SetActionGroupPathHash(context.ActionGroup.GroupPath.hashCode());
		SetBulletDef(context.Bullet);
		SetShotIndex(context.DefIndex);
		Entity e = context.ActionGroup.Gun.GetLockTarget();
		LockedOnTo = e; //This is messy
		if(e != null)
		SetLockID(e.getId());
		float fuseTime = context.FuseTimeSeconds();
		if(fuseTime > 0.0f)
		{
			FuseRemaining = Maths.ceil(fuseTime * 20f);
			ActionGroupInstance group = context.ActionGroup.Gun.GetOrCreateActionGroup(context.ActionGroup);
			if(group.GetProgressTicks() > 0)
			{
				FuseRemaining -= group.GetProgressTicks();
			}
		}
	}

	public void SetVelocity(Vec3 velocity)
	{
		setDeltaMovement(velocity);

		setOldPosAndRot();

		//RefreshLockOnTarget();
	}



	//
	private void RecalculateFacing(Vec3 motion)
	{
		double xz = Maths.sqrt(motion.x * motion.x + motion.z * motion.z);
		float yawDeg = (float)Maths.atan2(motion.x, motion.z) * Maths.RadToDegF;
		float pitchDeg = (float)Maths.atan2(motion.y, xz) * Maths.RadToDegF;
		// Slerp
		float turnRate = GetContext().TurnRate();
		pitchDeg = Maths.lerpDegrees(getXRot(), pitchDeg, Math.min(turnRate*20f,1.0f));
		yawDeg = Maths.lerpDegrees(getYRot(), yawDeg, Math.min(turnRate*20f,1.0f));

		setXRot(pitchDeg);
		setYRot(yawDeg);
	}
	private void RefreshLockOnTarget()
	{
		// TODO:

	}

	@Override
	public void tick()
	{
		super.tick();

		if(Stuck)
			setDeltaMovement(Vec3.ZERO);

		Vec3 motion = getDeltaMovement();
		motion = Accelerate(motion);
		motion = ApplyDrag(motion);
		motion = ApplyGravity(motion);
		setDeltaMovement(motion);
		motion = OnImpact(motion);

		ProjectileDefinition def = GetContext().GetProjectileDef();

		if(GetLockID() != -1){
			LockedOnTo = level().getEntity(GetLockID());
		}

		switch(def.GetGuidanceMode())
		{
			case LOCKON_SIMPLE -> RefreshLockOnTarget();
			case LOCKON_LEADING -> RefreshLockOnTarget();
		}

		if(LockedOnTo != null){
			if(LockedOnTo.isRemoved()){
				LockedOnTo = null;
			}
		}

		switch(def.GetGuidanceMode())
		{
			case BEAM_RIDING -> motion = BulletGuidance.BeamRide(this,motion);
			case BEAM_RIDING_TOP -> motion = BulletGuidance.BeamRideTop(this,motion);
			case BEAM_AND_LOCK -> {
				if(LockedOnTo == null)
				motion = BulletGuidance.BeamRide(this, motion);
				else motion = BulletGuidance.RotateTowardsProportional(this,motion);
			}
			case BEAM_AND_LOCK_TOP -> {
				if(LockedOnTo == null)
					motion = BulletGuidance.BeamRideTop(this, motion);
				else motion = BulletGuidance.TopAttackLocked(this,motion);
			}
			case LOCKON_SIMPLE -> motion = BulletGuidance.RotateTowards(this,motion);
			case LOCKON_LEADING -> motion = BulletGuidance.RotateTowardsProportional(this,motion);
			case LOCKON_TOP -> motion = BulletGuidance.TopAttackLocked(this,motion);
		}

		move(MoverType.SELF, motion);

		setDeltaMovement(motion);
		RecalculateFacing(motion);
		UpdateFuse();
		Vec3 origin = position();
		Vec3 look = motion.normalize();
		if(level().isClientSide() && def.GetGuidanceMode() != BulletGuidance.GuidanceType.NONE) //TODO: Proper particle spawning (action maybe)
			Minecraft.getInstance().level.addParticle(ParticleTypes.POOF, origin.x() + look.x * 0.1f, origin.y() + look.y * 0.1f, origin.z() + look.z * 0.1f, (look.x() * 0.3) , (look.y() * 0.3), (look.z() * 0.3));
	}

	protected Vec3 Accelerate(Vec3 motion){
		if(GetContext().Acceleration() > 0) {
			double currSpeed = motion.length();
			if(currSpeed < GetContext().MaxSpeed()/20d) {
				float acc = GetContext().Acceleration();
				motion = motion.normalize().scale(currSpeed+GetContext().Acceleration());
			}
		}
		return motion;
	}

	protected Vec3 ApplyDrag(Vec3 motion)
	{
		if(isInWater())
		{
			// TODO: Trails
			//ResourceLocation waterParticleLoc = Context.WaterParticles();
			//if(JsonDefinition.IsValidLocation(waterParticleLoc))
			//{
			//
			//}
			return motion.scale(Maths.clamp(1.0f - GetContext().DragInWater(), 0f, 1f));
		}
		else
		{
			// TODO: Trails
			//ResourceLocation airParticleLoc = Context.AirParticles();
			//if(JsonDefinition.IsValidLocation(airParticleLoc))
			//{
			//
			//}
			return motion.scale(Maths.clamp(1.0f - GetContext().DragInAir(), 0f, 1f));
		}
	}

	protected Vec3 ApplyGravity(Vec3 motion)
	{
		if(isNoGravity() || Stuck)
			return motion;
		return new Vec3(
			motion.x,
			motion.y - GetContext().GravityFactor() * 0.02d,
			motion.z);
	}

	protected void UpdateFuse()
	{
		if(!level().isClientSide)
		{
			if (GetContext().FuseTimeSeconds() > 0.0f)
			{
				FuseRemaining--;
				if (FuseRemaining <= 0)
					Detonate(BlockHitResult.miss(position(), Direction.DOWN, blockPosition()));
			}
		}
	}

	protected Vec3 OnImpact(Vec3 motion)
	{
		// Work out which thing we collided with (Minecraft is bad at telling us this...)
		HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::CanHitEntity);
		EProjectileResponseType responseType = switch(hitResult.getType()) {
			case MISS -> EProjectileResponseType.PassThrough;
			case ENTITY -> GetContext().ResponseToEntity();
			case BLOCK -> GetContext().ResponseToBlock();
		};

		switch(responseType)
		{
			case PassThrough -> {
				return motion;
			}
			case Stick -> {
				Stuck = true;
				return Vec3.ZERO;
			}
			case Detonate -> {
				Detonate(hitResult);
				return Vec3.ZERO;
			}
			case Bounce -> {
				BlockHitResult blockHitResult = (BlockHitResult) hitResult;
				return switch (blockHitResult.getDirection()) {
					case UP, DOWN -> new Vec3(motion.x, -motion.y, motion.z);
					case EAST, WEST -> new Vec3(-motion.x, motion.y, motion.z);
					case NORTH, SOUTH -> new Vec3(motion.x, motion.y, -motion.z);
				};
			}
		}
		return motion;
	}

	private boolean CanHitEntity(Entity entity)
	{
		return true;
	}

	public void Detonate(HitResult hit)
	{
		if(!Detonated)
		{
			Gunshot shot = new Gunshot();
			shot.fromShotIndex = 0;
			shot.bulletDef = GetBulletDef();
			shot.origin = position();
			shot.trajectory = getDeltaMovement();
			shot.hits = new HitResult[]{
				hit
			};
			GetContext().ProcessShot(shot);
			Detonated = true;
		}
		if(!level().isClientSide)
			kill();
	}


	public void addAdditionalSaveData(CompoundTag tags)
	{
		tags.putString("bullet", GetBulletDef().Location.toString());
		tags.putInt("fuse", FuseRemaining);
		tags.putBoolean("stuck", Stuck);
		tags.putInt("lockTarget",lockId);
		CompoundTag contextTags = new CompoundTag();
		GetContext().Save(contextTags);
		tags.put("context", contextTags);
	}

	public void readAdditionalSaveData(CompoundTag tags)
	{
		if(tags.contains("bullet"))
		{
			SetBulletDef(FlansMod.BULLETS.Get(new ResourceLocation(tags.getString("bullet"))));
		}
		FuseRemaining = tags.getInt("fuse");
		Stuck = tags.getBoolean("stuck");
		lockId = tags.getInt("lockTarget");
		GunshotContext context = GunshotContext.Load(tags.getCompound("context"), level().isClientSide);
		InitContext(context);
		LockedOnTo = level().getEntity(tags.getInt("lockTarget"));
	}


	@Override
	public void setSecondsOnFire(int i)
	{
		// No-op
	}

	@Override
	public boolean isPushable()
	{
		return false;
	}
}
