package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.SeparationResult;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.shapes.CollisionManifold;
import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.util.ProjectedRange;
import com.flansmod.physics.common.util.ProjectionUtil;
import com.flansmod.physics.common.util.shapes.Plane;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class CollisionTasks
{
	@Nullable
	private static SeparationResult QuickSeparateSpheres(@Nonnull Vec3 aCenter, double aRadius, @Nonnull Vec3 bCenter, double bRadius)
	{
		Vec3 deltaPos = bCenter.subtract(aCenter);
		double deltaLengthSq = deltaPos.lengthSqr();
		// A quick check to see if they are super far away.
		if (deltaLengthSq > (aRadius + bRadius) * (aRadius + bRadius))
		{
			// In this case, the plane halfway between is great
			return SeparationResult.successful(
					Plane.ofNormalAndPointOnPlane(
							deltaPos.normalize(),
							aCenter.add(deltaPos.scale(0.5d))));
		}
		return null;
	}

	@Nonnull
	public static SeparationResult SeparateSpheres(@Nonnull Vec3 aCenter, double aRadius, @Nonnull Vec3 bCenter, double bRadius)
	{
		Vec3 deltaPos = bCenter.subtract(aCenter);
		double deltaLengthSq = deltaPos.lengthSqr();
		// A quick check to see if they are super far away.
		if (deltaLengthSq > (aRadius + bRadius) * (aRadius + bRadius))
		{
			// In this case, the plane halfway between is great
			return SeparationResult.successful(
					Plane.ofNormalAndPointOnPlane(
							deltaPos.normalize(),
							aCenter.add(deltaPos.scale(0.5d))));
		}

		double dist = aRadius + bRadius - Maths.Sqrt(deltaLengthSq);
		return SeparationResult.failure(
				Plane.ofNormalAndPointOnPlane(
						deltaPos.normalize(),
						aCenter.add(deltaPos.scale(0.5d))),
				dist);
	}

	private static final Vec3 X_AXIS = new Vec3(1d, 0d, 0d);
	private static final Vec3 Y_AXIS = new Vec3(0d, 1d, 0d);
	private static final Vec3 Z_AXIS = new Vec3(0d, 0d, 1d);
	private static final Vec3[] GLOBAL_AXES = new Vec3[] { X_AXIS, Y_AXIS, Z_AXIS };
	private static final Direction[] DIRECTIONS = new Direction[] { Direction.EAST, Direction.NORTH, Direction.UP };
	private static abstract class AxisPermutationIterator implements Iterator<Vec3>
	{}
	private static abstract class AxisPermutationIterator3D extends AxisPermutationIterator
	{
		public static final int NUM_TESTS = 3 + 3 + 3*3;
		private int IterIndex = -1;

		public abstract Vec3 getAxisA(int index);
		public abstract Vec3 getAxisB(int index);

		public boolean hasNext() { return IterIndex < NUM_TESTS - 1; }
		public Vec3 next()
		{
			IterIndex++;
			if(IterIndex < 3)
				return getAxisA(IterIndex);
			else if(IterIndex < 6)
				return getAxisB(IterIndex - 3);
			else
			{
				int index = IterIndex - 6;
				int indexA = index / 3;
				int indexB = index % 3;
				return getAxisA(indexA).cross(getAxisB(indexB));
			}
		}

	}



	@Nonnull
	public static SeparationResult Separate(@Nonnull TransformedBB aCollider, @Nonnull TransformedBB bCollider)
	{
		return Separate(
				new AxisPermutationIterator3D() {
					@Override
					public Vec3 getAxisA(int index) { return aCollider.GetAxis(DIRECTIONS[index]); }
					@Override
					public Vec3 getAxisB(int index) { return bCollider.GetAxis(DIRECTIONS[index]); }
				},
				(normal) -> ProjectionUtil.ProjectOBBMinMax(normal, aCollider),
				(normal) -> ProjectionUtil.ProjectOBBMinMax(normal, bCollider));
	}

	@Nonnull
	public static SeparationResult Separate(@Nonnull TransformedBB aCollider, @Nonnull AABB bCollider)
	{
		return Separate(
				new AxisPermutationIterator3D()
				{
					@Override
					public Vec3 getAxisA(int index) { return aCollider.GetAxis(DIRECTIONS[index]); }
					@Override
					public Vec3 getAxisB(int index) { return GLOBAL_AXES[index]; }
				},
				(normal) -> ProjectionUtil.ProjectOBBMinMax(normal, aCollider),
				(normal) -> ProjectionUtil.ProjectAABBMinMax(normal, bCollider));
	}


	/*
		// AABB.radius???
		Optional<SeparationResult> sphereCheck = QuickSeparateSpheres(aCollider.GetCenter(), aCollider.GetMaxRadiusBound(), bCollider.getCenter(), bCollider.getSize());
		if(sphereCheck.isPresent())
			return sphereCheck.get()
		List<Vec3> testedNormals = new ArrayList<>(6);
		Optional<SeparationResult> testA_x = Test(aCollider, bCollider, X_AXIS, testedNormals);
		if(testA_x.isPresent() && testA_x.get().success())
			return testA_x.get();

		Optional<SeparationResult> testA_y = Test(aCollider, bCollider, Y_AXIS, testedNormals);
		if(testA_y.isPresent() && testA_y.get().success())
			return testA_y.get();

		Optional<SeparationResult> testA_z = Test(aCollider, bCollider, Z_AXIS, testedNormals);
		if(testA_z.isPresent() && testA_z.get().success())
			return testA_z.get();

		Vec3 xAxisB = aCollider.GetAxis(Direction.EAST);
		Optional<SeparationResult> testB_x = Test(aCollider, bCollider, xAxisB, testedNormals);
		if(testB_x.isPresent() && testB_x.get().success())
			return testB_x.get();

		Vec3 yAxisB = aCollider.GetAxis(Direction.UP);
		Optional<SeparationResult> testB_y = Test(aCollider, bCollider, yAxisB, testedNormals);
		if(testB_y.isPresent() && testB_y.get().success())
			return testB_y.get();

		Vec3 zAxisB = aCollider.GetAxis(Direction.NORTH);
		Optional<SeparationResult> testB_z = Test(aCollider, bCollider, zAxisB, testedNormals);
		if(testB_z.isPresent() && testB_z.get().success())
			return testB_z.get();

		SeparationResult.Builder failingTests = new SeparationResult.Builder();
		testA_x.ifPresent(failingTests::addFailure);
		testA_y.ifPresent(failingTests::addFailure);
		testA_z.ifPresent(failingTests::addFailure);
		testB_x.ifPresent(failingTests::addFailure);
		testB_y.ifPresent(failingTests::addFailure);
		testB_z.ifPresent(failingTests::addFailure);

		// In 3D, edge-to-edge testing is also needed
		for(int i = 0; i < 3; i++)
		{
			Vec3 axisA = i == 0 ? X_AXIS : (i == 1 ? Y_AXIS : Z_AXIS);
			for(int j = 0; j < 3; j++)
			{
				Vec3 axisB = j == 0 ? xAxisB : (j == 1 ? yAxisB : zAxisB);
				Vec3 testAxis = axisA.cross(axisB);
				Optional<SeparationResult> testCrossAxis = Test(aCollider, bCollider, testAxis, testedNormals);
				if(testCrossAxis.isPresent())
				{
					if (testCrossAxis.get().success())
						return testCrossAxis.get();
					else
						failingTests.addFailure(testCrossAxis.get());
				}
			}
		}

		return failingTests.returnFailure();

	@Nonnull
	public static SeparationResult Separate(@Nonnull TransformedBB aCollider, @Nonnull TransformedBB bCollider)
	{
		Optional<SeparationResult> sphereCheck = QuickSeparateSpheres(aCollider.GetCenter(), aCollider.GetMaxRadiusBound(), bCollider.GetCenter(), bCollider.GetMaxRadiusBound());
		if(sphereCheck.isPresent())
			return sphereCheck.get();

		SeparationResult.Builder resultCollector = new SeparationResult.Builder();

		// Try some axes in A-local space
		Vec3 deltaPos = bCollider.GetCenter().subtract(aCollider.GetCenter());
		Vec3 deltaInASpace = aCollider.Loc().GlobalToLocalDirection(deltaPos);

		// Test A local X axis
		{
			SeparationManifold testAX = aCollider.GetSide(deltaInASpace.x > 0.0f ? Direction.EAST : Direction.WEST);
			double dist = testAX.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testAX, dist);
			else
				resultCollector.addFailure(testAX, dist);
		}
		// Test A local Z axis
		{
			SeparationManifold testAZ = aCollider.GetSide(deltaInASpace.z > 0.0f ? Direction.SOUTH : Direction.NORTH);
			double dist = testAZ.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testAZ, dist);
			else
				resultCollector.addFailure(testAZ, dist);
		}
		// Test A local Y axis
		{
			SeparationManifold testAY = aCollider.GetSide(deltaInASpace.y > 0.0f ? Direction.UP : Direction.DOWN);
			double dist = testAY.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testAY, dist);
			else
				resultCollector.addFailure(testAY, dist);
		}

		// Try some axes in B-local space
		Vec3 deltaInBSpace = bCollider.Loc().GlobalToLocalDirection(deltaPos.scale(-1d));

		// Test B local X axis
		{
			SeparationManifold testBX = bCollider.GetSide(deltaInBSpace.x > 0.0f ? Direction.EAST : Direction.WEST);
			double dist = testBX.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testBX, dist);
			else
				resultCollector.addFailure(testBX, dist);
		}
		// Test B local Z axis
		{
			SeparationManifold testBZ = bCollider.GetSide(deltaInBSpace.z > 0.0f ? Direction.SOUTH : Direction.NORTH);
			double dist = testBZ.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testBZ, dist);
			else
				resultCollector.addFailure(testBZ, dist);
		}
		// Test B local Y axis
		{
			SeparationManifold testBY = bCollider.GetSide(deltaInBSpace.y > 0.0f ? Direction.UP : Direction.DOWN);
			double dist = testBY.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return SeparationResult.success(testBY, dist);
			else
				resultCollector.addFailure(testBY, dist);
		}

		return resultCollector.returnFailure();
	}


	@Nonnull
	private static Optional<SeparationResult> Test(@Nonnull TransformedBB aCollider,
												   @Nonnull AABB bCollider,
												   @Nonnull Vec3 normal,
												   @Nonnull List<Vec3> testedNormals)
	{
		return Test(() -> ProjectionUtil.ProjectAABBMinMax(normal, bCollider),
					() -> ProjectionUtil.ProjectOBBMinMax(normal, aCollider),
					normal,
					testedNormals);
	}
	@Nonnull
	private static Optional<SeparationResult> Test(@Nonnull TransformedBB aCollider,
												   @Nonnull TransformedBB bCollider,
												   @Nonnull Vec3 normal,
												   @Nonnull List<Vec3> testedNormals)
	{
		return Test(() -> ProjectionUtil.ProjectOBBMinMax(normal, bCollider),
					() -> ProjectionUtil.ProjectOBBMinMax(normal, aCollider),
					normal,
					testedNormals);
	}
 */


	@Nonnull
	private static SeparationResult Separate(@Nonnull AxisPermutationIterator testAxes,
											 @Nonnull Function<Vec3, ProjectedRange> projectFuncA,
											 @Nonnull Function<Vec3, ProjectedRange> projectFuncB)
	{
		List<Vec3> testedNormals = new ArrayList<>(6);
		//SeparationResult.Builder failingTests = new SeparationResult.Builder();
		SeparationResult bestResult = null;

        while (testAxes.hasNext())
		{
            Vec3 axis = testAxes.next();
			bestResult = Test(projectFuncA, projectFuncB, axis, testedNormals, bestResult);
            if(bestResult != null)
            {
                if (bestResult.success())
                    return bestResult;
            }
        }

		if(bestResult == null)
		{
			FlansPhysicsMod.LOGGER.error("Ended separation tests with no results at all, not even a failure");
			return new SeparationResult(false, Plane.of(X_AXIS, 0d), -1d);
		}
		else
			return bestResult;
	}

	@Nullable
	private static SeparationResult Test(@Nonnull Function<Vec3, ProjectedRange> projectFuncA,
												   @Nonnull Function<Vec3, ProjectedRange> projectFuncB,
												   @Nonnull Vec3 normal,
												   @Nonnull List<Vec3> testedNormals,
												   @Nullable SeparationResult previousBestResult)
	{
		if(normal.lengthSqr() <= Maths.Epsilon)
			return previousBestResult;

		for(Vec3 alreadyTested : testedNormals)
		{
			double dot = alreadyTested.dot(normal);
			if(Maths.AbsApprox(dot, 1))
				return previousBestResult;
		}

		testedNormals.add(normal);
		var bProjection = projectFuncA.apply(normal);
		var aProjection = projectFuncB.apply(normal);
		var comparison = ProjectionUtil.GetSeparationDistanceAndOrder(aProjection, bProjection);
		double sepDist = comparison.getFirst();
		if (sepDist >= 0.0f)
		{
			// A success is always better than any other result, return it
			if(comparison.getSecond() == ProjectionUtil.ProjectionComparison.Separate_B_Then_A) // [B_min,B_max]<=[A_min,A_max]
				return SeparationResult.successful(Plane.of(normal,bProjection.max()));
			else // [A_min,A_max] <= [B_min,B_max]
				return SeparationResult.successful(Plane.of(normal.scale(-1d), -bProjection.min()));
		}
		else
		{
			if(comparison.getSecond() == ProjectionUtil.ProjectionComparison.Colliding_B_Then_A)
				return SeparationResult.bestFailResult(
						previousBestResult,
						Plane.of(normal.scale(-1d), -aProjection.min()), sepDist);
			else
				return SeparationResult.bestFailResult(
						previousBestResult,
						Plane.of(normal, aProjection.max()), sepDist);
		}
	}
}
