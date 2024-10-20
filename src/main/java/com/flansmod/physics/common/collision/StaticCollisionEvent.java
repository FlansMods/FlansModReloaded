package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.shapes.IPlane;
import com.flansmod.physics.common.util.shapes.IPolygon;

import javax.annotation.Nonnull;

public record StaticCollisionEvent(@Nonnull IPolygon contactSurface,
								   @Nonnull IPlane separationPlane,
								   double depth)
{
}
