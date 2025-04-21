package com.flansmod.airtraffic.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;

public record AirmapPos(int x, int z)
{
	public static final int AIRMAP_REGION_BITSHIFT_X = 8;
	public static final int AIRMAP_REGION_CHUNKS_X = (1 << AIRMAP_REGION_BITSHIFT_X);
	public static final int AIRMAP_REGION_BITSHIFT_Z = 8;
	public static final int AIRMAP_REGION_CHUNKS_Z = (1 << AIRMAP_REGION_BITSHIFT_Z);
	public static int chunkToAirmapX(int chunkX) { return chunkX >> AIRMAP_REGION_BITSHIFT_X; }
	public static int chunkToAirmapZ(int chunkZ) { return chunkZ >> AIRMAP_REGION_BITSHIFT_Z; }
	public static int blockToAirmapX(int blockX) { return blockX >> (4 + AIRMAP_REGION_BITSHIFT_X); }
	public static int blockToAirmapZ(int blockZ) { return blockZ >> (4 + AIRMAP_REGION_BITSHIFT_Z); }

	public AirmapPos() { this(0,0); }
	public AirmapPos(@Nonnull ChunkPos chunkPos) { this(chunkToAirmapX(chunkPos.x), chunkToAirmapZ(chunkPos.z)); }
	public AirmapPos(@Nonnull BlockPos blockPos) { this(blockToAirmapX(blockPos.getX()), blockToAirmapZ(blockPos.getZ())); }


}
