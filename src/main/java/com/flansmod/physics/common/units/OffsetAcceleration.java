package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
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
	public Vec3 getTicksPerSecondSq() { return Acceleration; }
	@Nonnull
	public Vec3 getBlocksPerSecondSq() { return getInUnits(Units.Acceleration.BlocksPerSecondSquared); }

	@Nonnull
	public Pair<LinearVelocity, AngularVelocity> applyOneTick() { return applyOneTick(Transform.IDENTITY); }
	@Nonnull
	public Pair<LinearVelocity, AngularVelocity> applyOneTick(@Nonnull Transform actingOn)
	{
		return Pair.of(getLinearComponent(actingOn).applyOneTick(), getAngularComponent(actingOn).applyOneTick());
	}

	@Nonnull
	public LinearForce multiplyBy(double mass) { return new LinearForce(Acceleration.scale(mass)); }
	@Nonnull
	public Units.Acceleration getDefaultUnits() { return Units.Acceleration.BlocksPerTickSquared; }
	@Nonnull
	public Vec3 getInUnits(@Nonnull Units.Acceleration toUnit)
	{
		return Units.Acceleration.Convert(Acceleration, Units.Acceleration.BlocksPerTickSquared, toUnit);
	}
	
	@Override @Nonnull
	public OffsetAcceleration inverse() { return new OffsetAcceleration(Acceleration.scale(-1d), Origin); }
	@Override
	public boolean isApproxZero() { return Acceleration.lengthSqr() < Maths.EpsilonSq; }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearAcceleration getLinearComponent(@Nonnull Transform actingOn)
	{
		return new LinearAcceleration(Acceleration);
	}
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularAcceleration getAngularComponent(@Nonnull Transform actingOn)
	{
		Vec3 relativeOffset = Origin.subtract(actingOn.positionVec3());
		Vec3 axis = relativeOffset.cross(Acceleration).normalize();
		double magnitude = Acceleration.length() * relativeOffset.length();
		return new AngularAcceleration(axis, magnitude);
	}
	@Override
	public String toString() { return "OffsetAcceleration ["+Acceleration+"] at ["+Origin+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.offset_acceleration", Acceleration.x, Acceleration.y, Acceleration.z, Origin.x, Origin.y, Origin.z); }
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof OffsetAcceleration otherOffsetA)
			return otherOffsetA.Acceleration.equals(Acceleration) && otherOffsetA.Origin.equals(Origin);
		return false;
	}
	public boolean isApprox(@Nonnull OffsetAcceleration other) { return Maths.Approx(other.Acceleration, Acceleration) && Maths.Approx(other.Origin, Origin); }
	public boolean isApprox(@Nonnull OffsetAcceleration other, double epsilon) { return Maths.Approx(other.Acceleration, Acceleration, epsilon) && Maths.Approx(other.Origin, Origin, epsilon); }
}
