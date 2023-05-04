package com.flansmod.common.gunshots;

import net.minecraft.world.entity.Entity;

public class BulletHitEntity extends BulletHit
{
    private Entity entity;

    public BulletHitEntity(Entity e, double d)
    {
        super(d);
        entity = e;
    }

    @Override
    public Entity GetEntity() { return entity; }
}
