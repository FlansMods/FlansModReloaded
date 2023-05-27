package com.flansmod.packs.modern;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ModernWeaponsMod.MODID)
public class ModernWeaponsMod
{
	public static final String MODID = "flansmodernweapons";
	private static final Logger LOGGER = LogUtils.getLogger();
	
	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


	public static final RegistryObject<Item> GUN_A91 = 						FlansMod.Gun(ITEMS, MODID, "a91");
	public static final RegistryObject<Item> GUN_ACR = 						FlansMod.Gun(ITEMS, MODID, "acr");
	public static final RegistryObject<Item> GUN_AK47 = 					FlansMod.Gun(ITEMS, MODID, "ak47");
	public static final RegistryObject<Item> GUN_AK74 = 					FlansMod.Gun(ITEMS, MODID, "ak74");
	public static final RegistryObject<Item> GUN_AT4 = 						FlansMod.Gun(ITEMS, MODID, "at4");
	public static final RegistryObject<Item> GUN_AUG = 						FlansMod.Gun(ITEMS, MODID, "aug");
	public static final RegistryObject<Item> GUN_BARRETT = 					FlansMod.Gun(ITEMS, MODID, "barrett");
	public static final RegistryObject<Item> GUN_BIZON = 					FlansMod.Gun(ITEMS, MODID, "bizon");
	public static final RegistryObject<Item> GUN_DESERT_EAGLE = 			FlansMod.Gun(ITEMS, MODID, "desert_eagle");
	public static final RegistryObject<Item> GUN_DRAGUNOV = 				FlansMod.Gun(ITEMS, MODID, "dragunov");
	public static final RegistryObject<Item> GUN_FNSCAR = 					FlansMod.Gun(ITEMS, MODID, "fnscar");
	public static final RegistryObject<Item> GUN_G3 = 						FlansMod.Gun(ITEMS, MODID, "g3");
	public static final RegistryObject<Item> GUN_G36 = 						FlansMod.Gun(ITEMS, MODID, "g36");
	public static final RegistryObject<Item> GUN_GL1 = 						FlansMod.Gun(ITEMS, MODID, "gl1");
	public static final RegistryObject<Item> GUN_GL6 = 						FlansMod.Gun(ITEMS, MODID, "gl6");
	public static final RegistryObject<Item> GUN_GLOCK = 					FlansMod.Gun(ITEMS, MODID, "glock");
	//public static final RegistryObject<Item> GUN_GOLD_DESERT_EAGLE = 		FlansMod.Gun(ITEMS, MODID, "");
	//public static final RegistryObject<Item> GUN_HYDRA70 = 				FlansMod.Gun(ITEMS, MODID, "hydra70");
	public static final RegistryObject<Item> GUN_L86 = 						FlansMod.Gun(ITEMS, MODID, "l86");
	public static final RegistryObject<Item> GUN_L96 = 						FlansMod.Gun(ITEMS, MODID, "l96");
	public static final RegistryObject<Item> GUN_M9 = 						FlansMod.Gun(ITEMS, MODID, "m9");
	public static final RegistryObject<Item> GUN_M14 = 						FlansMod.Gun(ITEMS, MODID, "m14");
	public static final RegistryObject<Item> GUN_M16A4 = 					FlansMod.Gun(ITEMS, MODID, "m16a4");
	public static final RegistryObject<Item> GUN_M21 = 						FlansMod.Gun(ITEMS, MODID, "m21");
	public static final RegistryObject<Item> GUN_M40A3 = 					FlansMod.Gun(ITEMS, MODID, "m40a3");
	public static final RegistryObject<Item> GUN_M60 = 						FlansMod.Gun(ITEMS, MODID, "m60");
	public static final RegistryObject<Item> GUN_M72LAW = 					FlansMod.Gun(ITEMS, MODID, "m72law");
	public static final RegistryObject<Item> GUN_M249 = 					FlansMod.Gun(ITEMS, MODID, "m249");
	public static final RegistryObject<Item> GUN_M1014 = 					FlansMod.Gun(ITEMS, MODID, "m1014");
	public static final RegistryObject<Item> GUN_M1887 = 					FlansMod.Gun(ITEMS, MODID, "m1887");
	public static final RegistryObject<Item> GUN_M1911 = 					FlansMod.Gun(ITEMS, MODID, "m1911");
	public static final RegistryObject<Item> GUN_MAKAROV = 					FlansMod.Gun(ITEMS, MODID, "makarov");
	public static final RegistryObject<Item> GUN_MINIGUN = 					FlansMod.Gun(ITEMS, MODID, "minigun");
	public static final RegistryObject<Item> GUN_MP5 = 						FlansMod.Gun(ITEMS, MODID, "mp5");
	public static final RegistryObject<Item> GUN_MTAR = 					FlansMod.Gun(ITEMS, MODID, "mtar");
	public static final RegistryObject<Item> GUN_BINOCULARS = 				FlansMod.Gun(ITEMS, MODID, "binoculars");
	public static final RegistryObject<Item> GUN_KNIFE = 					FlansMod.Gun(ITEMS, MODID, "knife");
	public static final RegistryObject<Item> GUN_RIOT_SHIELD = 				FlansMod.Gun(ITEMS, MODID, "riot_shield");
	public static final RegistryObject<Item> GUN_P90 = 						FlansMod.Gun(ITEMS, MODID, "p90");
	public static final RegistryObject<Item> GUN_PANZERFAUST3 = 			FlansMod.Gun(ITEMS, MODID, "panzerfaust3");
	public static final RegistryObject<Item> GUN_R700 = 					FlansMod.Gun(ITEMS, MODID, "r700");
	public static final RegistryObject<Item> GUN_R870 = 					FlansMod.Gun(ITEMS, MODID, "r870");
	public static final RegistryObject<Item> GUN_RPD = 						FlansMod.Gun(ITEMS, MODID, "rpd");
	public static final RegistryObject<Item> GUN_RPG = 						FlansMod.Gun(ITEMS, MODID, "rpg");
	public static final RegistryObject<Item> GUN_RPK = 						FlansMod.Gun(ITEMS, MODID, "rpk");
	public static final RegistryObject<Item> GUN_SG550 = 					FlansMod.Gun(ITEMS, MODID, "sg550");
	public static final RegistryObject<Item> GUN_SIGP226 = 					FlansMod.Gun(ITEMS, MODID, "sigp226");
	public static final RegistryObject<Item> GUN_SKORPION = 				FlansMod.Gun(ITEMS, MODID, "skorpion");
	public static final RegistryObject<Item> GUN_SPAS = 					FlansMod.Gun(ITEMS, MODID, "spas");
	public static final RegistryObject<Item> GUN_STINGER = 					FlansMod.Gun(ITEMS, MODID, "stinger");
	public static final RegistryObject<Item> GUN_USP = 						FlansMod.Gun(ITEMS, MODID, "usp");
	public static final RegistryObject<Item> GUN_UZI = 						FlansMod.Gun(ITEMS, MODID, "uzi");
	public static final RegistryObject<Item> GUN_W1200 = 					FlansMod.Gun(ITEMS, MODID, "w1200");

	//ITEMS.register("r700", () -> new GunItem(new ResourceLocation(MODID, "guns/r700"), new Item.Properties()));
	
	public ModernWeaponsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.BakingCompleted event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for(var entry : ITEMS.getEntries())
			{
				shaper.register(entry.get(), new ModelResourceLocation(entry.getId(), "inventory"));
			}
		}
	}
}
