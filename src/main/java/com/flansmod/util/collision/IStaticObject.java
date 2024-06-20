package com.flansmod.util.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

public interface IStaticObject
{
	@Nonnull Vec3 Position();
	@Nonnull List<AABB> Colliders();
}
