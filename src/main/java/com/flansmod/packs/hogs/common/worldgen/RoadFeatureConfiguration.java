package com.flansmod.packs.hogs.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RoadFeatureConfiguration implements FeatureConfiguration
{
	public static final Codec<RoadFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.intRange(0, 64)
				.fieldOf("size")
				.forGetter(config -> config.size)
		).apply(instance, RoadFeatureConfiguration::new);
	});

	public final int size;

	public RoadFeatureConfiguration(int size) {
		this.size = size;
	}
}
