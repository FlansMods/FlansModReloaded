package com.flansmod.util.physics;

import com.flansmod.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record LinearVelocity(@Nonnull Vec3 Velocity)
{
	public static final LinearVelocity Zero = new LinearVelocity(Vec3.ZERO);

	@Nonnull
	public static LinearVelocity blocksPerSecond(@Nonnull Vec3 blocksPerSec)
	{
		return new LinearVelocity(Units.Speed.BlocksPerSecond_To_BlocksPerTick(blocksPerSec));
	}
	@Nonnull
	public static LinearVelocity blocksPerTick(@Nonnull Vec3 blocksPerTick)
	{
		return new LinearVelocity(blocksPerTick);
	}

	@Nonnull
	public LinearVelocity add(@Nonnull LinearVelocity other) { return new LinearVelocity(Velocity.add(other.Velocity)); }

	@Nonnull
	public Units.Speed GetDefaultUnits() { return Units.Speed.BlocksPerTick; }
	@Nonnull
	public Vec3 ConvertToUnits(@Nonnull Units.Speed toUnits) { return Units.Speed.Convert(Velocity, Units.Speed.BlocksPerTick, toUnits); }

	@Nonnull
	public Vec3 ApplyOverTicks(double ticks) { return Velocity.scale(ticks); }
	@Nonnull
	public Vec3 ApplyOneTick() { return Velocity; }




	public boolean IsApproxZero() { return Velocity.lengthSqr() < Maths.EpsilonSq; }
}
