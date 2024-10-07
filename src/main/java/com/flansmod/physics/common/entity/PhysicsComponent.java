package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.ColliderHandle;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class PhysicsComponent
{
	public static final PhysicsComponent invalid = new PhysicsComponent();
	public static final double DEFAULT_MASS = 1.0d;
	public static final Vec3 DEFAULT_MOMENT = new Vec3(1d, 1d, 1d);

	public double mass = DEFAULT_MASS;
	public Vec3 momentOfInertia = DEFAULT_MOMENT;

	@Nonnull
	public Transform LocationCurrent;
	@Nonnull
	public Transform LocationPrev;
	@Nonnull
	public ForcesOnPart Forces;
	@Nonnull
	public ColliderHandle PhysicsHandle;

	public PhysicsComponent()
	{
		LocationCurrent = Transform.Identity();
		LocationPrev = Transform.Identity();
		Forces = new ForcesOnPart();
		PhysicsHandle = ColliderHandle.invalid;
	}

	public PhysicsComponent(@Nonnull ColliderHandle physicsHandle)
	{
		LocationCurrent = Transform.Identity();
		LocationPrev = Transform.Identity();
		Forces = new ForcesOnPart();
		PhysicsHandle = physicsHandle;
	}

	public PhysicsComponent(@Nonnull Transform initialTransform, @Nonnull ColliderHandle physicsHandle)
	{
		LocationCurrent = initialTransform;
		LocationPrev = initialTransform;
		Forces = new ForcesOnPart();
		PhysicsHandle = physicsHandle;
	}

	@Nonnull
	public Transform getCurrentTransform() { return LocationCurrent; }
	@Nonnull
	public Transform getPreviousTransform() { return LocationPrev; }
	@Nonnull
	public ITransformPair Pair() { return ITransformPair.of(()->LocationPrev, ()->LocationCurrent); }
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
