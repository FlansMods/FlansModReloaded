package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IConstDynamicObject
{
    boolean isInvalid();

    @Nonnull AABB getLocalBounds();



    @Nonnull Transform getCurrentLocation();
    @Nonnull TransformedBB getCurrentBB();
    @Nonnull TransformedBBCollection getCurrentColliders();
    @Nonnull AABB getCurrentWorldBounds();

    @Nonnull Optional<Transform> getNextFrameTeleport();
    @Nonnull LinearVelocity getNextFrameLinearVelocity();
    @Nonnull AngularVelocity getNextFrameAngularVelocity();

    @Nonnull Transform getPendingLocation();
    @Nonnull TransformedBB getPendingBB();
    @Nonnull TransformedBBCollection getPendingColliders();
    @Nonnull AABB getPendingWorldBounds();
}
