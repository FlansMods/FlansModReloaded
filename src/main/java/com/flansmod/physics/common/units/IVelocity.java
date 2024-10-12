package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public interface IVelocity
{
    boolean isApproxZero();
    @Nonnull IVelocity inverse();
    boolean hasLinearComponent(@Nonnull Transform actingOn);
    @Nonnull LinearVelocity getLinearComponent(@Nonnull Transform actingOn);
    boolean hasAngularComponent(@Nonnull Transform actingOn);
    @Nonnull AngularVelocity getAngularComponent(@Nonnull Transform actingOn);
    @Nonnull Component toFancyString();
}
