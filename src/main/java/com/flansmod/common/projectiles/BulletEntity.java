package com.flansmod.common.projectiles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.gunshots.Gunshot;
import com.flansmod.common.actions.contexts.GunshotContext;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.util.Maths;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BulletEntity extends Projectile
{
	private static final EntityDataAccessor<Integer> DATA_BULLET_DEF = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);


	// We store the context locally because we may exist long after the player is gone
	@Nonnull
	public GunshotContext Context = GunshotContext.INVALID;
	@Nonnull
	public BulletDefinition Def = BulletDefinition.INVALID;
	@Nullable
	public ProjectileDefinition ProjectileDef = null;
	public UUID OwnerUUID;


	@Nullable
	public Entity LockedOnTo = null;
	public float RemainingPenetratingPower = 0.0f;
	public int FuseRemaining = 0;
	public boolean Stuck = false;
	public final ActionStack Actions;

	public Entity Owner()
	{
		return level().getPlayerByUUID(OwnerUUID);
	}

	public BulletEntity(EntityType<? extends BulletEntity> entityType, Level level)
	{
		super(entityType, level);
		Actions = new ActionStack(level.isClientSide);
	}

	public void InitContext(@Nonnull GunshotContext context)
	{
		Context = context;
		Def = context.Bullet;
		ProjectileDef = context.GetProjectileDef();
		entityData.set(DATA_BULLET_DEF, Def.hashCode());
		float fuseTime = Context.FuseTimeSeconds();
		if(fuseTime > 0.0f)
		{
			FuseRemaining = Maths.Ceil(fuseTime * 20f);
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

		RefreshLockOnTarget();
	}



	//
	private void RecalculateFacing(Vec3 motion)
	{
		double xz = Maths.Sqrt(motion.x * motion.x + motion.z * motion.z);
		float yawDeg = (float)Maths.Atan2(motion.x, motion.z) * Maths.RadToDegF;
		float pitchDeg = (float)Maths.Atan2(motion.y, xz) * Maths.RadToDegF;

		// Slerp
		pitchDeg = Maths.LerpDegrees(getXRot(), pitchDeg, Context.TurnRate());
		yawDeg = Maths.LerpDegrees(getYRot(), yawDeg, Context.TurnRate());

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
		motion = ApplyDrag(motion);
		motion = ApplyGravity(motion);
		setDeltaMovement(motion);
		motion = OnImpact(motion);
		move(MoverType.SELF, motion);


		setDeltaMovement(motion);
		RecalculateFacing(motion);
		UpdateFuse();
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
			return motion.scale(Maths.Clamp(1.0f - Context.DragInWater(), 0f, 1f));
		}
		else
		{
			// TODO: Trails
			//ResourceLocation airParticleLoc = Context.AirParticles();
			//if(JsonDefinition.IsValidLocation(airParticleLoc))
			//{
			//
			//}
			return motion.scale(Maths.Clamp(1.0f - Context.DragInAir(), 0f, 1f));
		}
	}

	protected Vec3 ApplyGravity(Vec3 motion)
	{
		if(isNoGravity())
			return motion;
		return new Vec3(
			motion.x,
			motion.y - Context.GravityFactor() * 0.02d,
			motion.z);
	}

	protected void UpdateFuse()
	{
		if(!level().isClientSide)
		{
			if (Context.FuseTimeSeconds() > 0.0f)
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
		switch(hitResult.getType())
		{
			case MISS:
				return motion;
			case ENTITY:
			{
				Detonate(hitResult);
				return Vec3.ZERO;
			}
			case BLOCK:
			{
				if(Context.Sticky())
				{
					Stuck = true;
					return Vec3.ZERO;
				}
				else if(Context.FuseTimeSeconds() > 0.0f)
				{
					// Bounce
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					return switch (blockHitResult.getDirection())
					{
						case UP, DOWN -> new Vec3(motion.x, -motion.y, motion.z);
						case EAST, WEST -> new Vec3(-motion.x, motion.y, motion.z);
						case NORTH, SOUTH -> new Vec3(motion.x, motion.y, -motion.z);
					};
				}
				else
				{
					Detonate(hitResult);
					return Vec3.ZERO;
				}
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
		Gunshot shot = new Gunshot();
		shot.fromShotIndex = 0;
		shot.bulletDef = Def;
		shot.origin = position();
		shot.trajectory = getDeltaMovement();
		shot.hits = new HitResult[] {
			hit
		};
		Context.ProcessShot(shot);
		kill();
	}

	@Override
	protected void defineSynchedData()
	{
		getEntityData().define(DATA_BULLET_DEF, 0);
	}

	public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> data)
	{
		if(DATA_BULLET_DEF.equals(data))
		{
			int hash = getEntityData().get(DATA_BULLET_DEF);
			Def = FlansMod.BULLETS.ByHash(hash);
		}

		super.onSyncedDataUpdated(data);
	}

	public void addAdditionalSaveData(CompoundTag tags)
	{
		tags.putString("bullet", Def.Location.toString());
		tags.putInt("fuse", FuseRemaining);
		tags.putBoolean("stuck", Stuck);
		CompoundTag contextTags = new CompoundTag();
		Context.Save(contextTags);
		tags.put("context", contextTags);
	}

	public void readAdditionalSaveData(CompoundTag tags)
	{
		if(tags.contains("bullet"))
		{
			Def = FlansMod.BULLETS.Get(new ResourceLocation(tags.getString("bullet")));
		}
		FuseRemaining = tags.getInt("fuse");
		Stuck = tags.getBoolean("stuck");
		Context = GunshotContext.Load(tags.getCompound("context"), level().isClientSide);
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
