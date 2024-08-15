package com.flansmod.util.collision;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record SeparationTestResult(@Nonnull ISeparator Separator,
								   double Distance)
{
	public boolean IsColliding() { return Distance < 0.0f; }
	public boolean IsNonColliding() { return Distance >= 0.0f; }
	public double GetCollisionDepth() { return Maths.Min(-Distance, 0d); }

	@Nonnull
	public Vec3 GetPointBetween(@Nonnull Vec3 centerA, @Nonnull Vec3 centerB)
	{
		return Separator.GetIntersectionPoint(centerA, centerB);
	}
	@Nonnull
	public Vec3 GetCollidingPoint(@Nonnull TransformedBB bb)
	{
		Vec3 intersection = Separator.GetIntersectionPoint(bb);
		// DEBUG
		if(!bb.ApproxContains(intersection))
		{
			FlansMod.LOGGER.error("Colliding Point is not in BB");
		}
		return intersection;
	}
}
