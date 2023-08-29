package com.flansmod.packs.worldwars;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

	public WorldWarsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
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
	}
}
