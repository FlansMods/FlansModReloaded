package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static java.lang.Math.*;

public class ContinuousCollisionUtility
{
	static final Vec3 uA0 = new Vec3(1, 0, 0);
	static final Vec3 uA1 = new Vec3(0, 1, 0);
	static final Vec3 uA2 = new Vec3(0, 0, 1);


	@Nullable
	public static ContinuousSeparationManifold SeparateContinuous(@Nonnull TransformedBB a,
																  @Nonnull Vec3 motionA,
																  @Nonnull AABB b)
	{
		return SeparateContinuous(a.HalfExtents(),
			new Vector3f((float)b.getXsize()/2f, (float)b.getYsize()/2f, (float)b.getZsize()/2f),
			a.Loc(),
			Transform.fromPos(b.getCenter()),
			motionA,
			Vec3.ZERO);
	}
	@Nullable
	public static ContinuousSeparationManifold SeparateContinuous(@Nonnull TransformedBB a,
																  @Nonnull Vec3 motionA,
																  @Nonnull Vec3 posB,
																  @Nonnull AABB sizeB)
	{
		return SeparateContinuous(a.HalfExtents(),
								  new Vector3f((float)sizeB.getXsize()/2f, (float)sizeB.getYsize()/2f, (float)sizeB.getZsize()/2f),
			 				      a.Loc(),
								  Transform.fromPos(posB.add(sizeB.getCenter())),
								  motionA,
								  Vec3.ZERO);
	}

	@Nullable
	public static ContinuousSeparationManifold SeparateContinuous(@Nonnull Vector3f halfA,
																  @Nonnull Vector3f halfB,
																  @Nonnull Transform locA,
																  @Nonnull Transform locB,
																  @Nonnull Vec3 localMotionA,
																  @Nonnull Vec3 localMotionB)
	{
		Transform AtoB = locA.globalToLocalTransform(locB);
		return SeparateContinuous(halfA, halfB, localMotionA.add(AtoB.localToGlobalVelocity(localMotionB)), AtoB);
	}

	// A is an Axis-Aligned box centered at 0,0,0 with half extents halfA
	// B is an Oriented (by AtoB.Ori) box centered at AtoB.Pos with half extents (halfB in B local space)
	// Return value is null if they are separated
	// Or a separation vector if not
	@Nullable
	public static ContinuousSeparationManifold SeparateContinuous(@Nonnull Vector3f halfA,
																  @Nonnull Vector3f halfB,
																  @Nonnull Vec3 motionA, // motion OF A, relative to A
																  @Nonnull Transform AtoB)
	{
		Vec3 t = AtoB.positionVec3();
		Matrix3f m = AtoB.oriMatrix();

		// A dodgy "absolute rotation" matrix that will give you always positive values,
		// but guarantees to give the max possible value of a specific axis
		Matrix3f maxMatrix = new Matrix3f(
			abs(m.m00), abs(m.m01), abs(m.m02),
			abs(m.m10), abs(m.m11), abs(m.m12),
			abs(m.m20), abs(m.m21), abs(m.m22));

		Vec3 uB0 = new Vec3(m.m00, m.m10, m.m20);
		Vec3 uB1 = new Vec3(m.m01, m.m11, m.m21);
		Vec3 uB2 = new Vec3(m.m02, m.m12, m.m22);

		ContinuousSeparationSelector selector = new ContinuousSeparationSelector(uB1);
		Vector3f halfBMax = halfB.mul(maxMatrix, new Vector3f());

		// Test global X axis
		if(selector.Try(t.x, halfA.x, halfBMax.x, motionA.x, true, ()->uA0))
			return null;
		if(selector.Try(t.y, halfA.y, halfBMax.y, motionA.y, true, ()->uA1))
			return null;
		if(selector.Try(t.z, halfA.z, halfBMax.z, motionA.z, true, ()->uA2))
			return null;

		Vec3 tBRelative = AtoB.globalToLocalPosition(Vec3.ZERO);
		Vec3 motionBRelative = AtoB.globalToLocalDirection(motionA);
		Vector3f halfAMax = halfA.mul(maxMatrix, new Vector3f());

		// Now test local axes of box B
		if(selector.Try(tBRelative.x, halfAMax.x, halfB.x, motionBRelative.x, false, ()->uB0))
			return null;
		if(selector.Try(tBRelative.y, halfAMax.y, halfB.y, motionBRelative.y, false, ()->uB1))
			return null;
		if(selector.Try(tBRelative.z, halfAMax.z, halfB.z, motionBRelative.z, false, ()->uB2))
			return null;

		return selector.Build();
	}

	private static class ContinuousSeparationSelector
	{
		@Nonnull private Vec3 Axis;
		private double Separation;
		private double LatestCollisionEntryTime;
		private double EarliestCollisionExitTime;
		private boolean IsDiscrete;
		@Nonnull private Vec3 CollisionPosition;
		@Nonnull private Vec3 StepSeparationAxis;
		private double StepSeparation;
		@Nonnull private Vec3 NormalSeparationAxis;
		private double NormalSeparation;

		public ContinuousSeparationSelector(@Nonnull Vec3 defaultUpAxis)
		{
			Axis = Vec3.ZERO;
			Separation = Double.MAX_VALUE;
			LatestCollisionEntryTime = ContinuousSeparationManifold.UNDEFINED;
			EarliestCollisionExitTime = Double.MAX_VALUE;
			IsDiscrete = true;
			CollisionPosition = Vec3.ZERO;
			StepSeparationAxis = defaultUpAxis;
			StepSeparation = Double.MAX_VALUE;
			NormalSeparationAxis = Vec3.ZERO;
			NormalSeparation = Double.MAX_VALUE;
		}

		public boolean Try(double dist, double radiusA, double radiusB, double projectedMotion,
						   boolean axisOfObjA, @Nonnull Supplier<Vec3> axisProvider)
		{
			double distance = abs(dist);
			double diff = distance - (radiusA + radiusB);
			double sTL = signum(dist);

			boolean discreteCollision = diff <= 0.0d;
			if(!discreteCollision && signum(projectedMotion) == sTL)
				return true;

			double separation = sTL * abs(diff);
			double entryTime = 0;
			double exitTime = Double.MAX_VALUE;
			if (!discreteCollision)
			{
				IsDiscrete = false;

				if (abs(separation) > abs(projectedMotion))
					return true;

				entryTime = abs(separation) / abs(projectedMotion);
				exitTime = (diff + abs(radiusA) + abs(radiusB)) / abs(projectedMotion);
				LatestCollisionEntryTime = Math.max(entryTime, LatestCollisionEntryTime);
				EarliestCollisionExitTime = Math.min(exitTime, EarliestCollisionExitTime);
			}


			// At this point, we want to register this as a good axis to separate on
			Vec3 newAxis = axisProvider.get();
			Vec3 newAxisNormalized = newAxis.normalize();

			boolean isBestSeperation = distance != 0 && -(diff) <= abs(Separation);
			// boolean isBestSeperation = discreteCollision && checkCount == 5; // Debug specific separations

			if (axisOfObjA && distance != 0 && -(diff) <= abs(NormalSeparation))
			{
				NormalSeparationAxis = newAxisNormalized;
				NormalSeparation = separation;
			}

			double dot = StepSeparationAxis.dot(newAxis);
			if (dot != 0 && discreteCollision)
			{
				Vec3 cross = newAxis.cross(StepSeparationAxis);
				double dotSeparation = signum(dot) * dist - (radiusA + radiusB);
				double stepSeparation = -dotSeparation;
				Vec3 stepSeparationVec = newAxis;

				if (!cross.equals(Vec3.ZERO))
				{
					Vec3 sepVec = newAxisNormalized.scale(dotSeparation);
					Vec3 axisPlane = newAxis.cross(cross);
					Vec3 stepPlane = StepSeparationAxis.cross(cross);
					stepSeparationVec =
						sepVec.subtract(axisPlane.scale(sepVec.dot(stepPlane) / axisPlane.dot(stepPlane)));
					stepSeparation = stepSeparationVec.length();
					if (abs(StepSeparation) > abs(stepSeparation) && stepSeparation != 0)
						StepSeparation = stepSeparation;
				}
				else if (abs(StepSeparation) > stepSeparation)
				{
					StepSeparation = stepSeparation;
				}
			}

			if (isBestSeperation)
			{
				Axis = newAxisNormalized;
				Separation = separation;
				CollisionPosition = newAxisNormalized.scale(signum(dist) * (axisOfObjA ? -radiusB : -radiusB) - signum(separation) * .125f);
			}

			return false;
		}

		@Nonnull
		public ContinuousSeparationManifold Build()
		{
			return new ContinuousSeparationManifold(
				Axis,
				Separation,
				LatestCollisionEntryTime,
				EarliestCollisionExitTime,
				IsDiscrete,
				CollisionPosition,
				StepSeparationAxis,
				StepSeparation,
				NormalSeparationAxis,
				NormalSeparation);
		}
	}
}
