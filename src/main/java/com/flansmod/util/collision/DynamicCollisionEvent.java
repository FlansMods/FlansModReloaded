package com.flansmod.util.collision;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record DynamicCollisionEvent(@Nonnull ColliderHandle ObjectHandle,
									@Nonnull Vec3 ContactPoint,
									@Nonnull Vec3 ContactNormal,
									double depth)
{

}
