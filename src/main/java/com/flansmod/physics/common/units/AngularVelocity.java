package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record AngularVelocity(@Nonnull Vec3 Axis, double Magnitude) implements IVelocity
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
	public Units.AngularSpeed getDefaultUnits() { return Units.AngularSpeed.RadiansPerTick; }
	public double convertToUnits(@Nonnull Units.AngularSpeed toUnits) { return Units.AngularSpeed.Convert(Magnitude, Units.AngularSpeed.RadiansPerTick, toUnits); }

	@Nonnull
	public Quaternionf applyOverTicks(double ticks) { return new Quaternionf().setAngleAxis(Magnitude * ticks, Axis.x, Axis.y, Axis.z); }
	@Nonnull
	public Quaternionf applyOneTick() { return new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z); }

	@Override @Nonnull
	public AngularVelocity inverse() { return new AngularVelocity(Axis, -Magnitude); }
	@Override
	public boolean isApproxZero() { return Maths.Approx(Magnitude, 0d); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearVelocity getLinearComponent(@Nonnull Transform actingOn) { return LinearVelocity.Zero; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularVelocity getAngularComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public String toString() { return "AngularVelocity ["+Units.Angle.Radians_To_Degrees(Magnitude)+"] degrees/tick around ["+Axis+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.angular_velocity", Units.Angle.Radians_To_Degrees(Magnitude), Axis.x, Axis.y, Axis.z); }
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof AngularVelocity otherAngularV)
			return otherAngularV.Axis.equals(Axis) && Maths.Approx(Magnitude, otherAngularV.Magnitude);
		return false;
	}
	public boolean isApprox(@Nonnull AngularVelocity other) { return Maths.Approx(other.Axis, Axis) && Maths.Approx(other.Magnitude, Magnitude); }
	public boolean isApprox(@Nonnull AngularVelocity other, double epsilon) { return Maths.Approx(other.Axis, Axis, epsilon) && Maths.Approx(other.Magnitude, Magnitude, epsilon); }
	public void toBuf(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeVector3f(Axis.toVector3f());
		buf.writeFloat((float)Magnitude);
	}
	@Nonnull
	public static AngularVelocity fromBuf(@Nonnull FriendlyByteBuf buf)
	{
		Vec3 axis = new Vec3(buf.readVector3f());
		double mag = buf.readFloat();
		return new AngularVelocity(axis, mag);
	}
}
