package com.flansmod.packs.vendersgame;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.packs.vendersgame.client.VenderModel;
import com.flansmod.packs.vendersgame.client.VenderRenderer;
import com.flansmod.packs.vendersgame.common.VenderEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(VendersGameMod.MODID)
public class VendersGameMod
{
	public static final String MODID = "flansvendersgame";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

	// Flint & Cobblesons Premium Guns
	public static final RegistryObject<Item> GUN_FC_700 = 						FlansMod.Gun(ITEMS, MODID, "fc_700");
	public static final RegistryObject<Item> GUN_FC_870 = 						FlansMod.Gun(ITEMS, MODID, "fc_870");
	public static final RegistryObject<Item> GUN_FC_HAWK = 						FlansMod.Gun(ITEMS, MODID, "fc_hawk");
	public static final RegistryObject<Item> GUN_FC_PUNCHER = 					FlansMod.Gun(ITEMS, MODID, "fc_puncher");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_FC = 				FlansMod.Workbench_Block(BLOCKS, MODID, "fc_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_FC = 				FlansMod.Workbench_Item(ITEMS, MODID, "fc_workbench", WORKBENCH_BLOCK_FC);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_FC =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "fc_workbench");


	// HitMarket Budget Weaponry
	public static final RegistryObject<Item> GUN_HM_9 = 						FlansMod.Gun(ITEMS, MODID, "hm_9");
	public static final RegistryObject<Item> GUN_HM_14 = 						FlansMod.Gun(ITEMS, MODID, "hm_14");
	public static final RegistryObject<Item> GUN_HM_16 = 						FlansMod.Gun(ITEMS, MODID, "hm_21");
	public static final RegistryObject<Item> GUN_HM_1200 = 						FlansMod.Gun(ITEMS, MODID, "hm_1200");
	public static final RegistryObject<Item> GUN_HM_COMBINE = 					FlansMod.Gun(ITEMS, MODID, "hm_combine");
	public static final RegistryObject<Item> GUN_HM_POCKET = 					FlansMod.Gun(ITEMS, MODID, "hm_pocket");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_HM = 				FlansMod.Workbench_Block(BLOCKS, MODID, "hm_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_HM = 				FlansMod.Workbench_Item(ITEMS, MODID, "hm_workbench", WORKBENCH_BLOCK_HM);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_HM =
																				FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "hm_workbench");

	// PhanTek Advanced Guns
	public static final RegistryObject<Item> GUN_PHANTEK_90 = 					FlansMod.Gun(ITEMS, MODID, "phantek_90");
	public static final RegistryObject<Item> GUN_PHANTEK_AUTO_12 = 				FlansMod.Gun(ITEMS, MODID, "phantek_auto_12");
	public static final RegistryObject<Item> GUN_PHANTEK_EYE = 					FlansMod.Gun(ITEMS, MODID, "phantek_eye");
	public static final RegistryObject<Item> GUN_PHANTEK_FURY = 				FlansMod.Gun(ITEMS, MODID, "phantek_fury");
	public static final RegistryObject<Item> GUN_PHANTEK_LOCK = 				FlansMod.Gun(ITEMS, MODID, "phantek_lock");
	public static final RegistryObject<Item> GUN_PHANTEK_SEEKER = 				FlansMod.Gun(ITEMS, MODID, "phantek_seeker");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_PHANTEK = 		FlansMod.Workbench_Block(BLOCKS, MODID, "phantek_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_PHANTEK = 			FlansMod.Workbench_Item(ITEMS, MODID, "phantek_workbench", WORKBENCH_BLOCK_PHANTEK);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_PHANTEK =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "phantek_workbench");


	// Vender NPC
	public static final RegistryObject<EntityType<VenderEntity>> ENTITY_TYPE_VENDER = ENTITY_TYPES.register(
		"vender",
		() -> EntityType.Builder.of(
				VenderEntity::new,
				MobCategory.CREATURE)
			.sized(0.8f, 1.65f)
			.build("vender"));


	public VendersGameMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		ENTITY_TYPES.register(modEventBus);
		modEventBus.register(this);
	}

	@SubscribeEvent
	public void SupplyAttributes(EntityAttributeCreationEvent event)
	{
		event.put(ENTITY_TYPE_VENDER.get(), VenderEntity.createAttributes().build());
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		@SubscribeEvent
		public static void RegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(VenderModel.MODEL_LAYER_LOCATION, VenderModel::createBodyLayer);
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
			event.registerEntityRenderer(ENTITY_TYPE_VENDER.get(), VenderRenderer::new);
		}
	}
}
