package com.flansmod.util.collision;

import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

import static java.lang.Math.*;


public record SeparationManifold(@Nonnull Vec3 Axis, double Distance) implements ISeparator
{
	@Nonnull
	public static SeparationManifold Invalid() { return new SeparationManifold(Vec3.ZERO, Double.MAX_VALUE); }
	@Nonnull
	public static SeparationManifold OfBox(@Nonnull TransformedBB obb, @Nonnull Direction side)
	{
		return obb.GetSide(side);
	}
	@Nonnull
	public static SeparationManifold OfPointAndNormal(@Nonnull Vec3 separatingPoint, @Nonnull Vec3 planarNormal)
	{
		return new SeparationManifold(planarNormal, separatingPoint.dot(planarNormal));
	}

	@Override @Nonnull
	public Vec3 GetNormal() { return Axis; }

	// The chosen definition of a plane is shown most clearly here
	public boolean IsPointBelow(@Nonnull Vec3 point) { return Project(point) <= Distance; }
	public boolean IsPointAbove(@Nonnull Vec3 point) { return Project(point) >= Distance; }
	public double GetPointHeightAbove(@Nonnull Vec3 point) { return (Project(point) - Distance) / Distance; }
	public double Project(@Nonnull Vec3 point) { return Axis.dot(point); }

	// Take a max over any of the box corners, i.e. v = (+/-1, +/-1, +/-1) * halfExtents
	//  Say we pick vMax, then our projection onto the planar normal is as follows:
	//  Axis.x * vMax.x, Axis.y * vMax.y, Axis.z * vMax.z
	// Since we can pick any corner, vMax.x, .y, and .z are all independent, so we can maximise each component
	// Notably, this cannot be negative, but that makes sense. We are centered on the origin
	public double ProjectOriginBoxMax(@Nonnull Vector3f halfExtents) { return abs(Axis.x * halfExtents.x) + abs(Axis.y * halfExtents.y) + abs(Axis.z * halfExtents.z); }

	// Now we give the box a position. so our corners are v = point + (+/-1, +/-1, +/-1) * halfExtents
	//  The same logic applies as last time, but taking the maximum value isn't just abs value
	//  projection = Axis.x * (pos.x + vMax.x), ...
	//  		   = dot(Axis, pos) + dot(Axis, vMax)
	//  max(dot(A,p)+dot(A,v)) = dot(A,p) + sign(dot(A,p))*abs(dot(A,v)) <- not sure if this is worth optimising
	public double ProjectAABoxMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents)
	{
		return max(Axis.x * (point.x + halfExtents.x), Axis.x * (point.x - halfExtents.x))
			 + max(Axis.y * (point.y + halfExtents.y), Axis.y * (point.y - halfExtents.y))
			 + max(Axis.z * (point.z + halfExtents.z), Axis.z * (point.z - halfExtents.z));
	}
	public double ProjectAABoxMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents)
	{
		return min(Axis.x * (point.x + halfExtents.x), Axis.x * (point.x - halfExtents.x))
			 + min(Axis.y * (point.y + halfExtents.y), Axis.y * (point.y - halfExtents.y))
			 + min(Axis.z * (point.z + halfExtents.z), Axis.z * (point.z - halfExtents.z));
	}
	public boolean IsAABoxFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABoxMax(point, halfExtents) <= Distance; }
	public boolean IsAABoxFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABoxMin(point, halfExtents) >= Distance; }
	public double GetAABoxHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return (ProjectAABoxMin(point, halfExtents) - Distance) / Distance; }
	public double GetAABoxHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return (ProjectAABoxMax(point, halfExtents) - Distance) / Distance; }

	// Last step, we give the box an orientation
	//  Now picking vMax is a little different
	//  Our projection will be:
	//   Axis.x * [pos.x + vMax.x * m00 + vMax.y * m01 + vMax.z * m02] + ...
	//   We can rearrange for vMax
	//   dot(pos, Axis) + vMax.x * [Axis.x * m00 + Axis.y * m10 + Axis.z * m20] + ...
	//  Whaddaya know, its the transpose
	@Override
	public double ProjectBoxMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		double projPosX = point.x * Axis.x;
		double orientedHalfX = halfExtents.x * (Axis.x * ori.m00 + Axis.y * ori.m10 + Axis.z * ori.m20); // * vMax.x either -1 or 1
		double maxProjX = max(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMax.x

		double projPosY = point.y * Axis.y;
		double orientedHalfY = halfExtents.y * (Axis.x * ori.m01 + Axis.y * ori.m11 + Axis.z * ori.m21); // * vMax.y either -1 or 1
		double maxProjY = max(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMax.y

		double projPosZ = point.z * Axis.z;
		double orientedHalfZ = halfExtents.z * (Axis.x * ori.m02 + Axis.y * ori.m12 + Axis.z * ori.m22); // * vMax.z either -1 or 1
		double maxProjZ = max(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMax.z

		return maxProjX + maxProjY + maxProjZ;
	}
	@Override
	public double ProjectBoxMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		double projPosX = point.x * Axis.x;
		double orientedHalfX = halfExtents.x * (Axis.x * ori.m00 + Axis.y * ori.m10 + Axis.z * ori.m20); // * vMin.x either -1 or 1
		double minProjX = min(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMin.x

		double projPosY = point.y * Axis.y;
		double orientedHalfY = halfExtents.y * (Axis.x * ori.m01 + Axis.y * ori.m11 + Axis.z * ori.m21); // * vMin.y either -1 or 1
		double minProjY = min(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMin.y

		double projPosZ = point.z * Axis.z;
		double orientedHalfZ = halfExtents.z * (Axis.x * ori.m02 + Axis.y * ori.m12 + Axis.z * ori.m22); // * vMin.z either -1 or 1
		double minProjZ = min(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMin.z

		return minProjX + minProjY + minProjZ;
	}
	@Override
	@Nonnull
	public Pair<Double, Double> ProjectBoxMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		double projPosX = point.x * Axis.x;
		double orientedHalfX = halfExtents.x * (Axis.x * ori.m00 + Axis.y * ori.m10 + Axis.z * ori.m20); // * v.x either -1 or 1
		double maxProjX = max(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMax.x
		double minProjX = min(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMin.x

		double projPosY = point.y * Axis.y;
		double orientedHalfY = halfExtents.y * (Axis.x * ori.m01 + Axis.y * ori.m11 + Axis.z * ori.m21); // * v.y either -1 or 1
		double maxProjY = max(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMax.y
		double minProjY = min(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMin.y

		double projPosZ = point.z * Axis.z;
		double orientedHalfZ = halfExtents.z * (Axis.x * ori.m02 + Axis.y * ori.m12 + Axis.z * ori.m22); // * v.z either -1 or 1
		double maxProjZ = max(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMax.z
		double minProjZ = min(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMin.z

		return Pair.of(minProjX + minProjY + minProjZ, maxProjX + maxProjY + maxProjZ);
	}

	@Nonnull
	public Vec3 GetIntersectionPoint(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		Vector3f maxCorner = new Vector3f(halfExtents);
		Vector3f minCorner = new Vector3f(halfExtents);
		double projPosX = point.x * Axis.x;
		double orientedHalfX = halfExtents.x * (Axis.x * ori.m00 + Axis.y * ori.m10 + Axis.z * ori.m20); // * v.x either -1 or 1
		double xPos = projPosX + orientedHalfX;
		double xNeg = projPosX - orientedHalfX;
		double maxProjX = max(xPos, xNeg); // <- Pick the best vMax.x
		double minProjX = min(xPos, xNeg); // <- Pick the best vMin.x
		// If subtracting half x is better than adding half x, then swap our min/max corner vectors
		if(xPos < xNeg)
			maxCorner.mul(-1f, 1f, 1f);
		else
			minCorner.mul(-1f, 1f, 1f);

		double projPosY = point.y * Axis.y;
		double orientedHalfY = halfExtents.y * (Axis.x * ori.m01 + Axis.y * ori.m11 + Axis.z * ori.m21); // * v.y either -1 or 1
		double yPos = projPosY + orientedHalfY;
		double yNeg = projPosY - orientedHalfY;
		double maxProjY = max(yPos, yNeg); // <- Pick the best vMax.y
		double minProjY = min(yPos, yNeg); // <- Pick the best vMin.y
		// If subtracting half y is better than adding half y, then swap our min/max corner vectors
		if(yPos < yNeg)
			maxCorner.mul(1f, -1f, 1f);
		else
			minCorner.mul(1f, -1f, 1f);

		double projPosZ = point.z * Axis.z;
		double orientedHalfZ = halfExtents.z * (Axis.x * ori.m02 + Axis.y * ori.m12 + Axis.z * ori.m22); // * v.z either -1 or 1
		double zPos = projPosZ + orientedHalfZ;
		double zNeg = projPosZ - orientedHalfZ;
		double maxProjZ = max(zPos, zNeg); // <- Pick the best vMax.z
		double minProjZ = min(zPos, zNeg); // <- Pick the best vMin.z
		// If subtracting half z is better than adding half z, then swap our min/max corner vectors
		if(zPos < zNeg)
			maxCorner.mul(1f, 1f, -1f);
		else
			minCorner.mul(1f, 1f, -1f);

		double min = minProjX + minProjY + minProjZ;
		double max = maxProjX + maxProjY + maxProjZ;

		if(Maths.Abs(min) < Maths.Abs(max))
		{
			minCorner.mul(ori);
			return point.add(minCorner.x, minCorner.y, minCorner.z);
		}

		maxCorner.mul(ori);
		return point.add(maxCorner.x, maxCorner.y, maxCorner.z);
	}

	@Override
	public double GetBoxHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return (ProjectBoxMin(point, halfExtents, ori) - Distance) / Distance;
	}
	@Override
	public double GetBoxHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return (ProjectBoxMax(point, halfExtents, ori) - Distance) / Distance;
	}

	@Override
	public double GetSeparationDistance(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
	{
		Pair<Double, Double> aProjection = ProjectBoxMinMax(a);
		Pair<Double, Double> bProjection = ProjectBoxMinMax(b);
		double aMin = aProjection.getFirst();
		double aMax = aProjection.getSecond();
		double bMin = bProjection.getFirst();
		double bMax = bProjection.getSecond();

		// Non-intersection cases
		if(aMax <= bMin)
			return bMin - aMax;
		else if(bMax <= aMin)
			return aMin - bMax;

		// Intersection cases
		return -Maths.Min(aMax - bMin, bMax - aMin);
	}
	@Override
	public double GetSeparationDistance(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
	{
		Pair<Double, Double> aProjection = ProjectBoxesMinMax(a);
		Pair<Double, Double> bProjection = ProjectBoxesMinMax(b);
		double aMin = aProjection.getFirst();
		double aMax = aProjection.getSecond();
		double bMin = bProjection.getFirst();
		double bMax = bProjection.getSecond();

		// Non-intersection cases
		if(aMax <= bMin)
			return bMin - aMax;
		else if(bMax <= aMin)
			return aMin - bMax;

		// Intersection cases
		return -Maths.Min(aMax - bMin, bMax - aMin);
	}
	@Override
	public double GetSeparationDistanceWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
	{
		Pair<Double, Double> projectedRangeA = ProjectBoxMinMax(a);
		double projectedMotionA = Project(motionA);
		Pair<Double, Double> projectedRangeB = ProjectBoxMinMax(b);
		double projectedMotionB = Project(motionB);

		double aMin = projectedRangeA.getFirst() + projectedMotionA;
		double aMax = projectedRangeA.getSecond() + projectedMotionA;
		double bMin = projectedRangeB.getFirst() + projectedMotionB;
		double bMax = projectedRangeB.getSecond() + projectedMotionB;

		// Non-intersection cases
		if(aMax <= bMin)
			return bMin - aMax;
		else if(bMax <= aMin)
			return aMin - bMax;

		// Intersection cases
		return -Maths.Min(aMax - bMin, bMax - aMin);
	}
	@Override
	public double GetSeparationDistanceWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
	{
		Pair<Double, Double> projectedRangeA = ProjectBoxesMinMax(a);
		double projectedMotionA = Project(motionA);
		Pair<Double, Double> projectedRangeB = ProjectBoxesMinMax(b);
		double projectedMotionB = Project(motionB);

		double aMin = projectedRangeA.getFirst() + projectedMotionA;
		double aMax = projectedRangeA.getSecond() + projectedMotionA;
		double bMin = projectedRangeB.getFirst() + projectedMotionB;
		double bMax = projectedRangeB.getSecond() + projectedMotionB;

		// Non-intersection cases
		if(aMax <= bMin)
			return bMin - aMax;
		else if(bMax <= aMin)
			return aMin - bMax;

		// Intersection cases
		return -Maths.Min(aMax - bMin, bMax - aMin);
	}




	// Override defaults to save a few operations
	@Override
	public boolean IsBoxFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectBoxMax(point, halfExtents, ori) <= Distance; }
	@Override
	public boolean IsBoxFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectBoxMin(point, halfExtents, ori) >= Distance; }
	@Override
	public boolean Separates(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
	{
		if(IsBoxFullyAbove(a))
			return IsBoxFullyBelow(b);
		if(IsBoxFullyBelow(a))
			return IsBoxFullyAbove(b);
		return false;
	}
	@Override
	public boolean Separates(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
	{
		if(AreBoxesFullyAbove(a))
			return AreBoxesFullyBelow(b);
		if(AreBoxesFullyBelow(a))
			return AreBoxesFullyAbove(b);
		return false;
	}
	@Override
	public boolean SeparatesWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
	{
		Pair<Double, Double> projectedRangeA = ProjectBoxMinMax(a);
		double projectedMotionA = Project(motionA);
		Pair<Double, Double> projectedRangeB = ProjectBoxMinMax(b);
		double projectedMotionB = Project(motionB);

		return (projectedRangeA.getFirst() + projectedMotionA > projectedRangeB.getSecond() + projectedMotionB)
			&& (projectedRangeB.getFirst() + projectedMotionB > projectedRangeA.getSecond() + projectedMotionA);
	}
	@Override
	public boolean SeparatesWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
	{
		Pair<Double, Double> projectedRangeA = ProjectBoxesMinMax(a);
		double projectedMotionA = Project(motionA);
		Pair<Double, Double> projectedRangeB = ProjectBoxesMinMax(b);
		double projectedMotionB = Project(motionB);

		return (projectedRangeA.getFirst() + projectedMotionA > projectedRangeB.getSecond() + projectedMotionB)
			&& (projectedRangeB.getFirst() + projectedMotionB > projectedRangeA.getSecond() + projectedMotionA);
	}
}

