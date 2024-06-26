package com.flansmod.util.collision;

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
	public Vec3 GetCollidingPoint(@Nonnull TransformedBB bb)
	{
		return Separator.GetIntersectionPoint(bb);
	}
}
