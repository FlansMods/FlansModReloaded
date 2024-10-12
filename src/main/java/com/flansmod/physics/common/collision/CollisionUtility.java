package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.shapes.CollisionManifold;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.function.Supplier;

import static java.lang.Math.*;

public class CollisionUtility
{
	static final Vec3 uA0 = new Vec3(1, 0, 0);
	static final Vec3 uA1 = new Vec3(0, 1, 0);
	static final Vec3 uA2 = new Vec3(0, 0, 1);

	@Nullable
	public static Vec3 Separate(@Nonnull TransformedBB a,
								@Nonnull TransformedBB b)
	{
		return Separate(a.HalfExtents(),
						b.HalfExtents(),
						a.Loc(),
						b.Loc());
	}
	@Nullable
	public static Vec3 Separate(@Nonnull AABB a,
								@Nonnull TransformedBB b)
	{
		return Separate(new Vector3f((float)a.getXsize()/2f, (float)a.getYsize()/2f, (float)a.getZsize()/2f),
						b.HalfExtents(),
						Transform.fromPos(a.getCenter()),
						b.Loc());
	}
	@Nullable
	public static Vec3 Separate(@Nonnull TransformedBB a,
								@Nonnull AABB b)
	{
		return Separate(a.HalfExtents(),
						new Vector3f((float)b.getXsize()/2f, (float)b.getYsize()/2f, (float)b.getZsize()/2f),
						a.Loc(),
						Transform.fromPos(b.getCenter()));
	}

	@Nullable
	public static Vec3 Separate(@Nonnull Vector3f halfA,
								@Nonnull Vector3f halfB,
								@Nonnull Transform locA,
								@Nonnull Transform locB)
	{
		return Separate(halfA, halfB, locA.globalToLocalTransform(locB));
	}

	// A is an Axis-Aligned box centered at 0,0,0 with half extents halfA
	// B is an Oriented (by AtoB.Ori) box centered at AtoB.Pos with half extents (halfB in B local space)
	// Return value is null if they are separated
	// Or a separation vector if not
	@Nullable
	public static Vec3 Separate(@Nonnull Vector3f halfA,
								@Nonnull Vector3f halfB,
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

		SeparationSelector selector = new SeparationSelector();
		Vector3f halfBMax = halfB.mul(maxMatrix, new Vector3f());
		// Try global (A-box) X
		if(selector.Try(t.x, halfA.x, halfBMax.x, () -> uA0))
			return null;
		// Try global (A-box) Y
		if(selector.Try(t.y, halfA.y, halfBMax.y, () -> uA1))
			return null;
		// Try global (A-box) Z
		if(selector.Try(t.z, halfA.z, halfBMax.z, () -> uA2))
			return null;

		m.transpose();
		Vector3f tInverse = t.toVector3f().mul(m);
		m.transpose();
		Vector3f halfAMax = halfA.mul(maxMatrix, new Vector3f());

		// Try local (B-box) X
		if(selector.Try(tInverse.x, halfAMax.x, halfB.x, () -> new Vec3(m.m00, m.m10, m.m20))) // uB0
			return null;
		// Try local (B-box) Y
		if(selector.Try(tInverse.y, halfAMax.y, halfB.y, () -> new Vec3(m.m01, m.m11, m.m21))) // uB1
			return null;
		// Try local (B-box) Z
		if(selector.Try(tInverse.z, halfAMax.z, halfB.z, () -> new Vec3(m.m02, m.m12, m.m22))) // uB2
			return null;

		return selector.manifold.GetNormal();
	}

	private static double GetSeparationDistance(double dist, double radiusA, double radiusB)
	{
		return abs(dist) - (radiusA + radiusB);
	}

	private static class SeparationSelector
	{
		//int checkCount = 0;
		CollisionManifold manifold = CollisionManifold.invalid();

		public boolean Try(double dist, double radiusA, double radiusB, Supplier<Vec3> axisProvider)
		{
			/*
			//checkCount++;
			double diff = GetSeparationDistance(dist, radiusA, radiusB);
			if(diff > 0.0d)
				return true;

			// This was in Create source, I think we want it off
			//boolean isBestSeparation = checkCount == 2; // Debug specific separations
			//if (isBestSeparation) {
				double sTL = signum(dist);
				double value = sTL * abs(diff);
				manifold = new CollisionManifold(axisProvider.get().normalize(), value);
			//}
			*/
			return false;
		}
	}
}
