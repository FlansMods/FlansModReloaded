package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record Torque(@Nonnull Vec3 Axis, double Magnitude) implements IForce
{
	public static final Torque Zero = new Torque(new Vec3(0d, 1d, 0d), 0.0d);

	@Nonnull
	public static Torque kgBlocksSqPerSecondSq(@Nonnull Vec3 axis, double kgBlocksSqPerSecondSq)
	{
		return new Torque(axis, Units.Force.KgBlocksPerSecondSq_To_KgBlocksPerTickSq(kgBlocksSqPerSecondSq));
	}
	@Nonnull
	public static Torque kgBlocksSqPerTickSq(@Nonnull Vec3 axis, double kgBlocksSqPerTickSq)
	{
		return new Torque(axis, kgBlocksSqPerTickSq);
	}


	@Nonnull
	public Torque compose(@Nonnull Torque other)
	{
		Quaternionf torqueA = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		Quaternionf torqueB = new Quaternionf().setAngleAxis(other.Magnitude, other.Axis.x, other.Axis.y, other.Axis.z);
		Quaternionf composed = torqueA.mul(torqueB);
		AxisAngle4f axisAngle = new AxisAngle4f().set(composed);
		return new Torque(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle);
	}

	@Nonnull
	public Units.Torque getDefaultUnits() { return Units.Torque.KgBlocksSqPerTickSq; }
	public double convertToUnits(@Nonnull Units.Torque toUnits) { return Units.Torque.Convert(Magnitude, Units.Torque.KgBlocksSqPerTickSq, toUnits); }
	@Nonnull
	public AngularAcceleration sumTorqueActingOn(double mass) { return new AngularAcceleration(Axis, Magnitude / mass); }
	@Nonnull
	public AngularAcceleration actingOn(Vec3 momentOfInertia)
	{
		// TODO: Moment of inertia
	 	return new AngularAcceleration(Axis, Magnitude / momentOfInertia.x);
	}

	@Override
	public boolean isApproxZero() { return Maths.Approx(Magnitude, 0d); }
	@Override @Nonnull
	public Torque inverse() { return new Torque(Axis, -Magnitude); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearForce getLinearComponent(@Nonnull Transform actingOn) { throw new NotImplementedException(); }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public Torque getTorqueComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public String toString() { return "Torque ["+Magnitude+"] around ["+Axis+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.torque", Magnitude, Axis.x, Axis.y, Axis.z); }

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof Torque otherForce)
			return otherForce.Axis.equals(Axis) && Maths.Approx(Magnitude, otherForce.Magnitude);
		return false;
	}
	public boolean isApprox(@Nonnull Torque other) { return Maths.Approx(other.Axis, Axis) && Maths.Approx(other.Magnitude, Magnitude); }
	public boolean isApprox(@Nonnull Torque other, double epsilon) { return Maths.Approx(other.Axis, Axis, epsilon) && Maths.Approx(other.Magnitude, Magnitude, epsilon); }

}