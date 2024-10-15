package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record AngularAcceleration(@Nonnull Vec3 Axis, double Magnitude) implements IAcceleration
{
	public static final AngularAcceleration Zero = new AngularAcceleration(new Vec3(0d, 1d, 0d), 0d);

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
	public static AngularAcceleration fromUtoVinTicks(@Nonnull AngularVelocity u, @Nonnull AngularVelocity v, int ticks)
	{
		if(ticks == 0)
			return Zero;

		Quaternionf uPerT = new Quaternionf().setAngleAxis(u.Magnitude(), u.Axis().x, u.Axis().y, u.Axis().z);
		Quaternionf vPerT = new Quaternionf().setAngleAxis(v.Magnitude(), v.Axis().x, v.Axis().y, v.Axis().z);
		uPerT.invert();
		Quaternionf composed = vPerT.mul(uPerT); // VU^-1
		AxisAngle4f axisAngle = new AxisAngle4f().set(composed);

		// v = u+at, a=(v-u)/t, or in this case A = [VU^-1] / t
		return new AngularAcceleration(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle / ticks);
	}

	@Nonnull
	public Torque multiplyBy(double mass) { return new Torque(Axis, Magnitude * mass); }
	@Nonnull
	public Units.AngularAcceleration getDefaultUnits() { return Units.AngularAcceleration.RadiansPerTickSq; }
	public double getInUnits(@Nonnull Units.AngularAcceleration toUnit)
	{
		return Units.AngularAcceleration.Convert(Magnitude, Units.AngularAcceleration.RadiansPerTickSq, toUnit);
	}
	@Nonnull
	public AngularVelocity applyOverTicks(double ticks) { return new AngularVelocity(Axis, Magnitude * ticks); }
	@Nonnull
	public AngularVelocity applyOneTick() { return new AngularVelocity(Axis, Magnitude); }

	@Override
	public boolean isApproxZero() { return Maths.Approx(Magnitude, 0d); }
	@Override @Nonnull
	public AngularAcceleration inverse() { return new AngularAcceleration(Axis, -Magnitude); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearAcceleration getLinearComponent(@Nonnull Transform actingOn) { return LinearAcceleration.Zero; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularAcceleration getAngularComponent(@Nonnull Transform actingOn) { return this; }
	public boolean IsApproxZero() { return Maths.Approx(Magnitude, 0d); }
	@Override
	public String toString() { return "AngularAcceleration ["+Units.Angle.Radians_To_Degrees(Magnitude)+"] degrees/tick^2 around ["+Axis+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.angular_acceleration", Units.Angle.Radians_To_Degrees(Magnitude), Axis.x, Axis.y, Axis.z); }
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof AngularAcceleration otherAngularA)
			return otherAngularA.Axis.equals(Axis) && Maths.Approx(Magnitude, otherAngularA.Magnitude);
		return false;
	}
	public boolean isApprox(@Nonnull AngularAcceleration other) { return Maths.Approx(other.Axis, Axis) && Maths.Approx(other.Magnitude, Magnitude); }
	public boolean isApprox(@Nonnull AngularAcceleration other, double epsilon) { return Maths.Approx(other.Axis, Axis, epsilon) && Maths.Approx(other.Magnitude, Magnitude, epsilon); }
}
