package com.flansmod.packs.hogs.common.worldgen;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class RoadCarver extends WorldCarver<RoadCarverConfiguration>
{
	private static final int BLOCK_TO_CHUNK = 16;
	private static final int CHUNK_TO_URBAN = 16;
	private static final int URBAN_TO_REGIONAL = 4;
	public record RoadMapperLayer(@Nonnull RoadLayer Layer,
								  int NoiseSampleLevel,
								  int AbsoluteScaleFromBlock,
								  int RelativeScaleFromNextLayer,
								  double RoadWidth,
								  double ConnectionChance)
	{
		@Nonnull public BlockPos BlockMin(int cellX, int cellZ) { return new BlockPos(cellX * AbsoluteScaleFromBlock, 0, cellZ * AbsoluteScaleFromBlock); }
		@Nonnull public BlockPos BlockMax(int cellX, int cellZ) { return new BlockPos((cellX + 1) * AbsoluteScaleFromBlock - 1, 0, (cellZ + 1) * AbsoluteScaleFromBlock - 1); }
		@Nonnull public ChunkPos ChunkMin(int cellX, int cellZ) { return new ChunkPos(cellX * AbsoluteScaleFromBlock / BLOCK_TO_CHUNK, cellZ * AbsoluteScaleFromBlock / BLOCK_TO_CHUNK); }
		public int BlockWidth() { return AbsoluteScaleFromBlock; }
		public int BlockHeight() { return AbsoluteScaleFromBlock; }
		public int ChunkWidth() { return AbsoluteScaleFromBlock / BLOCK_TO_CHUNK; }
		public int ChunkHeight() { return AbsoluteScaleFromBlock / BLOCK_TO_CHUNK; }
		public int GetCellOfBlockX(int blockX) { return Math.floorDiv(blockX, AbsoluteScaleFromBlock); }
		public int GetCellOfBlockZ(int blockZ) { return Math.floorDiv(blockZ, AbsoluteScaleFromBlock); }


		public double ClampPointToEdge(double noiseSample01) { return noiseSample01 * (AbsoluteScaleFromBlock - RoadWidth * 2) / AbsoluteScaleFromBlock; }
		public int SampleBlockOnEdge(double noiseSample01) { return Maths.Floor(RoadWidth + noiseSample01 * (AbsoluteScaleFromBlock - RoadWidth * 2)); }

		// Random generation stuff
		// How many BLOCKS along the north edge do we enter this cell?
		public int GetNorthConnectionX(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return (int) Math.floor(AbsoluteScaleFromBlock * noise.getValue(cellX, 0, cellZ));
		}
		public int GetSouthConnectionX(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return (int) Math.floor(AbsoluteScaleFromBlock * noise.getValue(-cellX, 13, cellZ));
		}
		public int GetEastConnectionZ(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return (int) Math.floor(AbsoluteScaleFromBlock * noise.getValue(-cellX, 29, -cellZ));
		}
		public int GetWestConnectionZ(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return (int) Math.floor(AbsoluteScaleFromBlock * noise.getValue(cellX, 41, -cellZ));
		}
	}

	public static class RoadMappingCell
	{
		@Nonnull public final RoadMapperLayer Layer;
		public int CellX;
		public int CellZ;

		public RoadMappingCell(@Nonnull RoadMapperLayer layer, int cellX, int cellZ)
		{
			Layer = layer;
			CellX = cellX;
			CellZ = cellZ;
		}

		public boolean HasConnection(@Nonnull NormalNoise noise, @Nonnull Direction direction)
		{
			double sampleX = CellX + 0.5d * (1+direction.getStepX());
			double sampleZ = CellZ + 0.5d * (1+direction.getStepZ());
			double sample = noise.getValue(sampleX, Layer.NoiseSampleLevel, sampleZ);
			double sample01 = Maths.Clamp((1d + sample) * 0.5d, 0d, 1d);
			return sample01 <= Layer.ConnectionChance;
		}

		@Nonnull
		public Vec3 GetConnectionPosition(@Nonnull NormalNoise noise, @Nonnull Direction direction)
		{
			double sampleX = CellX + 0.5d * (1+direction.getStepX());
			double sampleZ = CellZ + 0.5d * (1+direction.getStepZ());

			double sample = noise.getValue(sampleX, 63 - Layer.NoiseSampleLevel, sampleZ);
			double sample01 = Maths.Clamp((1d + sample) * 0.5d, 0d, 1d);
			int distAlongEdge = Layer.SampleBlockOnEdge(sample01);
			int scale = Layer.AbsoluteScaleFromBlock;
			return switch(direction)
			{
				case NORTH -> new Vec3(CellX * scale + distAlongEdge, 0, (CellZ) * scale);
				case SOUTH -> new Vec3(CellX * scale + distAlongEdge, 0, (CellZ+1) * scale);

				case EAST -> new Vec3((CellX+1) * scale, 0, CellZ * scale + distAlongEdge);
				case WEST -> new Vec3((CellX) * scale, 0, CellZ * scale + distAlongEdge);

				default -> Vec3.ZERO;
			};
		}

	}

	public final List<RoadMapperLayer> MapperLayers;

	public RoadCarver(Codec<RoadCarverConfiguration> codec)
	{
		super(codec);

		MapperLayers = new ArrayList<>();
		MapperLayers.add(new RoadMapperLayer(RoadLayer.Chunk, 13, BLOCK_TO_CHUNK, BLOCK_TO_CHUNK, 2, 0.5d));
		MapperLayers.add(new RoadMapperLayer(RoadLayer.Urban, 27, BLOCK_TO_CHUNK * CHUNK_TO_URBAN, CHUNK_TO_URBAN, 3, 0.4d));
		MapperLayers.add(new RoadMapperLayer(RoadLayer.Regional, 45, BLOCK_TO_CHUNK * CHUNK_TO_URBAN * URBAN_TO_REGIONAL, URBAN_TO_REGIONAL, 6, 0.75d));


	}

	public enum RoadLayer
	{

		Regional,
		Urban,
		Chunk,
	}





	@Override
	public boolean carve(@Nonnull CarvingContext context,
						 @Nonnull RoadCarverConfiguration config,
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

				for(int i = MapperLayers.size() - 1; i>=0; i--)
				{
					RoadMapperLayer layer = MapperLayers.get(i);

					RoadMappingCell cell = new RoadMappingCell(layer, layer.GetCellOfBlockX(chunkPos.getMinBlockX()), layer.GetCellOfBlockZ(chunkPos.getMinBlockZ()));




				}


				double northEdgeNoise = noise.getValue(1000d * (chunkPos.x + 0.5d), 	0, 1000d * (chunkPos.z));
				double southEdgeNoise = noise.getValue(1000d * (chunkPos.x + 0.5d), 	0, 1000d * (chunkPos.z + 1));
				double eastEdgeNoise = noise.getValue( 1000d * (chunkPos.x + 1), 		0, 1000d * (chunkPos.z + 0.5d));
				double westEdgeNoise = noise.getValue( 1000d * (chunkPos.x), 			0, 1000d * (chunkPos.z + 0.5d));

				Vec3 northEntryPoint = new Vec3((chunkPos.x + northEdgeNoise) * 16d, 90d, (chunkPos.z) * 16d);
				Vec3 southEntryPoint = new Vec3((chunkPos.x + southEdgeNoise) * 16d, 90d, (chunkPos.z + 1) * 16d);
				Vec3 eastEntryPoint = new Vec3((chunkPos.x + 1) * 16d, 90d, (chunkPos.z + eastEdgeNoise) * 16d);
				Vec3 westEntryPoint = new Vec3((chunkPos.x) * 16d, 90d, (chunkPos.z + westEdgeNoise) * 16d);

				for (float step = 0.0f; step < 1.0f; step += 0.99f)
				{
					chunk.setBlockState(BlockPos.containing(northEntryPoint.lerp(eastEntryPoint, step)), Blocks.IRON_BLOCK.defaultBlockState(), false);
					chunk.setBlockState(BlockPos.containing(eastEntryPoint.lerp(southEntryPoint, step)), Blocks.IRON_BLOCK.defaultBlockState(), false);
					chunk.setBlockState(BlockPos.containing(southEntryPoint.lerp(westEntryPoint, step)), Blocks.IRON_BLOCK.defaultBlockState(), false);
					chunk.setBlockState(BlockPos.containing(westEntryPoint.lerp(northEntryPoint, step)), Blocks.IRON_BLOCK.defaultBlockState(), false);
				}
				//for(int i = 1; i < 15; i++)
				//{
				//	for(int k = 1; k < 15; k++)
				//	{
				//		level.setBlockState(chunkPos.getBlockAt(i, 90, k), Blocks.IRON_BLOCK.defaultBlockState(), false);
				//	}
				//}
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isStartChunk(@Nonnull RoadCarverConfiguration config, @Nonnull RandomSource random)
	{
		return true;
	}
}