package com.flansmod.util.physics;

import com.flansmod.util.Transform;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record LinearForce(@Nonnull Vec3 Force) implements IForce
{
	public static final LinearForce Zero = new LinearForce(Vec3.ZERO);

	@Nonnull
	public static LinearForce kgBlocksPerSecondSq(@Nonnull Vec3 kgBlocksPerSecSq) { return new LinearForce(Units.Force.KgBlocksPerSecondSq_To_KgBlocksPerTickSq(kgBlocksPerSecSq)); }
	@Nonnull
	public static LinearForce kgBlocksPerTickSq(@Nonnull Vec3 kgBlocksPerTickSq) { return new LinearForce(kgBlocksPerTickSq); }

	@Nonnull
	public LinearForce add(@Nonnull LinearForce other)
	{
		return new LinearForce(Force.add(other.Force));
	}

	@Nonnull
	public Units.Force GetDefaultUnits() { return Units.Force.KgBlocksPerTickSquared; }
	@Nonnull
	public Vec3 ConvertToUnits(@Nonnull Units.Force toUnits) { return Units.Force.Convert(Force, Units.Force.KgBlocksPerTickSquared, toUnits); }
	@Nonnull
	public LinearAcceleration ActingOn(double mass) { return new LinearAcceleration(Force.scale(1d / mass)); }

	@Override
	public boolean HasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearForce GetLinearComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public boolean HasAngularComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public Torque GetTorqueComponent(@Nonnull Transform actingOn) { throw new NotImplementedException(); }
}