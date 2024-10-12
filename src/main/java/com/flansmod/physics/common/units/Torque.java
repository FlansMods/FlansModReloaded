package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
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
	public Units.Torque GetDefaultUnits() { return Units.Torque.KgBlocksSqPerTickSq; }
	public double ConvertToUnits(@Nonnull Units.Torque toUnits) { return Units.Torque.Convert(Magnitude, Units.Torque.KgBlocksSqPerTickSq, toUnits); }
	@Nonnull
	public AngularAcceleration SumTorqueActingOn(double mass) { return new AngularAcceleration(Axis, Magnitude / mass); }
	@Nonnull
	public AngularAcceleration ActingOn(Vec3 momentOfInertia)
	{
		// TODO: Moment of inertia
	 	return new AngularAcceleration(Axis, Magnitude / momentOfInertia.x);
	}


	@Override
	public boolean HasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearForce GetLinearComponent(@Nonnull Transform actingOn) { throw new NotImplementedException(); }
	@Override
	public boolean HasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public Torque GetTorqueComponent(@Nonnull Transform actingOn) { return this; }
}