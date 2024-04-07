package com.flansmod.packs.hogs;

import com.flansmod.packs.hogs.common.worldgen.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

@Mod(WarHogsMod.MODID)
public class WarHogsMod
{
	public static final String MODID = "flanswarhogs";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

	// Carver Implementation
	public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(Registries.CARVER, MODID);
	//public static final DeferredRegister<ConfiguredWorldCarver<?>> CONFIGURED_CARVERS = DeferredRegister.create(Registries.CONFIGURED_CARVER, MODID);
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);

	public static final RegistryObject<WorldCarver<?>> ROAD_CARVER = CARVERS.register("road_carver", () -> new RoadCarver(RoadCarverConfiguration.CODEC));
	public static final RegistryObject<Codec<? extends BiomeModifier>> ADD_CARVER_BIOME_MODIFIER_SERIALIZER = BIOME_MODIFIER_SERIALIZERS.register("add_road_carver", () -> AddRoadCarverBiomeModifier.CODEC);





	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
	public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, MODID);

	public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
	public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
	public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MODID);
	public static final DeferredRegister<Structure> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE, MODID);


	//public static final RegistryObject<RoadFeature> ROAD = FEATURES.register("road", RoadFeature::new);
	//public static final RegistryObject<ConfiguredFeature<>> CONFIGURED_ROAD =
	// What...? Thanks Grunt
	// https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.20.2-Neoforge-Jigsaw/src/main/java/com/telepathicgrunt/structuretutorial/STStructures.java
	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(@Nonnull Codec<T> structureCodec) {
		return () -> structureCodec;
	}
	public static final RegistryObject<StructureType<?>> ROAD_NETWORK_STRUCTURE = STRUCTURE_TYPES.register(
		"road_network",
		() -> explicitStructureTypeTyping(RoadStructure.CODEC)
	);

	public static final RegistryObject<StructurePieceType> ROAD_BASE_PIECE = STRUCTURE_PIECE_TYPES.register(
		"road_base", RoadPieces.RoadPieceType::new);

	private static <T extends StructurePlacement> StructurePlacementType<T> explicifier() {
		return () -> (Codec<T>) VoronoiStructurePlacement.CODEC;
	}
	public static final RegistryObject<StructurePlacementType<?>> VORONOI_PLACEMENT = STRUCTURE_PLACEMENT_TYPES.register(
		"voronoi",
		() -> explicifier()
	);

	//public static final RegistryObject<Structure> ROAD_STRUCTURE = STRUCTURES.register("road", () ->
	//{
	//	return new RoadStructure((Structure.StructureSettings)null);
	//	//	new Structure.StructureSettings(new HashSet<Biome>(),
	//	//		Map.of(),
	//	//		)
	//	//		//Registries.BIOME.
	//	//	)
	//	//	//structure(holdergetter.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION), TerrainAdjustment.NONE)));
	//});

	public WarHogsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		FEATURES.register(modEventBus);
		CONFIGURED_FEATURES.register(modEventBus);
		STRUCTURE_TYPES.register(modEventBus);
		STRUCTURE_PIECE_TYPES.register(modEventBus);
		STRUCTURE_PLACEMENT_TYPES.register(modEventBus);
		STRUCTURES.register(modEventBus);
		CARVERS.register(modEventBus);
		BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
		modEventBus.register(this);
	}


}
