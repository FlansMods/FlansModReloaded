package com.flansmod.physics.common.util;

import com.flansmod.physics.common.units.LinearForce;
import com.flansmod.physics.common.units.Torque;

import javax.annotation.Nonnull;

public interface IForce
{
	boolean HasLinearComponent(@Nonnull Transform actingOn);
	@Nonnull
	LinearForce GetLinearComponent(@Nonnull Transform actingOn);
	boolean HasAngularComponent(@Nonnull Transform actingOn);
	@Nonnull
	Torque GetTorqueComponent(@Nonnull Transform actingOn);
}
