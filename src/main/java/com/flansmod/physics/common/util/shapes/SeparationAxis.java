package com.flansmod.physics.common.util.shapes;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record SeparationAxis(@Nonnull Vec3 Normal) implements ISeparationAxis
{
    @Override @Nonnull
    public Vec3 GetNormal() { return Normal; }
    @Override
    public double Project(@Nonnull Vec3 point) { return point.dot(Normal); }

}
