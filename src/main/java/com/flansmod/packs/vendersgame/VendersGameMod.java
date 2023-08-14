package com.flansmod.packs.vendersgame;

import com.flansmod.common.FlansMod;
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

@Mod(VendersGameMod.MODID)
public class VendersGameMod
{
	public static final String MODID = "flansvendersgame";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

	// Flint & Cobblesons Premium Guns
	public static final RegistryObject<Item> GUN_FC_700 = 						FlansMod.Gun(ITEMS, MODID, "fc_700");
	public static final RegistryObject<Item> GUN_FC_870 = 						FlansMod.Gun(ITEMS, MODID, "fc_870");
	public static final RegistryObject<Item> GUN_FC_HAWK = 						FlansMod.Gun(ITEMS, MODID, "fc_hawk");
	public static final RegistryObject<Item> GUN_FC_PUNCHER = 					FlansMod.Gun(ITEMS, MODID, "fc_puncher");

	// HitMarket Budget Weaponry
	public static final RegistryObject<Item> GUN_HM_9 = 						FlansMod.Gun(ITEMS, MODID, "hm_9");
	public static final RegistryObject<Item> GUN_HM_14 = 						FlansMod.Gun(ITEMS, MODID, "hm_14");
	public static final RegistryObject<Item> GUN_HM_16 = 						FlansMod.Gun(ITEMS, MODID, "hm_21");
	public static final RegistryObject<Item> GUN_HM_1200 = 						FlansMod.Gun(ITEMS, MODID, "hm_1200");
	public static final RegistryObject<Item> GUN_HM_COMBINE = 					FlansMod.Gun(ITEMS, MODID, "hm_combine");
	public static final RegistryObject<Item> GUN_HM_POCKET = 					FlansMod.Gun(ITEMS, MODID, "hm_pocket");

	// PhanTek Advanced Guns
	public static final RegistryObject<Item> GUN_PHANTEK_90 = 					FlansMod.Gun(ITEMS, MODID, "phantek_90");
	public static final RegistryObject<Item> GUN_PHANTEK_AUTO_12 = 				FlansMod.Gun(ITEMS, MODID, "phantek_auto_12");
	public static final RegistryObject<Item> GUN_PHANTEK_EYE = 					FlansMod.Gun(ITEMS, MODID, "phantek_eye");
	public static final RegistryObject<Item> GUN_PHANTEK_FURY = 				FlansMod.Gun(ITEMS, MODID, "phantek_fury");
	public static final RegistryObject<Item> GUN_PHANTEK_LOCK = 				FlansMod.Gun(ITEMS, MODID, "phantek_lock");
	public static final RegistryObject<Item> GUN_PHANTEK_SEEKER = 				FlansMod.Gun(ITEMS, MODID, "phantek_seeker");

	public VendersGameMod()
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
