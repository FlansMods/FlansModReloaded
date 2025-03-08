package com.flansmod.common.roads;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.TorchBlock;

import javax.annotation.Nonnull;

public class BlockRoadMarker extends TorchBlock
{
	public BlockRoadMarker(@Nonnull Properties props)
	{
		super(props, DustParticleOptions.REDSTONE);
	}
}
