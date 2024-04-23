package com.flansmod.packs.hogs.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoadCarverConfiguration extends CarverConfiguration
{
	public static final Codec<RoadCarverConfiguration> CODEC = RecordCodecBuilder.create((builder) ->
		builder.group(
			CarverConfiguration.CODEC.forGetter((carverSuper) -> carverSuper),

			FloatProvider.CODEC.fieldOf("wiggle_magnitude_first").forGetter((carver) -> carver.wiggleMagnitudeFirstOrder),
			FloatProvider.CODEC.fieldOf("wiggle_magnitude_second").forGetter((carver) -> carver.wiggleMagnitudeSecondOrder),

			Codec.floatRange(0f, 256f).fieldOf("road_inner_radius").forGetter((carver) -> carver.roadInnerRadius),
			BlockState.CODEC.fieldOf("road_inner_block_state").forGetter((carver) -> carver.roadInnerBlockState),

			Codec.floatRange(0f, 256f).fieldOf("road_outer_radius").forGetter((carver) -> carver.roadOuterRadius),
			BlockState.CODEC.fieldOf("road_outer_block_state").forGetter((carver) -> carver.roadOuterBlockState),

			FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter((carver) -> carver.floorLevel),
			NormalNoise.NoiseParameters.CODEC.fieldOf("noise_parameters").forGetter((carver) -> carver.noiseParameters))
		.apply(builder, RoadCarverConfiguration::new));

	public final Holder<NormalNoise.NoiseParameters> noiseParameters;

	public final FloatProvider floorLevel;

	public final FloatProvider wiggleMagnitudeFirstOrder;
	public final FloatProvider wiggleMagnitudeSecondOrder;

	public final float roadInnerRadius;
	public final BlockState roadInnerBlockState;
	public final float roadOuterRadius;
	public final BlockState roadOuterBlockState;

	public RoadCarverConfiguration(float probability,
								   @Nonnull HeightProvider yProvider,
								   @Nonnull FloatProvider yScaleProvider,
								   @Nonnull VerticalAnchor lavaLevelAnchor,
								   @Nonnull CarverDebugSettings carverDebugSettings,
								   @Nonnull HolderSet<Block> canReplaceBlocks,

								   @Nonnull FloatProvider wiggleMagnitudeFirstOrder,
								   @Nonnull FloatProvider wiggleMagnitudeSecondOrder,

								   float roadInnerRadius,
								   @Nonnull BlockState roadInnerBlockState,

								   float roadOuterRadius,
								   @Nonnull BlockState roadOuterBlockState,

								   @Nonnull FloatProvider floorLevelProvider,
								   @Nonnull Holder<NormalNoise.NoiseParameters> noiseParameterProvider) {
		super(probability, yProvider, yScaleProvider, lavaLevelAnchor, carverDebugSettings, canReplaceBlocks);

		this.wiggleMagnitudeFirstOrder = wiggleMagnitudeFirstOrder;
		this.wiggleMagnitudeSecondOrder = wiggleMagnitudeSecondOrder;

		this.roadInnerRadius = roadInnerRadius;
		this.roadInnerBlockState = roadInnerBlockState;
		this.roadOuterRadius = roadOuterRadius;
		this.roadOuterBlockState = roadOuterBlockState;

		this.floorLevel = floorLevelProvider;
		this.noiseParameters = noiseParameterProvider;
	}

	public RoadCarverConfiguration(@Nonnull CarverConfiguration other,

								   @Nonnull FloatProvider wiggleMagnitudeFirstOrder,
								   @Nonnull FloatProvider wiggleMagnitudeSecondOrder,

								   float roadInnerRadius,
								   @Nonnull BlockState roadInnerBlockState,

								   float roadOuterRadius,
								   @Nonnull BlockState roadOuterBlockState,

								   @Nonnull FloatProvider floorLevelProvider,
								   @Nonnull Holder<NormalNoise.NoiseParameters> noiseParameterProvider)
	{
		this(other.probability, other.y, other.yScale, other.lavaLevel, other.debugSettings, other.replaceable,
			wiggleMagnitudeFirstOrder,
			wiggleMagnitudeSecondOrder,
			roadInnerRadius,
			roadInnerBlockState,
			roadOuterRadius,
			roadOuterBlockState,
			floorLevelProvider,
			noiseParameterProvider);
	}
}
