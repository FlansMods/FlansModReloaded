package com.flansmod.packs.hogs.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public record AddRoadCarverBiomeModifier(@Nonnull HolderSet<Biome> MatchBiomes, @Nonnull RoadCarverConfiguration RoadCarverConfig, @Nonnull WorldCarver<?> CarverType) implements BiomeModifier
{
	public static Codec<AddRoadCarverBiomeModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddRoadCarverBiomeModifier::MatchBiomes),
			RoadCarverConfiguration.CODEC.fieldOf("carver_config").forGetter(AddRoadCarverBiomeModifier::RoadCarverConfig),
			ForgeRegistries.WORLD_CARVERS.getCodec().fieldOf("carver_type").forGetter(AddRoadCarverBiomeModifier::CarverType))
		.apply(builder, AddRoadCarverBiomeModifier::new));

	@Override
	public void modify(@Nonnull Holder<Biome> biome, @Nonnull Phase phase, @Nonnull ModifiableBiomeInfo.BiomeInfo.Builder builder)
	{
		if(MatchBiomes.contains(biome) && phase == Phase.BEFORE_EVERYTHING)
		{
			ConfiguredWorldCarver<?> configured = new ConfiguredWorldCarver(CarverType, RoadCarverConfig);

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
