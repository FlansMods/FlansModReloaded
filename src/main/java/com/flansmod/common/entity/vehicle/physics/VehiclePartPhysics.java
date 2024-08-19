package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.util.Transform;
import com.flansmod.util.collision.ColliderHandle;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class VehiclePartPhysics
{
	public static final VehiclePartPhysics invalid = new VehiclePartPhysics();

	@Nonnull
	public Transform LocationCurrent;
	@Nonnull
	public Transform LocationPrev;
	@Nonnull
	public ForcesOnPart Forces;
	@Nonnull
	public ColliderHandle PhysicsHandle;

	public VehiclePartPhysics()
	{
		LocationCurrent = Transform.Identity();
		LocationPrev = Transform.Identity();
		Forces = new ForcesOnPart();
		PhysicsHandle = ColliderHandle.invalid;
	}

	public VehiclePartPhysics(@Nonnull ColliderHandle physicsHandle)
	{
		LocationCurrent = Transform.Identity();
		LocationPrev = Transform.Identity();
		Forces = new ForcesOnPart();
		PhysicsHandle = physicsHandle;
	}

	public VehiclePartPhysics(@Nonnull Transform initialTransform, @Nonnull ColliderHandle physicsHandle)
	{
		LocationCurrent = initialTransform;
		LocationPrev = initialTransform;
		Forces = new ForcesOnPart();
		PhysicsHandle = physicsHandle;
	}

	@Nonnull
	public Vec3 GetDeltaFramePos()
	{
		return LocationCurrent.PositionVec3().subtract(LocationPrev.PositionVec3());
	}
	@Nonnull
	public Vec3 GetVelocityMS()
	{
		return GetDeltaFramePos().scale(20f);
	}

}
