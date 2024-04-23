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
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
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

		public enum EEdge
		{
			North,
			East,
			West,
			South,
		}

		public boolean IsEdgePresent(@Nonnull NormalNoise noise, int cellX, int cellZ, @Nonnull EEdge edge)
		{
			return switch(edge)
			{
				case North -> NorthSouthEdgeSample(noise, cellX, cellZ) > ConnectionChance;
				case South -> NorthSouthEdgeSample(noise, cellX, cellZ + 1) > ConnectionChance;
				case East -> EastWestEdgeSample(noise, cellX, cellZ) > ConnectionChance;
				case West -> EastWestEdgeSample(noise, cellX + 1, cellZ) > ConnectionChance;
			};
		}
		private double NorthSouthEdgeSample(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return noise.getValue(1433021d - 0.17d * cellX, NoiseSampleLevel, 3939941d + 0.23d * cellZ);
		}
		private double EastWestEdgeSample(@Nonnull NormalNoise noise, int cellX, int cellZ)
		{
			return noise.getValue(13411d - 0.31d * cellX, NoiseSampleLevel, 55571d + 0.29d * cellZ);
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


	public record RoadPath(@Nonnull List<Vec3> Nodes)
	{
		public void Wiggle(int firstIndex, double noiseSample, double magnitude)
		{
			int secondIndex = firstIndex + 1;
			if(secondIndex < Nodes.size())
			{
				Vec3 firstNode = Nodes.get(firstIndex);
				Vec3 secondNode = Nodes.get(secondIndex);

				Vec3 delta = secondNode.subtract(firstNode);
				Vec3 perp = new Vec3(-delta.z, 0d, delta.x);

				Vec3 newNode = firstNode.add(delta.scale(0.5d)).add(perp.scale(magnitude * noiseSample));
				Nodes.add(secondIndex, newNode);
			}
		}

		public double DistanceFromRoad(@Nonnull Vec3 pos)
		{
			pos = new Vec3(pos.x, 0, pos.z);
			double shortestDistance = Double.MAX_VALUE;
			for(int i = 0; i < Nodes.size() - 1; i++)
			{
				Vec3 from = Nodes.get(i);
				Vec3 to = Nodes.get(i + 1);
				Vec3 direction = to.subtract(from);

				double dist = DistanceToLineSegment(from, direction, pos);

				if (dist < shortestDistance)
				{
					shortestDistance = dist;
				}
			}
			return shortestDistance;
		}

		private double DistanceToLineSegment(@Nonnull Vec3 origin, @Nonnull Vec3 direction, @Nonnull Vec3 pos)
		{
			// We use xz of a Vec3 for simplicity
			double lineLengthSq = direction.x * direction.x + direction.z * direction.z;

			// Quick check for zero-length line
			if(lineLengthSq == 0)
				return Maths.Sqrt((pos.x - origin.x) * (pos.x - origin.x) + (pos.z - origin.z) * (pos.z - origin.z));

			// Line = origin + t * direction
			// Line segment is this in the range [0,1]
			// Project to line to find t, effectively dot(pos - origin, direction)
			double t = ((pos.x - origin.x) * (direction.x) + (pos.z - origin.z) * (direction.z)) / lineLengthSq;
			// Clamp to segment
			double tSegment = Maths.Clamp(t, 0, 1);
			// Now get distance to that clamped point

			Vec3 tPoint = new Vec3(origin.x + tSegment * direction.x, 0d, origin.z + tSegment * direction.z);
			return Maths.Sqrt((pos.x - tPoint.x) * (pos.x - tPoint.x) + (pos.z - tPoint.z) * (pos.z - tPoint.z));
		}
	}

	private static final double MIN_CONNECTION_DIST_FROM_CORNER = 0.1d;
	private static final int REGION_CHUNK_SIZE = 8;
	private static final int REGION_BLOCK_SIZE = 16 * REGION_CHUNK_SIZE;

	private static final double ROAD_WIDTH = 3d;
	private static final double ROAD_PLUS_BORDER_WIDTH = 4d;

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
		if(chunk.getPos().equals(chunkPos) && config.noiseParameters.unwrapKey().isPresent())
		{
			float WIGGLE_MAG_FIRST = config.wiggleMagnitudeFirstOrder.sample(random);
			float WIGGLE_MAG_SECOND = config.wiggleMagnitudeSecondOrder.sample(random);

			float ROAD_INNER_RADIUS = config.roadInnerRadius;
			float ROAD_OUTER_RADIUS = config.roadOuterRadius;

			NormalNoise noise = context.randomState().getOrCreateNoise(config.noiseParameters.unwrapKey().get());
			int regionX = Math.floorDiv(chunkPos.x, REGION_CHUNK_SIZE);
			int regionZ = Math.floorDiv(chunkPos.z, REGION_CHUNK_SIZE);

			double northEdgeNoise = 0.5d + 0.5d * noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ));
			double southEdgeNoise = 0.5d + 0.5d * noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 1));
			double eastEdgeNoise =  0.5d + 0.5d * noise.getValue(1000d * (regionX + 1), 	0, 1000d * (regionZ + 0.5d));
			double westEdgeNoise =  0.5d + 0.5d * noise.getValue(1000d * (regionX), 		0, 1000d * (regionZ + 0.5d));

			boolean connectedNorth = MIN_CONNECTION_DIST_FROM_CORNER < northEdgeNoise && northEdgeNoise < (1.0d - MIN_CONNECTION_DIST_FROM_CORNER);
			boolean connectedEast =  MIN_CONNECTION_DIST_FROM_CORNER < eastEdgeNoise  && eastEdgeNoise  < (1.0d - MIN_CONNECTION_DIST_FROM_CORNER);
			boolean connectedSouth = MIN_CONNECTION_DIST_FROM_CORNER < southEdgeNoise && southEdgeNoise < (1.0d - MIN_CONNECTION_DIST_FROM_CORNER);
			boolean connectedWest =  MIN_CONNECTION_DIST_FROM_CORNER < westEdgeNoise  && westEdgeNoise  < (1.0d - MIN_CONNECTION_DIST_FROM_CORNER);
			int numConnected = (connectedNorth ? 1 : 0) + (connectedEast ? 1 : 0) + (connectedSouth ? 1 : 0) + (connectedWest ? 1 : 0);

			Vec3 northEntryPoint = new Vec3((regionX + northEdgeNoise) * REGION_BLOCK_SIZE, 90d, (regionZ) * REGION_BLOCK_SIZE);
			Vec3 southEntryPoint = new Vec3((regionX + southEdgeNoise) * REGION_BLOCK_SIZE, 90d, (regionZ + 1) * REGION_BLOCK_SIZE);
			Vec3 eastEntryPoint =  new Vec3((regionX + 1) * REGION_BLOCK_SIZE, 			    90d, (regionZ + eastEdgeNoise) * REGION_BLOCK_SIZE);
			Vec3 westEntryPoint =  new Vec3((regionX) * REGION_BLOCK_SIZE, 					90d, (regionZ + westEdgeNoise) * REGION_BLOCK_SIZE);

			double intersectionNoiseX = 0.5d + 0.1d * noise.getValue(-1000d * (regionX + 0.5d), 0, 1000d * (regionZ + 0.5d));
			double intersectionNoiseZ = 0.5d + 0.1d * noise.getValue(1000d * (regionX + 0.5d), 0, -1000d * (regionZ + 0.5d));
			Vec3 intersectionPoint = new Vec3((regionX + intersectionNoiseX) * REGION_BLOCK_SIZE, 90d, (regionZ + intersectionNoiseZ) * REGION_BLOCK_SIZE);
			List<RoadPath> roads = new ArrayList<>();

			Vec3 northEndPoint = intersectionPoint;
			Vec3 eastEndPoint = intersectionPoint;
			Vec3 southEndPoint = intersectionPoint;
			Vec3 westEndPoint = intersectionPoint;

			if(numConnected == 4)
			{
				boolean roundabout = noise.getValue(7771d * (regionX + 0.5d), 0, -7771d * (regionZ + 0.5d)) > 0d;
				if(roundabout)
				{
					northEndPoint = intersectionPoint.add(new Vec3(0d, 0d, -8d));
					eastEndPoint = intersectionPoint.add(new Vec3(8d, 0d, 0d));
					southEndPoint = intersectionPoint.add(new Vec3(0d, 0d, 8d));
					westEndPoint = intersectionPoint.add(new Vec3(-8d, 0d, 0d));

					roads.add(new RoadPath(List.of(
						northEndPoint,
						intersectionPoint.add(new Vec3(5d, 0d, -5d)),
						eastEndPoint,
						intersectionPoint.add(new Vec3(5d, 0d, 5d)),
						southEndPoint,
						intersectionPoint.add(new Vec3(-5d, 0d, 5d)),
						westEndPoint,
						intersectionPoint.add(new Vec3(-5d, 0d, -5d)),
						northEndPoint
					)));
				}
			}

			if(connectedNorth)
			{
				RoadPath path = new RoadPath(new ArrayList<>(List.of(northEntryPoint, northEntryPoint.add(0d, 0d, 8d), northEndPoint)));
				// Path is currently { enter, enter+8, intersect }

				double sample50 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.25d));
				path.Wiggle(1, sample50, WIGGLE_MAG_FIRST);
				// Path is now { enter, enter+8, <wiggle50>, intersect }

				double sample25 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.125d));
				path.Wiggle(1, sample25, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, intersect }

				double sample75 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.375d));
				path.Wiggle(3, sample75, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, <wiggle75>, intersect }

				roads.add(path);
			}
			if(connectedEast)
			{
				RoadPath path = new RoadPath(new ArrayList<>(List.of(eastEntryPoint, eastEntryPoint.add(-8d, 0d, 0d), eastEndPoint)));
				// Path is currently { enter, enter+8, intersect }

				double sample50 = noise.getValue(1000d * (regionX + 0.75d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(1, sample50, WIGGLE_MAG_FIRST);
				// Path is now { enter, enter+8, <wiggle50>, intersect }

				double sample25 = noise.getValue(1000d * (regionX + 0.875d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(1, sample25, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, intersect }

				double sample75 = noise.getValue(1000d * (regionX + 0.625d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(3, sample75, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, <wiggle75>, intersect }

				roads.add(path);
			}
			if(connectedSouth)
			{
				RoadPath path = new RoadPath(new ArrayList<>(List.of(southEntryPoint, southEntryPoint.add(0d, 0d, -8d), southEndPoint)));
				// Path is currently { enter, enter+8, intersect }

				double sample50 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.75d));
				path.Wiggle(1, sample50, WIGGLE_MAG_FIRST);
				// Path is now { enter, enter+8, <wiggle50>, intersect }

				double sample25 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.875d));
				path.Wiggle(1, sample25, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, intersect }

				double sample75 = noise.getValue(1000d * (regionX + 0.5d), 	0, 1000d * (regionZ + 0.625d));
				path.Wiggle(3, sample75, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, <wiggle75>, intersect }

				roads.add(path);
			}
			if(connectedWest)
			{
				RoadPath path = new RoadPath(new ArrayList<>(List.of(westEntryPoint, westEntryPoint.add(8d, 0d, 0d), westEndPoint)));
				// Path is currently { enter, enter+8, intersect }

				double sample50 = noise.getValue(1000d * (regionX + 0.25d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(1, sample50, WIGGLE_MAG_FIRST);
				// Path is now { enter, enter+8, <wiggle50>, intersect }

				double sample25 = noise.getValue(1000d * (regionX + 0.125d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(1, sample25, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, intersect }

				double sample75 = noise.getValue(1000d * (regionX + 0.375d), 	0, 1000d * (regionZ + 0.5d));
				path.Wiggle(3, sample75, WIGGLE_MAG_SECOND);
				// Path is now { enter, enter+8, <wiggle25>, <wiggle50>, <wiggle75>, intersect }

				roads.add(path);
			}

			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					BlockPos blockPos = chunkPos.getBlockAt(i, 31, k);
					Vec3 pos = blockPos.getCenter();

					double minDist = Double.MAX_VALUE;
					for (RoadPath road : roads)
					{
						double distToRoad = road.DistanceFromRoad(pos);
						if (distToRoad < minDist)
							minDist = distToRoad;
					}


					if(minDist < ROAD_OUTER_RADIUS)
					{
						if(minDist < ROAD_INNER_RADIUS)
							chunk.setBlockState(blockPos, config.roadOuterBlockState, false);
						else chunk.setBlockState(blockPos, config.roadInnerBlockState, false);

						int roofHeight = minDist < ROAD_INNER_RADIUS ? 6 : 5;

						for(int j = 1; j < roofHeight + 1; j++)
						{
							chunk.setBlockState(blockPos.above(j), Blocks.AIR.defaultBlockState(), false);
						}

					}


				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isStartChunk(@Nonnull RoadCarverConfiguration config, @Nonnull RandomSource random)
	{
		return true;
	}
}