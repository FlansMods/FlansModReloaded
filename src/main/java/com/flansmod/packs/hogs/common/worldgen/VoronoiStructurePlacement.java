package com.flansmod.packs.hogs.common.worldgen;

import com.flansmod.packs.hogs.WarHogsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class VoronoiStructurePlacement extends StructurePlacement
{
	public static final Codec<VoronoiStructurePlacement> CODEC =
		ExtraCodecs.validate(RecordCodecBuilder.mapCodec(
			(stream) -> {
				return placementCodec(stream)
					.apply(stream, VoronoiStructurePlacement::new);
			}),
			VoronoiStructurePlacement::validate).codec();

	@Nonnull
	private static DataResult<VoronoiStructurePlacement> validate(@Nonnull VoronoiStructurePlacement placement)
	{
		return DataResult.success(placement);
	}

	@Override
	@Nonnull
	public StructurePlacementType<?> type() { return WarHogsMod.VORONOI_PLACEMENT.get(); }

	public VoronoiStructurePlacement(@Nonnull Vec3i locateOffset,
									 @Nonnull StructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
									 float frequency,
									 int salt,
									 @Nonnull Optional<ExclusionZone> exclusionZone)
	{
		super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
	}

	@Override
	protected boolean isPlacementChunk(@Nonnull ChunkGeneratorStructureState state, int chunkX, int chunkZ)
	{


		return VoronoiUtil.ShouldPlaceChunk(state.getLevelSeed(), chunkX, chunkZ);
	}
}
