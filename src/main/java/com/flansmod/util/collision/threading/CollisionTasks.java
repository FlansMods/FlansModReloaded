package com.flansmod.util.collision.threading;

import com.flansmod.util.Maths;
import com.flansmod.util.collision.SeparationManifold;
import com.flansmod.util.collision.SeparationTestResult;
import com.flansmod.util.collision.TransformedBB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class CollisionTasks
{
	@Nonnull
	public static SeparationTestResult TestSeparation(@Nonnull TransformedBB aCollider, @Nonnull TransformedBB bCollider)
	{
		Vec3 deltaPos = bCollider.GetCenter().subtract(aCollider.GetCenter());
		double deltaLengthSq = deltaPos.lengthSqr();
		double aMaxRadius = aCollider.GetMaxRadiusBound();
		double bMaxRadius = bCollider.GetMaxRadiusBound();
		// A quick check to see if they are super far away.
		if (deltaLengthSq > (aMaxRadius + bMaxRadius) * (aMaxRadius + bMaxRadius))
		{
			// In this case, the plane halfway between is great
			return new SeparationTestResult(
				SeparationManifold.OfPointAndNormal(aCollider.GetCenter().add(deltaPos.scale(0.5d)), deltaPos.normalize()),
				Maths.Sqrt(deltaLengthSq) - aMaxRadius - bMaxRadius);
		}

		SeparationManifold shortestIntersection = null;
		double shortestIntersectionDist = -Double.MAX_VALUE;

		// Try some axes in A-local space
		Vec3 deltaInASpace = aCollider.Loc().GlobalToLocalDirection(deltaPos);

		// Test A local X axis
		{
			SeparationManifold testAX = aCollider.GetSide(deltaInASpace.x > 0.0f ? Direction.EAST : Direction.WEST);
			double dist = testAX.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testAX, dist);
			else if(dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testAX;
			}
		}
		// Test A local Z axis
		{
			SeparationManifold testAZ = aCollider.GetSide(deltaInASpace.z > 0.0f ? Direction.SOUTH : Direction.NORTH);
			double dist = testAZ.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testAZ, dist);
			else if(dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testAZ;
			}
		}
		// Test A local Y axis
		{
			SeparationManifold testAY = aCollider.GetSide(deltaInASpace.y > 0.0f ? Direction.UP : Direction.DOWN);
			double dist = testAY.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testAY, dist);
			else if(dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testAY;
			}
		}

		// Try some axes in B-local space
		Vec3 deltaInBSpace = bCollider.Loc().GlobalToLocalDirection(deltaPos.scale(-1d));

		// Test B local X axis
		{
			SeparationManifold testBX = bCollider.GetSide(deltaInBSpace.x > 0.0f ? Direction.EAST : Direction.WEST);
			double dist = testBX.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testBX, dist);
			else if (dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testBX;
			}
		}
		// Test B local Z axis
		{
			SeparationManifold testBZ = bCollider.GetSide(deltaInBSpace.z > 0.0f ? Direction.SOUTH : Direction.NORTH);
			double dist = testBZ.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testBZ, dist);
			else if (dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testBZ;
			}
		}
		// Test B local Y axis
		{
			SeparationManifold testBY = bCollider.GetSide(deltaInBSpace.y > 0.0f ? Direction.UP : Direction.DOWN);
			double dist = testBY.GetSeparationDistance(aCollider, bCollider);
			if (dist >= 0.0f)
				return new SeparationTestResult(testBY, dist);
			else if (dist > shortestIntersectionDist)
			{
				shortestIntersectionDist = dist;
				shortestIntersection = testBY;
			}
		}

		return new SeparationTestResult(shortestIntersection, shortestIntersectionDist);
	}
}
