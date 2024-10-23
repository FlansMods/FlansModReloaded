package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.CompoundAcceleration;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;

import javax.annotation.Nonnull;

public interface IDynamicObjectUpdateReceiver
{
    void handleStaticCollision(@Nonnull StaticCollisionEvent event);
    void handleDynamicCollision(@Nonnull DynamicCollisionEvent event);
    void updateLocation(@Nonnull Transform newLoc);
    void updateLinearVelocity(@Nonnull LinearVelocity linearVelocity);
    void updateAngularVelocity(@Nonnull AngularVelocity angularVelocity);
    void updateReactionForce(@Nonnull CompoundAcceleration reactionAcceleration);
}
