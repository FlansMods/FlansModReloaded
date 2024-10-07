package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.shapes.IPolygon;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record StaticCollisionEvent(@Nonnull IPolygon ContactSurface,
								   @Nonnull Vec3 ContactNormal,
								   double depth)
{
}
