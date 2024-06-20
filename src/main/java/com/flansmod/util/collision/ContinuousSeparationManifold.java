package com.flansmod.util.collision;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public record ContinuousSeparationManifold(
	@Nonnull Vec3 Axis,
	double Separation,
	double LatestCollisionEntryTime,
	double EarliestCollisionExitTime,
	boolean IsDiscrete,
	@Nullable Vec3 CollisionPosition,
	@Nonnull Vec3 StepSeparationAxis,
	double StepSeparation,
	@Nullable Vec3 NormalSeparationAxis,
	double NormalSeparation)
{
	public static double UNDEFINED = -1;
	public static ContinuousSeparationManifold Invalid() {
		return new ContinuousSeparationManifold(
			Vec3.ZERO,
			Double.MAX_VALUE,
			UNDEFINED,
			Double.MAX_VALUE,
			true,
			null,
			Vec3.ZERO,
			0.0f,
			null,
			0.0f);
	}

	public double GetTimeOfImpact() {
		if(LatestCollisionEntryTime == UNDEFINED)
			return UNDEFINED;
		if(LatestCollisionEntryTime > EarliestCollisionExitTime)
			return UNDEFINED;
		return LatestCollisionEntryTime;
	}

	public boolean IsSurfaceCollision() { return true; }
	@Nullable
	public Vec3 GetCollisionNormal()
	{
		return NormalSeparationAxis == null ? null : SeparationManifold.CreateSeparationVec(NormalSeparation, NormalSeparationAxis);
	}
	@Nullable
	public Vec3 AsSeparationVec(double obbStepHeight)
	{
		if (IsDiscrete)
		{
			if (StepSeparation <= obbStepHeight)
				return SeparationManifold.CreateSeparationVec(StepSeparation, StepSeparationAxis);
			return SeparationManifold.CreateSeparationVec(Separation, Axis);
		}
		double t = GetTimeOfImpact();
		if (t == UNDEFINED)
			return null;
		return Vec3.ZERO;
	}
	@Nullable
	public Vec3 AsSeparationVec() {
		return AsSeparationVec(0);
	}

	public Vec3 GetCollisionPosition() {
		return CollisionPosition;
	}
}
