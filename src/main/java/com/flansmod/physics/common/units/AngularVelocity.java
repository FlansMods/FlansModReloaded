package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record AngularVelocity(@Nonnull Vec3 Axis, double Magnitude)
{
	public static final AngularVelocity Zero = new AngularVelocity(Vec3.ZERO, 0.0d);

	@Nonnull
	public static AngularVelocity radiansPerSecond(@Nonnull Vec3 axis, double radiansPerSecond)
	{
		return new AngularVelocity(axis, Units.AngularSpeed.RadiansPerSecond_To_RadiansPerTick(radiansPerSecond));
	}
	@Nonnull
	public static AngularVelocity radiansPerTick(@Nonnull Vec3 axis, double radiansPerTick)
	{
		return new AngularVelocity(axis, radiansPerTick);
	}
	@Nonnull
	public static AngularVelocity degreesPerSecond(@Nonnull Vec3 axis, double degressPerSecond)
	{
		return new AngularVelocity(axis, Units.AngularSpeed.DegreesPerSecond_To_RadiansPerTick(degressPerSecond));
	}

	@Nonnull
	public AngularVelocity compose(@Nonnull AngularVelocity other)
	{
		Quaternionf angularA = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		Quaternionf angularB = new Quaternionf().setAngleAxis(other.Magnitude, other.Axis.x, other.Axis.y, other.Axis.z);
		Quaternionf composed = angularA.mul(angularB);
		AxisAngle4f axisAngle = new AxisAngle4f().set(composed);
		return new AngularVelocity(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle);
	}
	@Nonnull
	public AngularVelocity scale(double scale)
	{
		return new AngularVelocity(Axis, Magnitude * scale);
	}


	//
	//@Nonnull
	//public AngularVelocity add(@Nonnull AngularVelocity other) { return new AngularVelocity(Velocity.add(other.Velocity)); }

	@Nonnull
	public Units.AngularSpeed GetDefaultUnits() { return Units.AngularSpeed.RadiansPerTick; }
	public double ConvertToUnits(@Nonnull Units.AngularSpeed toUnits) { return Units.AngularSpeed.Convert(Magnitude, Units.AngularSpeed.RadiansPerTick, toUnits); }

	@Nonnull
	public Quaternionf ApplyOverTicks(double ticks) { return new Quaternionf().setAngleAxis(Magnitude * ticks, Axis.x, Axis.y, Axis.z); }
	@Nonnull
	public Quaternionf ApplyOneTick() { return new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z); }


	public boolean IsApproxZero() { return Maths.Approx(Magnitude, 0d); }

}
