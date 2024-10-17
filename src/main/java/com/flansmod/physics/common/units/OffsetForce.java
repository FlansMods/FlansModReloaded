package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record OffsetForce(@Nonnull Vec3 Force, @Nonnull Vec3 Offset) implements IForce
{
	@Nonnull
	public static OffsetForce kgBlocksPerSecondSq(@Nonnull Vec3 kgBlocksPerSecSq, @Nonnull Vec3 offset)
	{
		return new OffsetForce(Units.Force.KgBlocksPerSecondSq_To_KgBlocksPerTickSq(kgBlocksPerSecSq), offset);
	}
	@Nonnull
	public static OffsetForce kgBlocksPerTickSq(@Nonnull Vec3 kgBlocksPerTickSq, @Nonnull Vec3 offset)
	{
		return new OffsetForce(kgBlocksPerTickSq, offset);
	}
	@Nonnull
	public static OffsetForce offset(@Nonnull LinearForce force, @Nonnull Vec3 offset)
	{
		return new OffsetForce(force.Force(), offset);
	}

	@Nonnull
	public Units.Force getDefaultUnits() { return Units.Force.KgBlocksPerTickSquared; }
	@Nonnull
	public Vec3 convertToUnits(@Nonnull Units.Force toUnits) { return Units.Force.Convert(Force, Units.Force.KgBlocksPerTickSquared, toUnits); }
	@Nonnull
	public OffsetAcceleration actingOn(double mass) { return new OffsetAcceleration(Force.scale(1d / mass), Offset); }
	@Nonnull
	public Quaternionf getAngularComponentRadiansPerSecondSq(@Nonnull Transform actingOn)
	{
		//Vec3 relativeOffset = Offset.subtract(actingOn.PositionVec3());
		//double radiusSq = relativeOffset.lengthSqr();
		//if(radiusSq > Maths.Epsilon)
		//{
		//	Vec3 cross = Vector.cross(relativeOffset);
		//	Vec3 axisOfRotation = cross.normalize();
		//	double angularMagSq = cross.lengthSqr();
//
		//	double radsPerTSq = angularMagSq / radiusSq;
		//	Quaternionf angular = new Quaternionf().rotateAxis((float)Maths.Sqrt(radsPerTSq), axisOfRotation.toVector3f());
//
		//	return switch (TimeUnit)
		//	{
		//		case Seconds -> angular;
		//		case Ticks -> angular.scale(TICKS_SQ_TO_SECONDS_SQ);
		//	};
		//}
		return new Quaternionf();
	}

	@Override @Nonnull
	public OffsetForce inverse() { return new OffsetForce(Force.scale(-1d), Offset); }
	@Override
	public boolean isApproxZero() { return Force.lengthSqr() < Maths.EpsilonSq; }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearForce getLinearComponent(@Nonnull Transform actingOn) { return new LinearForce(Force); }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public Torque getTorqueComponent(@Nonnull Transform actingOn)
	{
		Vec3 relativeOffset = Offset.subtract(actingOn.positionVec3());
		Vec3 axis = relativeOffset.cross(Force).normalize();
		double magnitude = Force.length() * relativeOffset.length();
		return new Torque(axis, magnitude);
	}
	@Override
	public String toString() { return "OffsetForce ["+Force+"] at ["+Offset+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.offset_force", Force.x, Force.y, Force.z, Offset.x, Offset.y, Offset.z); }

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof OffsetForce otherForce)
			return otherForce.Force.equals(Force) && otherForce.Offset.equals(Offset);
		return false;
	}
	public boolean isApprox(@Nonnull OffsetForce other) { return Maths.approx(other.Force, Force) && Maths.approx(other.Offset, Offset); }
	public boolean isApprox(@Nonnull OffsetForce other, double epsilon) { return Maths.approx(other.Force, Force, epsilon) && Maths.approx(other.Offset, Offset, epsilon); }
}