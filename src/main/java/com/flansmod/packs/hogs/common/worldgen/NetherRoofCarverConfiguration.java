package com.flansmod.packs.hogs.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;

public class NetherRoofCarverConfiguration extends CarverConfiguration
{
	public static final Codec<NetherRoofCarverConfiguration> CODEC = RecordCodecBuilder.create((builder) ->
		builder.group(
				CarverConfiguration.CODEC.forGetter((carverSuper) -> carverSuper),
				FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter((carver) -> carver.horizontalRadiusMultiplier),
				FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter((carver) -> carver.verticalRadiusMultiplier),
				FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter((carver) -> carver.floorLevel),
				NormalNoise.NoiseParameters.CODEC.fieldOf("noise_parameters").forGetter((carver) -> carver.noiseParameters))
			.apply(builder, NetherRoofCarverConfiguration::new));

	public final Holder<NormalNoise.NoiseParameters> noiseParameters;
	public final FloatProvider horizontalRadiusMultiplier;
	public final FloatProvider verticalRadiusMultiplier;
	public final FloatProvider floorLevel;

	public NetherRoofCarverConfiguration(float probability,
								   @Nonnull HeightProvider yProvider,
								   @Nonnull FloatProvider yScaleProvider,
								   @Nonnull VerticalAnchor lavaLevelAnchor,
								   @Nonnull CarverDebugSettings carverDebugSettings,
								   @Nonnull HolderSet<Block> canReplaceBlocks,
								   @Nonnull FloatProvider horizontalRadiusProvider,
								   @Nonnull FloatProvider verticalRadiusProvider,
								   @Nonnull FloatProvider floorLevelProvider,
								   @Nonnull Holder<NormalNoise.NoiseParameters> noiseParameterProvider) {
		super(probability, yProvider, yScaleProvider, lavaLevelAnchor, carverDebugSettings, canReplaceBlocks);
		this.horizontalRadiusMultiplier = horizontalRadiusProvider;
		this.verticalRadiusMultiplier = verticalRadiusProvider;
		this.floorLevel = floorLevelProvider;
		this.noiseParameters = noiseParameterProvider;
	}

	public NetherRoofCarverConfiguration(@Nonnull CarverConfiguration other,
								   @Nonnull FloatProvider horizontalRadiusProvider,
								   @Nonnull FloatProvider verticalRadiusProvider,
								   @Nonnull FloatProvider floorLevelProvider,
								   @Nonnull Holder<NormalNoise.NoiseParameters> noiseParameterProvider)
	{
		this(other.probability, other.y, other.yScale, other.lavaLevel, other.debugSettings, other.replaceable,
			horizontalRadiusProvider, verticalRadiusProvider, floorLevelProvider, noiseParameterProvider);
	}
}
