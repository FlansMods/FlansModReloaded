package com.flansmod.physics.common.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class CenteredEntityDimensions extends EntityDimensions
{
    @Nonnull
    public static CenteredEntityDimensions scalable(float width, float height) {
        return new CenteredEntityDimensions(width, height, false);
    }
    @Nonnull
    public static CenteredEntityDimensions fixed(float width, float height) {
        return new CenteredEntityDimensions(width, height, true);
    }

    public CenteredEntityDimensions(float width, float height, boolean fixed)
    {
        super(width, height, fixed);
    }

    @Override @Nonnull
    public AABB makeBoundingBox(double x, double y, double z)
    {
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        return new AABB(x - halfWidth, y - halfHeight, z - halfWidth,
                        x + halfWidth, y + halfHeight, z + halfWidth);
    }

    @Override @Nonnull
    public CenteredEntityDimensions scale(float widthScale, float heightScale)
    {
        return !fixed && (widthScale != 1.0F || heightScale != 1.0F)
                ? scalable(width * widthScale, height * heightScale)
                : this;
    }
}
