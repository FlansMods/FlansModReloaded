package com.flansmod.packs.worldwars;

import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.vehicles.VehicleRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.packs.worldwars.client.JanModel;
import com.flansmod.packs.worldwars.client.JanRenderer;
import com.flansmod.packs.worldwars.common.JanEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(WorldWarsMod.MODID)
public class WorldWarsMod
{
	public static final String MODID = "flansworldwars";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

	public static final RegistryObject<Item> GUN_BAR = 								FlansMod.Gun(ITEMS, MODID, "bar");
	public static final RegistryObject<Item> GUN_BAZOOKA = 							FlansMod.Gun(ITEMS, MODID, "bazooka");
	public static final RegistryObject<Item> GUN_BREN = 							FlansMod.Gun(ITEMS, MODID, "bren");
	public static final RegistryObject<Item> GUN_BROWNING = 						FlansMod.Gun(ITEMS, MODID, "browning");
	public static final RegistryObject<Item> GUN_COLT = 							FlansMod.Gun(ITEMS, MODID, "colt");
	public static final RegistryObject<Item> GUN_DP28 = 							FlansMod.Gun(ITEMS, MODID, "dp28");
	public static final RegistryObject<Item> GUN_FG42 = 							FlansMod.Gun(ITEMS, MODID, "fg42");
	public static final RegistryObject<Item> GUN_FLAMETHROWER = 					FlansMod.Gun(ITEMS, MODID, "flamethrower");
	public static final RegistryObject<Item> GUN_G43 = 								FlansMod.Gun(ITEMS, MODID, "g43");
	public static final RegistryObject<Item> GUN_KAR98K = 							FlansMod.Gun(ITEMS, MODID, "kar98k");
	public static final RegistryObject<Item> GUN_KAR98K_SNIPER = 					FlansMod.Gun(ITEMS, MODID, "kar98k_sniper");
	public static final RegistryObject<Item> GUN_LEE_ENFIELD = 						FlansMod.Gun(ITEMS, MODID, "lee_enfield");
	public static final RegistryObject<Item> GUN_LEE_ENFIELD_SNIPER = 				FlansMod.Gun(ITEMS, MODID, "lee_enfield_sniper");
	public static final RegistryObject<Item> GUN_LUGER = 							FlansMod.Gun(ITEMS, MODID, "luger");
	public static final RegistryObject<Item> GUN_M1_CARBINE = 						FlansMod.Gun(ITEMS, MODID, "m1_carbine");
	public static final RegistryObject<Item> GUN_M1_GARAND = 						FlansMod.Gun(ITEMS, MODID, "m1_garand");
	public static final RegistryObject<Item> GUN_M3A1 = 							FlansMod.Gun(ITEMS, MODID, "m3a1");
	public static final RegistryObject<Item> GUN_MG42 = 							FlansMod.Gun(ITEMS, MODID, "mg42");
	public static final RegistryObject<Item> GUN_MP40 = 							FlansMod.Gun(ITEMS, MODID, "mp40");
	public static final RegistryObject<Item> GUN_MP44 = 							FlansMod.Gun(ITEMS, MODID, "mp44");
	public static final RegistryObject<Item> GUN_NAGANT = 							FlansMod.Gun(ITEMS, MODID, "nagant");
	public static final RegistryObject<Item> GUN_NAGANT_SNIPER = 					FlansMod.Gun(ITEMS, MODID, "nagant_sniper");
	public static final RegistryObject<Item> GUN_PANZERFAUST = 						FlansMod.Gun(ITEMS, MODID, "panzerfaust");
	public static final RegistryObject<Item> GUN_PANZERSCHRECK = 					FlansMod.Gun(ITEMS, MODID, "panzerschreck");
	public static final RegistryObject<Item> GUN_PIAT = 							FlansMod.Gun(ITEMS, MODID, "piat");
	public static final RegistryObject<Item> GUN_PPSH = 							FlansMod.Gun(ITEMS, MODID, "ppsh");
	public static final RegistryObject<Item> GUN_SPRINGFIELD = 						FlansMod.Gun(ITEMS, MODID, "springfield");
	public static final RegistryObject<Item> GUN_STEN = 							FlansMod.Gun(ITEMS, MODID, "sten");
	public static final RegistryObject<Item> GUN_THOMPSON = 						FlansMod.Gun(ITEMS, MODID, "thompson");
	public static final RegistryObject<Item> GUN_TRENCHGUN = 						FlansMod.Gun(ITEMS, MODID, "trenchgun");
	public static final RegistryObject<Item> GUN_TT33 = 							FlansMod.Gun(ITEMS, MODID, "tt33");
	public static final RegistryObject<Item> GUN_TYPE14 = 							FlansMod.Gun(ITEMS, MODID, "type14");
	public static final RegistryObject<Item> GUN_TYPE38 = 							FlansMod.Gun(ITEMS, MODID, "type38");
	public static final RegistryObject<Item> GUN_TYPE38_SNIPER = 					FlansMod.Gun(ITEMS, MODID, "type38_sniper");
	public static final RegistryObject<Item> GUN_TYPE99 = 							FlansMod.Gun(ITEMS, MODID, "type99");
	public static final RegistryObject<Item> GUN_TYPE100 = 							FlansMod.Gun(ITEMS, MODID, "type100");
	public static final RegistryObject<Item> GUN_WEBLEY = 							FlansMod.Gun(ITEMS, MODID, "webley");

	public static final RegistryObject<Item> GUN_BINOCULARS = 						FlansMod.Gun(ITEMS, MODID, "binoculars");
	public static final RegistryObject<Item> GUN_KNIFE = 							FlansMod.Gun(ITEMS, MODID, "knife");

	public static final RegistryObject<Item> VEHICLE_ITEM_JEEP = 					FlansMod.Vehicle_Item(ITEMS, MODID, "jeep");
	public static final RegistryObject<EntityType<VehicleEntity>> VEHICLE_ENTITY_JEEP = FlansMod.Vehicle_Entity(ENTITY_TYPES, MODID, "jeep", true);


	// British
	public static final RegistryObject<Block> WORKBENCH_BLOCK_BRITISH = 			FlansMod.Workbench_Block(BLOCKS, MODID, "british_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_BRITISH = 				FlansMod.Workbench_Item(ITEMS, MODID, "british_workbench", WORKBENCH_BLOCK_BRITISH);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_BRITISH =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "british_workbench");


	// American
	public static final RegistryObject<Block> WORKBENCH_BLOCK_AMERICAN = 			FlansMod.Workbench_Block(BLOCKS, MODID, "american_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_AMERICAN = 				FlansMod.Workbench_Item(ITEMS, MODID, "american_workbench", WORKBENCH_BLOCK_AMERICAN);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_AMERICAN =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "american_workbench");


	// Russian
	public static final RegistryObject<Block> WORKBENCH_BLOCK_RUSSIAN = 			FlansMod.Workbench_Block(BLOCKS, MODID, "russian_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_RUSSIAN = 				FlansMod.Workbench_Item(ITEMS, MODID, "russian_workbench", WORKBENCH_BLOCK_RUSSIAN);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_RUSSIAN =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "russian_workbench");


	// German
	public static final RegistryObject<Block> WORKBENCH_BLOCK_GERMAN = 				FlansMod.Workbench_Block(BLOCKS, MODID, "german_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_GERMAN = 				FlansMod.Workbench_Item(ITEMS, MODID, "german_workbench", WORKBENCH_BLOCK_GERMAN);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_GERMAN =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "german_workbench");


	// Japanese
	public static final RegistryObject<Block> WORKBENCH_BLOCK_JAPANESE = 			FlansMod.Workbench_Block(BLOCKS, MODID, "japanese_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_JAPANESE = 				FlansMod.Workbench_Item(ITEMS, MODID, "japanese_workbench", WORKBENCH_BLOCK_JAPANESE);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_JAPANESE =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "japanese_workbench");

	// Jan NPC
	public static final RegistryObject<EntityType<JanEntity>> ENTITY_TYPE_JAN = ENTITY_TYPES.register(
		"jan",
		() -> EntityType.Builder.of(
				JanEntity::new,
				MobCategory.CREATURE)
			.sized(0.6f, 2.6f)
			.build("jan"));


	public WorldWarsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		ENTITY_TYPES.register(modEventBus);
		modEventBus.register(this);
	}

	@SubscribeEvent
	public void OnCreativeTabs(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTab().getDisplayName() == FlansMod.CREATIVE_TAB_NAME_GUNS)
		{
			event.accept(WORKBENCH_ITEM_RUSSIAN);
			event.accept(WORKBENCH_ITEM_AMERICAN);
			event.accept(WORKBENCH_ITEM_BRITISH);
			event.accept(WORKBENCH_ITEM_GERMAN);
			event.accept(WORKBENCH_ITEM_JAPANESE);
		}
	}

	@SubscribeEvent
	public void SupplyAttributes(EntityAttributeCreationEvent event)
	{
		event.put(ENTITY_TYPE_JAN.get(), JanEntity.createAttributes().build());
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		static
		{
			FlansModelRegistry.PreRegisterModel(new ResourceLocation(MODID, "jeep"));
		}

		@SubscribeEvent
		public static void ClientInit(final FMLClientSetupEvent event)
		{
			EntityRenderers.register(VEHICLE_ENTITY_JEEP.get(), (ctx) -> new VehicleRenderer(VEHICLE_ENTITY_JEEP.getId(), ctx));
		}

		@SubscribeEvent
		public static void RegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(JanModel.MODEL_LAYER_LOCATION, JanModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for (var entry : ITEMS.getEntries())
			{
				event.register(new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
				shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
			}

		}

		@SubscribeEvent
		public static void EntityRenderEvent(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerEntityRenderer(ENTITY_TYPE_JAN.get(), JanRenderer::new);
			//event.registerEntityRenderer((EntityType<? extends VehicleEntity>)VEHICLE_ENTITY_JEEP.get(), VehicleRenderer::new);
		}
	}
}
