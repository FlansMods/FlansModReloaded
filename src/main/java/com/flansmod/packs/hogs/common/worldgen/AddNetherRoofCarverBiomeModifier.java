package com.flansmod.packs.hogs.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public record AddNetherRoofCarverBiomeModifier(@Nonnull HolderSet<Biome> MatchBiomes, @Nonnull NetherRoofCarverConfiguration RoofCarverConfig, @Nonnull WorldCarver<?> CarverType) implements BiomeModifier
{
	public static Codec<AddNetherRoofCarverBiomeModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddNetherRoofCarverBiomeModifier::MatchBiomes),
			NetherRoofCarverConfiguration.CODEC.fieldOf("carver_config").forGetter(AddNetherRoofCarverBiomeModifier::RoofCarverConfig),
			ForgeRegistries.WORLD_CARVERS.getCodec().fieldOf("carver_type").forGetter(AddNetherRoofCarverBiomeModifier::CarverType))
		.apply(builder, AddNetherRoofCarverBiomeModifier::new));

	@Override
	public void modify(@Nonnull Holder<Biome> biome, @Nonnull Phase phase, @Nonnull ModifiableBiomeInfo.BiomeInfo.Builder builder)
	{
		if(MatchBiomes.contains(biome) && phase == Phase.BEFORE_EVERYTHING)
		{
			ConfiguredWorldCarver<?> configured = new ConfiguredWorldCarver(CarverType, RoofCarverConfig);

			builder.getGenerationSettings().addCarver(GenerationStep.Carving.AIR, Holder.direct(configured));
		}
	}

	@Override
	@Nonnull
	public Codec<? extends BiomeModifier> codec()
	{
		return null;
	}
}
