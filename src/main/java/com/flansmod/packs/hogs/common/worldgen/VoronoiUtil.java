package com.flansmod.packs.hogs.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction8;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoronoiUtil
{
	public static final FractalLayer BlockLayer = new FractalLayer(0x3e3d3b3c);
	public static final FractalLayer ChunkLayer = new FractalLayer(BlockLayer, 0x5a115a11, 16);
	public static final FractalLayer UrbanLayer = new FractalLayer(ChunkLayer, 0x4ee15ef9, 4);
	public static final FractalLayer RegionalLayer = new FractalLayer(UrbanLayer, 0xdee099ed, 4);
	public static final Map<Long, PerlinNoise> PerlinOffsetLayers = new HashMap<>();

	public static boolean ShouldPlaceChunk(long seed, int chunkX, int chunkZ)
	{
		if(!PerlinOffsetLayers.containsKey(seed))
			PerlinOffsetLayers.put(seed, PerlinNoise.create(RandomSource.create(seed), List.of(1, 4, 5, 6, 9, 13)));
		double noiseX = PerlinOffsetLayers.get(seed).getValue(chunkX * 0.0513d, 0, chunkZ * 0.0513d);
		double noiseZ = PerlinOffsetLayers.get(seed).getValue(chunkX * 0.0517d + 1337, 0, chunkZ * 0.0517d + 1321);

		chunkX += Math.signum(noiseX) * Math.floor(Math.abs(noiseX) * 20);
		chunkZ += Math.signum(noiseZ) * Math.floor(Math.abs(noiseZ) * 20);

		return RegionalLayer.ShouldPlaceChunk(seed, chunkX, chunkZ);
	}

	public static boolean ShouldPlaceBlock(long seed, int blockX, int blockZ, int thickness)
	{
		return RegionalLayer.ShouldPlaceBlock(seed, blockX, blockZ, thickness);
	}

	@Nonnull
	public static List<Direction8> GetAdjacencies(long seed, @Nonnull FractalPosition pos)
	{
		return List.of();
	}

	private static class FractalLayer
	{
		@Nullable
		public final FractalLayer Child;
		public final int Salt;
		public final int LayerScale;
		public final int AbsoluteScale;

		public FractalLayer(int salt)
		{
			Child = null;
			LayerScale = 1;
			Salt = salt;
			AbsoluteScale = 1;
		}
		public FractalLayer(@Nonnull FractalLayer child, int salt, int scale)
		{
			Child = child;
			LayerScale = scale;
			Salt = salt;
			AbsoluteScale = LayerScale * child.AbsoluteScale;
		}

		public int FractalToBlockScale() { return AbsoluteScale; }
		public float BlockToFractalScale() { return 1.0f / AbsoluteScale; }
		public int FractalToBlock(int layerCoord) { return layerCoord * AbsoluteScale; }
		public int BlockToFractal(int blockCoord) { return Math.floorDiv(blockCoord, AbsoluteScale); }
		public int RelativeBlock(int blockCoord) { return Math.floorMod(blockCoord, AbsoluteScale); }

		public boolean ShouldPlaceBlock(long levelSeed, int blockX, int blockZ, int thickness)
		{
			int minX = blockX - thickness / 2;
			int maxX = minX + thickness;
			int minZ = blockZ - thickness / 2;
			int maxZ = minZ + thickness;

			FractalPosition nearest00 = GetClosestFocalPoint(levelSeed, new FractalPosition(minX, minZ));
			FractalPosition nearest01 = GetClosestFocalPoint(levelSeed, new FractalPosition(minX, maxZ));
			if(!nearest00.equals(nearest01))
				return true;
			FractalPosition nearest10 = GetClosestFocalPoint(levelSeed, new FractalPosition(maxX, minZ));
			if(!nearest00.equals(nearest10))
				return true;
			FractalPosition nearest11 = GetClosestFocalPoint(levelSeed, new FractalPosition(maxX, maxZ));
			return !nearest00.equals(nearest11);
		}

		public boolean ShouldPlaceChunk(long levelSeed, int chunkX, int chunkZ)
		{
			FractalPosition nearest00 = GetClosestFocalPoint(levelSeed, new FractalPosition(chunkX * 16, chunkZ * 16));
			FractalPosition nearest01 = GetClosestFocalPoint(levelSeed, new FractalPosition(chunkX * 16, (chunkZ + 1) * 16));
			if(!nearest00.equals(nearest01))
				return true;
			FractalPosition nearest10 = GetClosestFocalPoint(levelSeed, new FractalPosition((chunkX + 1) * 16, chunkZ * 16));
			if(!nearest00.equals(nearest10))
				return true;
			FractalPosition nearest11 = GetClosestFocalPoint(levelSeed, new FractalPosition((chunkX + 1) * 16, (chunkZ + 1) * 16));
			return !nearest00.equals(nearest11);
		}

		@Nonnull
		public FractalPosition GetFocalPoint(long levelSeed, @Nonnull FractalPosition pos)
		{
			int cellX = BlockToFractal(pos.BlockX);
			int cellZ = BlockToFractal(pos.BlockZ);

			WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
			random.setLargeFeatureWithSalt(levelSeed, cellX, cellZ, Salt);

			FractalPosition focalPoint = new FractalPosition(
				cellX * AbsoluteScale + random.nextInt(AbsoluteScale),
				cellZ * AbsoluteScale + random.nextInt(AbsoluteScale));

			if(Child != null)
			{
				return Child.GetFocalPoint(levelSeed, focalPoint);
			}
			return focalPoint;
		}

		@Nonnull
		public FractalPosition GetClosestFocalPoint(long levelSeed, @Nonnull FractalPosition pos)
		{
			int cellX = BlockToFractal(pos.BlockX);
			int cellZ = BlockToFractal(pos.BlockZ);

			// Check all the 9 cells -1 to +1 and find which is closest
			FractalPosition closest = null;
			float closestDistSq = Float.MAX_VALUE;


			for(int i = -1; i <= 1; i++)
			{
				for(int k = -1; k <= 1; k++)
				{
					// These focal points are parametric within the neighbour cell
					FractalPosition samplePointInNeighbour = new FractalPosition((cellX + i) * AbsoluteScale, (cellZ + k) * AbsoluteScale);
					FractalPosition focalPoint = GetFocalPoint(levelSeed, samplePointInNeighbour);
					float dx = (focalPoint.BlockX - pos.BlockX);
					float dz = (focalPoint.BlockZ - pos.BlockZ);
					float distSq = dx * dx + dz * dz;
					if(distSq < closestDistSq)
					{
						closestDistSq = distSq;
						closest = focalPoint;
					}
				}
			}
			return closest != null ? closest : pos;
		}
	}

	public static class FractalPosition
	{
		public final int BlockX;
		public final int BlockZ;

		public FractalPosition(int blockX, int blockZ)
		{
			BlockX = blockX;
			BlockZ = blockZ;
		}

		public FractalPosition(@Nonnull ChunkPos chunkPos)
		{
			BlockX = chunkPos.getMiddleBlockX();
			BlockZ = chunkPos.getMiddleBlockZ();
		}

		public FractalPosition(@Nonnull BlockPos blockPos)
		{
			BlockX = blockPos.getX();
			BlockZ = blockPos.getZ();
		}

		@Override
		public boolean equals(Object other)
		{
			if(other instanceof FractalPosition otherCell)
			{
				return otherCell.BlockX == BlockX && otherCell.BlockZ == BlockZ;
			}
			return false;
		}
	}
}
