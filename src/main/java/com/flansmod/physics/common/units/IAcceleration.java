package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;

import javax.annotation.Nonnull;

public interface IAcceleration
{
    boolean HasLinearComponent(@Nonnull Transform actingOn);
    @Nonnull LinearAcceleration GetLinearComponent(@Nonnull Transform actingOn);
    boolean HasAngularComponent(@Nonnull Transform actingOn);
    @Nonnull AngularAcceleration GetAngularComponent(@Nonnull Transform actingOn);
}
