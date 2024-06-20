package com.flansmod.util.collision;

import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public interface ISeparator
{
	@Nonnull Vec3 GetNormal();
	double GetBoxHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori);
	double GetBoxHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori);
	@Nonnull Vec3 GetIntersectionPoint(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori);
	@Nonnull Pair<Double, Double> ProjectBoxMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori);
	double GetSeparationDistance(@Nonnull TransformedBB a, @Nonnull TransformedBB b);
	double GetSeparationDistance(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b);
	double GetSeparationDistanceWithMotion(@Nonnull TransformedBB a,
										   @Nonnull Vec3 motionA,
										   @Nonnull TransformedBB b,
										   @Nonnull Vec3 motionB);
	double GetSeparationDistanceWithMotion(@Nonnull TransformedBBCollection a,
										   @Nonnull Vec3 motionA,
										   @Nonnull TransformedBBCollection b,
										   @Nonnull Vec3 motionB);


	// Some defaults to give more accessors
	default boolean IsBoxFullyBelow(@Nonnull TransformedBB bb) { return IsBoxFullyBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix()); }
	default boolean IsBoxFullyAbove(@Nonnull TransformedBB bb) { return IsBoxFullyAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix()); }
	default double GetBoxHeightAbove(@Nonnull TransformedBB bb) { return GetBoxHeightAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix()); }
	default double GetBoxHeightBelow(@Nonnull TransformedBB bb) { return GetBoxHeightBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix()); }
	@Nonnull
	default Vec3 GetIntersectionPoint(@Nonnull TransformedBB bb)
	{
		return GetIntersectionPoint(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix());
	}
	default boolean IsBoxFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return GetBoxHeightBelow(point, halfExtents, ori) <= 0.0d;
	}
	default boolean IsBoxFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return GetBoxHeightAbove(point, halfExtents, ori) >= 0.0d;
	}
	default boolean AreBoxesFullyBelow(@Nonnull TransformedBBCollection set)
	{
		Matrix3f mat = set.Location().OriMatrix();
		for(int i = 0; i < set.GetCount(); i++)
			if(!IsBoxFullyBelow(set.GetCenter(i), set.GetHalfExtents(i), mat))
				return false;
		return true;
	}
	default boolean AreBoxesFullyAbove(@Nonnull TransformedBBCollection set)
	{
		Matrix3f mat = set.Location().OriMatrix();
		for(int i = 0; i < set.GetCount(); i++)
			if(!IsBoxFullyAbove(set.GetCenter(i), set.GetHalfExtents(i), mat))
				return false;
		return true;
	}
	@Nonnull
	default Pair<Double, Double> ProjectBoxMinMax(@Nonnull TransformedBB bb)
	{
		return ProjectBoxMinMax(bb.GetCenter(), bb.HalfExtents(), bb.Loc().OriMatrix());
	}
	@Nonnull
	default Pair<Double, Double> ProjectBoxesMinMax(@Nonnull TransformedBBCollection set)
	{
		Matrix3f matrix = set.Location().OriMatrix();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for(int i = 0; i < set.GetCount(); i++)
		{
			Pair<Double, Double> projection = ProjectBoxMinMax(set.GetCenter(i), set.GetHalfExtents(i), matrix);
			min = Maths.Min(projection.getFirst(), min);
			max = Maths.Min(projection.getSecond(), max);
		}
		return Pair.of(min, max);
	}
	default double ProjectBoxMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return ProjectBoxMinMax(point, halfExtents, ori).getFirst();
	}
	default double ProjectBoxMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
	{
		return ProjectBoxMinMax(point, halfExtents, ori).getSecond();
	}
	default boolean Separates(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
	{
		return SeparatesWithMotion(a, Vec3.ZERO, b, Vec3.ZERO);
	}
	default boolean Separates(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
	{
		return SeparatesWithMotion(a, Vec3.ZERO, b, Vec3.ZERO);
	}
	default boolean SeparatesWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
	{
		return GetSeparationDistanceWithMotion(a, motionA, b, motionB) >= 0.0d;
	}
	default boolean SeparatesWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
	{
		return GetSeparationDistanceWithMotion(a, motionA, b, motionB) >= 0.0d;
	}
}
