package com.flansmod.util.physics;

import com.flansmod.util.Transform;

import javax.annotation.Nonnull;

public interface IForce
{
	boolean HasLinearComponent(@Nonnull Transform actingOn);
	@Nonnull LinearForce GetLinearComponent(@Nonnull Transform actingOn);
	boolean HasAngularComponent(@Nonnull Transform actingOn);
	@Nonnull Torque GetTorqueComponent(@Nonnull Transform actingOn);
}
