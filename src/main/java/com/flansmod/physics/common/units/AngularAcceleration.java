package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record AngularAcceleration(@Nonnull Vec3 Axis, double Magnitude)
{
	@Nonnull
	public static AngularAcceleration radiansPerSecondSq(@Nonnull Vec3 axis, double radsPerSecSq)
	{
		return new AngularAcceleration(axis, Units.AngularAcceleration.Convert(radsPerSecSq, Units.AngularAcceleration.RadiansPerSecondSq, Units.AngularAcceleration.RadiansPerTickSq));
	}
	@Nonnull
	public static AngularAcceleration radiansPerTickSq(@Nonnull Vec3 axis, double radsPerTickSq)
	{
		return new AngularAcceleration(axis, radsPerTickSq);
	}
	@Nonnull
	public static AngularAcceleration degreesPerSecondSq(@Nonnull Vec3 axis, double degsPerSecSq)
	{
		return new AngularAcceleration(axis, Units.AngularAcceleration.Convert(degsPerSecSq, Units.AngularAcceleration.DegreesPerSecondSq, Units.AngularAcceleration.RadiansPerTickSq));
	}
	@Nonnull
	public static AngularAcceleration degreesPerTickSq(@Nonnull Vec3 axis, double degsPerTickSq)
	{
		return new AngularAcceleration(axis, Units.AngularAcceleration.Convert(degsPerTickSq, Units.AngularAcceleration.DegreesPerTickSq, Units.AngularAcceleration.RadiansPerTickSq));
	}


	@Nonnull
	public Torque MultiplyBy(double mass) { return new Torque(Axis, Magnitude * mass); }
	@Nonnull
	public Units.AngularAcceleration GetDefaultUnits() { return Units.AngularAcceleration.RadiansPerTickSq; }
	public double GetInUnits(@Nonnull Units.AngularAcceleration toUnit)
	{
		return Units.AngularAcceleration.Convert(Magnitude, Units.AngularAcceleration.RadiansPerTickSq, toUnit);
	}
	@Nonnull
	public AngularVelocity ApplyOverTicks(double ticks) { return new AngularVelocity(Axis, Magnitude * ticks); }
	@Nonnull
	public AngularVelocity ApplyOneTick() { return new AngularVelocity(Axis, Magnitude); }


	public boolean IsApproxZero() { return Maths.Approx(Magnitude, 0d); }
}
