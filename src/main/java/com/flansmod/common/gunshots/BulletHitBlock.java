package com.flansmod.common.gunshots;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletHitBlock extends BulletHit
{
    private BlockHitResult raytraceResult;

    public BulletHitBlock(BlockHitResult mop, double time)
    {
        super(time);
        raytraceResult = mop;
    }

    @Override
    public Entity GetEntity() { return null; }
    public BlockHitResult GetRayTraceResult()
    {
        return raytraceResult;
    }
}
