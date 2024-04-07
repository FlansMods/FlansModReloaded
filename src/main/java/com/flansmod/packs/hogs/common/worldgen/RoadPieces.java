package com.flansmod.packs.hogs.common.worldgen;

import com.flansmod.packs.hogs.WarHogsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import javax.annotation.Nonnull;
import java.util.List;

public class RoadPieces
{
	public static void generateRoadNetwork(@Nonnull StructureTemplateManager mananger,
										   long seed,
										   @Nonnull BlockPos startingPos,
										   @Nonnull Rotation orientation,
										   @Nonnull List<RoadPieces.RoadPiece> outPieces,
										   @Nonnull RandomSource random)
	{
		//List<Direction8> adjacencies = VoronoiUtil.GetAdjacencies(seed, new VoronoiUtil.FractalPosition(startingPos));
//
		//switch(adjacencies.size())
		//{
		//	case 0: // Weird, this road is alone!
		//		outPieces.add(new RoadPieceBase(orientation.rotate(Direction.NORTH), 1, new BoundingBox(startingPos.getX(), startingPos.getY(), startingPos.getZ(), startingPos.getX() + 16, startingPos.getY() + 16, startingPos.getZ() + 16)));
//
		//}

		outPieces.add(new RoadPieceBase(orientation.rotate(Direction.NORTH), 1, new BoundingBox(startingPos.getX(), startingPos.getY(), startingPos.getZ(), startingPos.getX() + 16, startingPos.getY() + 16, startingPos.getZ() + 16)));
		//RoadPieces.MansionGrid woodlandmansionpieces$mansiongrid = new WoodlandMansionPieces.MansionGrid(p_229990_);
		//RoadPieces.MansionPiecePlacer woodlandmansionpieces$mansionpieceplacer = new WoodlandMansionPieces.MansionPiecePlacer(p_229986_, p_229990_);
		//woodlandmansionpieces$mansionpieceplacer.createMansion(p_229987_, p_229988_, p_229989_, woodlandmansionpieces$mansiongrid);
	}


	public static class RoadPieceType implements StructurePieceType
	{
		@Override
		@Nonnull
		public StructurePiece load(@Nonnull StructurePieceSerializationContext context, @Nonnull CompoundTag tags)
		{
			return new RoadPieceBase(tags);
		}
	}

	//public static class RoadPieceStraight extends RoadPiece
	//{
	//	public RoadPieceStraight(@Nonnull Direction direction, int size, @Nonnull BoundingBox bb)
	//	{
	//		super(WarHogsMod.ROAD_BASE_PIECE.get(), direction, size, bb);
	//	}
	//	public RoadPieceStraight(@Nonnull CompoundTag tags)
	//	{
	//		super(WarHogsMod.ROAD_BASE_PIECE.get(), tags);
	//	}
	//}

	public static class RoadPieceBase extends RoadPiece
	{
		public RoadPieceBase(@Nonnull Direction direction, int size, @Nonnull BoundingBox bb)
		{
			super(WarHogsMod.ROAD_BASE_PIECE.get(), direction, size, bb);
		}
		public RoadPieceBase(@Nonnull CompoundTag tags)
		{
			super(WarHogsMod.ROAD_BASE_PIECE.get(), tags);
		}
	}

	public static class RoadPiece extends StructurePiece
	{
		public RoadPiece(@Nonnull StructurePieceType type,
						 @Nonnull Direction direction,
						 int genDepth,
						 @Nonnull BoundingBox bb)
		{
			super(type, genDepth, bb);
			setOrientation(direction);
		}

		public RoadPiece(@Nonnull StructurePieceType type,
						 @Nonnull CompoundTag tags)
		{
			super(type, tags);
		}



		@Override
		protected void addAdditionalSaveData(
			@Nonnull StructurePieceSerializationContext context,
			@Nonnull CompoundTag tags)
		{

		}

		@Override
		public void postProcess(@Nonnull WorldGenLevel level,
								@Nonnull StructureManager manager,
								@Nonnull ChunkGenerator generator,
								@Nonnull RandomSource random,
								@Nonnull BoundingBox bb,
								@Nonnull ChunkPos chunkPos,
								@Nonnull BlockPos startingPos)
		{
			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					int x = bb.minX() + i;
					int z = bb.maxZ() - k;
					if(VoronoiUtil.ShouldPlaceBlock(level.getSeed(), x, z, 4))
					{
						generateBox(level, bb, i, 0, k, i+1, 16, k+1, Blocks.BASALT.defaultBlockState(), Blocks.BASALT.defaultBlockState(), false);

					}
				}
			}

		}
	}
}
