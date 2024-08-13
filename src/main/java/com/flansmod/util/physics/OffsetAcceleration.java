package com.flansmod.util.physics;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record OffsetAcceleration(@Nonnull Vec3 Acceleration, @Nonnull Vec3 Origin)
{
	@Nonnull
	public static OffsetAcceleration blocksPerSecondSq(@Nonnull Vec3 blocksPerSecSq, @Nonnull Vec3 offset)
	{
		return new OffsetAcceleration(Units.Acceleration.BlocksPerSecondSq_To_BlocksPerTickSq(blocksPerSecSq), offset);
	}
	@Nonnull
	public static OffsetAcceleration blocksPerTickSq(@Nonnull Vec3 blocksPerTickSq, @Nonnull Vec3 offset)
	{
		return new OffsetAcceleration(blocksPerTickSq, offset);
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
}
