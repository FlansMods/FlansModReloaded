package com.flansmod.packs.hogs.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import javax.annotation.Nonnull;

public class RoadFeature extends Feature<RoadFeatureConfiguration>
{

	public RoadFeature()
	{
		super(RoadFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(@Nonnull FeaturePlaceContext<RoadFeatureConfiguration> context)
	{
		BlockPos origin = context.origin();
		WorldGenLevel level = context.level();
		RoadFeatureConfiguration config = context.config();

		int radius = config.size;

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(level);

		for(int dz = -radius; dz <= radius; dz++)
		{
			for(int dy = -radius; dy <= radius; dy++)
			{
				for(int dx = -radius; dx <= radius; dx++)
				{
					mutablePos.set(origin.offset(dx, dy, dz));
					if(!level.ensureCanWrite(mutablePos))
						continue;
					LevelChunkSection levelChunkSection = bulkSectionAccess.getSection(mutablePos);
					if (levelChunkSection == null)
						continue;

					int localX = SectionPos.sectionRelative(mutablePos.getX());
					int localY = SectionPos.sectionRelative(mutablePos.getY());
					int localZ = SectionPos.sectionRelative(mutablePos.getZ());

					levelChunkSection.setBlockState(localX, localY, localZ, Blocks.IRON_BLOCK.defaultBlockState(), false);

				}
			}
		}

		bulkSectionAccess.close();

		return true;

	}
}
