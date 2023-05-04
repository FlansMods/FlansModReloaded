package com.flansmod.common.gunshots;

import net.minecraft.world.entity.Entity;

public abstract class BulletHit implements Comparable<BulletHit>
{
    public static final BulletHitNothing NOTHING = new BulletHitNothing();

    public static class BulletHitNothing extends BulletHit
    {
        public BulletHitNothing() { super(Double.MAX_VALUE); }
        @Override
        public Entity GetEntity() { return null; }
    }

    // The time along the ray that the intersection happened. Between 0 and 1
    public double intersectTime;

    public BulletHit(double d)
    {
        intersectTime = d;
    }

    public boolean IsValid() { return intersectTime < 1e20; }

    @Override
    public int compareTo(BulletHit other)
    {
        if(intersectTime < other.intersectTime)
            return -1;
        else if(intersectTime > other.intersectTime)
            return 1;
        return 0;
    }

    public abstract Entity GetEntity();
}