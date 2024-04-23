package com.flansmod.packs.hogs.common.worldgen;

import com.flansmod.util.Maths;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class NetherRoofCarver extends WorldCarver<NetherRoofCarverConfiguration>
{
	public NetherRoofCarver(@Nonnull Codec<NetherRoofCarverConfiguration> codec)
	{
		super(codec);

	}

	private static final double CHUNK_DISCARD_DIST = 1000d;


	@Override
	public boolean isStartChunk(@Nonnull NetherRoofCarverConfiguration config, @Nonnull RandomSource random)
	{
		return true;
	}

	@Override
	public boolean carve(@Nonnull CarvingContext context,
						 @Nonnull NetherRoofCarverConfiguration config,
						 @Nonnull ChunkAccess chunk,
						 @Nonnull Function<BlockPos, Holder<Biome>> biomeLookup,
						 @Nonnull RandomSource random,
						 @Nonnull Aquifer aquifer,
						 @Nonnull ChunkPos chunkPos,
						 @Nonnull CarvingMask mask)
	{
		if(chunk.getPos().equals(chunkPos))
		{
			if (config.noiseParameters.unwrapKey().isPresent())
			{
				NormalNoise noise = context.randomState().getOrCreateNoise(config.noiseParameters.unwrapKey().get());

				int cellX = Maths.Modulo(chunkPos.x, 16);
				int cellZ = Maths.Modulo(chunkPos.z, 16);

				double posInCellX = Maths.Clamp(noise.getValue(127.0d + cellX * 0.023d, 32d, 31.0d + cellZ * 0.017d), 0d, 1d);
				double posInCellZ = Maths.Clamp(noise.getValue(131.0d + cellX * 0.029d, 32d, 17.0d + cellZ * 0.013d), 0d, 1d);
				Vec3 holeOrigin = new Vec3((cellX + posInCellX) * 16 * 16, 127d, (cellZ + posInCellZ) * 16 * 16);

				if(holeOrigin.distanceTo(chunkPos.getBlockAt(7, 127, 7).getCenter()) > CHUNK_DISCARD_DIST)
					return false;

				for(int i = 0; i < 16; i++)
				{
					for(int k = 0; k < 16; k++)
					{
						BlockPos blockPos = chunkPos.getBlockAt(i, 127, k);
						double distance = holeOrigin.distanceTo(blockPos.getCenter());
						if(7d < distance && distance < 10d)
							chunk.setBlockState(blockPos, Blocks.GREEN_WOOL.defaultBlockState(), false);
					}

				}

				return true;
			}
		}
		return false;
	}
}
