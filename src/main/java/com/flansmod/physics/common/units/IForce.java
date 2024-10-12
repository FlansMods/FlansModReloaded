package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;

import javax.annotation.Nonnull;

public interface IForce
{
	boolean HasLinearComponent(@Nonnull Transform actingOn);
	@Nonnull LinearForce GetLinearComponent(@Nonnull Transform actingOn);
	boolean HasAngularComponent(@Nonnull Transform actingOn);
	@Nonnull Torque GetTorqueComponent(@Nonnull Transform actingOn);
}
