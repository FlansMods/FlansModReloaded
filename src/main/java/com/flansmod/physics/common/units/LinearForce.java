package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

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
	public Units.Force getDefaultUnits() { return Units.Force.KgBlocksPerTickSquared; }
	@Nonnull
	public Vec3 convertToUnits(@Nonnull Units.Force toUnits) { return Units.Force.Convert(Force, Units.Force.KgBlocksPerTickSquared, toUnits); }
	@Nonnull
	public LinearAcceleration actingOn(double mass) { return new LinearAcceleration(Force.scale(1d / mass)); }

	@Override
	public boolean isApproxZero() { return Force.lengthSqr() < Maths.EpsilonSq; }
	@Override @Nonnull
	public LinearForce inverse() { return new LinearForce(Force.scale(-1d)); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearForce getLinearComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public Torque getTorqueComponent(@Nonnull Transform actingOn) { throw new NotImplementedException(); }
	@Override
	public String toString() { return "LinearForce ["+Force+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.linear_force", Force.x, Force.y, Force.z); }

}