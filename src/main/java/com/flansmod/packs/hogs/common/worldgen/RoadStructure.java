package com.flansmod.packs.hogs.common.worldgen;

import com.flansmod.packs.hogs.WarHogsMod;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class RoadStructure extends Structure
{
	public static final Codec<RoadStructure> CODEC = simpleCodec(RoadStructure::new);

	@Nonnull
	@Override
	public StructureType<?> type() {
		return WarHogsMod.ROAD_NETWORK_STRUCTURE.get();
	}

	public RoadStructure(@Nonnull Structure.StructureSettings settings) {
		super(settings);
	}

	@Nonnull
	public Optional<GenerationStub> findGenerationPoint(@Nonnull Structure.GenerationContext context)
	{
		Rotation rotation = Rotation.NONE; //Rotation.getRandom(context.random());
		BlockPos blockpos = context.chunkPos().getBlockAt(0, 60, 0);

		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, (builder) -> {
			generatePieces(builder, context, blockpos, rotation);
		});
	}

	private void generatePieces(@Nonnull StructurePiecesBuilder builder,
								@Nonnull Structure.GenerationContext context,
								@Nonnull BlockPos startPos,
								@Nonnull Rotation orientation)
	{
		List<RoadPieces.RoadPiece> list = Lists.newLinkedList();
		RoadPieces.generateRoadNetwork(context.structureTemplateManager(), context.seed(), startPos, orientation, list, context.random());
		list.forEach(builder::addPiece);
	}

	public void afterPlace(@Nonnull WorldGenLevel level,
						   @Nonnull StructureManager manager,
						   @Nonnull ChunkGenerator chunkGen,
						   @Nonnull RandomSource random,
						   @Nonnull BoundingBox bb,
						   @Nonnull ChunkPos chunkPos,
						   @Nonnull PiecesContainer piecesContainer)
	{
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		int i = level.getMinBuildHeight();
		BoundingBox boundingbox = piecesContainer.calculateBoundingBox();
		int j = boundingbox.minY();

		for(int k = bb.minX(); k <= bb.maxX(); ++k) {
			for(int l = bb.minZ(); l <= bb.maxZ(); ++l) {
				blockpos$mutableblockpos.set(k, j, l);
				if (!level.isEmptyBlock(blockpos$mutableblockpos) && boundingbox.isInside(blockpos$mutableblockpos) && piecesContainer.isInsidePiece(blockpos$mutableblockpos)) {
					for(int i1 = j - 1; i1 > i; --i1) {
						blockpos$mutableblockpos.setY(i1);
						if (!level.isEmptyBlock(blockpos$mutableblockpos) && !level.getBlockState(blockpos$mutableblockpos).liquid()) {
							break;
						}

						level.setBlock(blockpos$mutableblockpos, Blocks.COBBLESTONE.defaultBlockState(), 2);
					}
				}
			}
		}

	}
}
