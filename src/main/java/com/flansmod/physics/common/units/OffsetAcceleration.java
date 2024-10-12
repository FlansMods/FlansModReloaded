package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record OffsetAcceleration(@Nonnull Vec3 Acceleration, @Nonnull Vec3 Origin) implements IAcceleration
{
	public static final OffsetAcceleration Zero = new OffsetAcceleration(Vec3.ZERO, Vec3.ZERO);

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
	public static OffsetAcceleration offset(@Nonnull LinearAcceleration acceleration, @Nonnull Vec3 offset)
	{
		return new OffsetAcceleration(acceleration.Acceleration(), offset);
	}
	@Nonnull
	public Vec3 GetTicksPerSecondSq() { return Acceleration; }
	@Nonnull
	public Vec3 GetBlocksPerSecondSq() { return GetInUnits(Units.Acceleration.BlocksPerSecondSquared); }

	@Nonnull
	public Pair<LinearVelocity, AngularVelocity> ApplyOneTick() { return ApplyOneTick(Transform.IDENTITY); }
	@Nonnull
	public Pair<LinearVelocity, AngularVelocity> ApplyOneTick(@Nonnull Transform actingOn)
	{
		return Pair.of(GetLinearComponent(actingOn).ApplyOneTick(), GetAngularComponent(actingOn).ApplyOneTick());
	}

	@Nonnull
	public LinearForce MultiplyBy(double mass) { return new LinearForce(Acceleration.scale(mass)); }
	@Nonnull
	public Units.Acceleration GetDefaultUnits() { return Units.Acceleration.BlocksPerTickSquared; }
	@Nonnull
	public Vec3 GetInUnits(@Nonnull Units.Acceleration toUnit)
	{
		return Units.Acceleration.Convert(Acceleration, Units.Acceleration.BlocksPerTickSquared, toUnit);
	}

	@Override
	public boolean HasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearAcceleration GetLinearComponent(@Nonnull Transform actingOn)
	{
		return new LinearAcceleration(Acceleration);
	}
	@Override
	public boolean HasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularAcceleration GetAngularComponent(@Nonnull Transform actingOn)
	{
		Vec3 relativeOffset = Origin.subtract(actingOn.PositionVec3());
		Vec3 axis = relativeOffset.cross(Acceleration).normalize();
		double magnitude = Acceleration.length() * relativeOffset.length();
		return new AngularAcceleration(axis, magnitude);
	}
}
