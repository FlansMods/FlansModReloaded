package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public interface IForce
{
	boolean isApproxZero();
	@Nonnull IForce inverse();
	boolean hasLinearComponent(@Nonnull Transform actingOn);
	@Nonnull LinearForce getLinearComponent(@Nonnull Transform actingOn);
	boolean hasAngularComponent(@Nonnull Transform actingOn);
	@Nonnull Torque getTorqueComponent(@Nonnull Transform actingOn);
	@Nonnull Component toFancyString();
}
