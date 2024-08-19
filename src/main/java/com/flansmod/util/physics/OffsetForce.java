package com.flansmod.util.physics;

import com.flansmod.util.Transform;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
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
	public Units.Force GetDefaultUnits() { return Units.Force.KgBlocksPerTickSquared; }
	@Nonnull
	public Vec3 ConvertToUnits(@Nonnull Units.Force toUnits) { return Units.Force.Convert(Force, Units.Force.KgBlocksPerTickSquared, toUnits); }
	@Nonnull
	public OffsetAcceleration ActingOn(double mass) { return new OffsetAcceleration(Force.scale(1d / mass), Offset); }
	@Nonnull
	public Quaternionf GetAngularComponentRadiansPerSecondSq(@Nonnull Transform actingOn)
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

	@Override
	public boolean HasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearForce GetLinearComponent(@Nonnull Transform actingOn) { return new LinearForce(Force); }
	@Override
	public boolean HasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public Torque GetTorqueComponent(@Nonnull Transform actingOn)
	{
		Vec3 relativeOffset = Offset.subtract(actingOn.PositionVec3());
		Vec3 axis = relativeOffset.cross(Force).normalize();
		double magnitude = Force.length() * relativeOffset.length();
		return new Torque(axis, magnitude);
	}

}