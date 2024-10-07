package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

// Internally, always store as Blocks/Tick^2
public record LinearAcceleration(@Nonnull Vec3 Acceleration)
{
	@Nonnull
	public static LinearAcceleration blocksPerSecondSq(@Nonnull Vec3 blocksPerSecSq)
	{
		return new LinearAcceleration(Units.Acceleration.BlocksPerSecondSq_To_BlocksPerTickSq(blocksPerSecSq));
	}
	@Nonnull
	public static LinearAcceleration blocksPerTickSq(@Nonnull Vec3 blocksPerTickSq)
	{
		return new LinearAcceleration(blocksPerTickSq);
	}


	@Nonnull
	public Vec3 GetTicksPerSecondSq() { return Acceleration; }
	@Nonnull
	public Vec3 GetBlocksPerSecondSq() { return GetInUnits(Units.Acceleration.BlocksPerSecondSquared); }

	@Nonnull
	public LinearForce MultiplyBy(double mass) { return new LinearForce(Acceleration.scale(mass)); }
	@Nonnull
	public Units.Acceleration GetDefaultUnits() { return Units.Acceleration.BlocksPerTickSquared; }
	@Nonnull
	public Vec3 GetInUnits(@Nonnull Units.Acceleration toUnit)
	{
		return Units.Acceleration.Convert(Acceleration, Units.Acceleration.BlocksPerTickSquared, toUnit);
	}
	@Nonnull
	public LinearVelocity ApplyOverTicks(double ticks) { return new LinearVelocity(Acceleration.scale(ticks)); }
	@Nonnull
	public LinearVelocity ApplyOneTick() { return new LinearVelocity(Acceleration); }


	public boolean IsApproxZero() { return Acceleration.lengthSqr() < Maths.EpsilonSq; }

}
