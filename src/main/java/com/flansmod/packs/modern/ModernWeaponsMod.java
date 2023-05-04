package com.flansmod.packs.modern;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
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


	public static final RegistryObject<Item> GUN_A91 = 						FlansMod.Gun(MODID, "a91");
	public static final RegistryObject<Item> GUN_ACR = 						FlansMod.Gun(MODID, "acr");
	public static final RegistryObject<Item> GUN_AK47 = 					FlansMod.Gun(MODID, "ak47");
	public static final RegistryObject<Item> GUN_AK74 = 					FlansMod.Gun(MODID, "ak74");
	public static final RegistryObject<Item> GUN_AT4 = 						FlansMod.Gun(MODID, "at4");
	public static final RegistryObject<Item> GUN_AUG = 						FlansMod.Gun(MODID, "aug");
	public static final RegistryObject<Item> GUN_BARRETT = 					FlansMod.Gun(MODID, "barrett");
	public static final RegistryObject<Item> GUN_BIZON = 					FlansMod.Gun(MODID, "bizon");
	public static final RegistryObject<Item> GUN_DESERT_EAGLE = 			FlansMod.Gun(MODID, "desert_eagle");
	public static final RegistryObject<Item> GUN_DRAGUNOV = 				FlansMod.Gun(MODID, "dragunov");
	public static final RegistryObject<Item> GUN_FNSCAR = 					FlansMod.Gun(MODID, "fnscar");
	public static final RegistryObject<Item> GUN_G3 = 						FlansMod.Gun(MODID, "g3");
	public static final RegistryObject<Item> GUN_G36 = 						FlansMod.Gun(MODID, "g36");
	public static final RegistryObject<Item> GUN_GL1 = 						FlansMod.Gun(MODID, "gl1");
	public static final RegistryObject<Item> GUN_GL6 = 						FlansMod.Gun(MODID, "gl6");
	public static final RegistryObject<Item> GUN_GLOCK = 					FlansMod.Gun(MODID, "glock");
	//public static final RegistryObject<Item> GUN_GOLD_DESERT_EAGLE = 		FlansMod.Gun(MODID, "");
	//public static final RegistryObject<Item> GUN_HYDRA70 = 				FlansMod.Gun(MODID, "hydra70");
	public static final RegistryObject<Item> GUN_L86 = 						FlansMod.Gun(MODID, "l86");
	public static final RegistryObject<Item> GUN_L96 = 						FlansMod.Gun(MODID, "l96");
	public static final RegistryObject<Item> GUN_M9 = 						FlansMod.Gun(MODID, "m9");
	public static final RegistryObject<Item> GUN_M14 = 						FlansMod.Gun(MODID, "m14");
	public static final RegistryObject<Item> GUN_M16A4 = 					FlansMod.Gun(MODID, "m16a4");
	public static final RegistryObject<Item> GUN_M21 = 						FlansMod.Gun(MODID, "m21");
	public static final RegistryObject<Item> GUN_M40A3 = 					FlansMod.Gun(MODID, "m40a3");
	public static final RegistryObject<Item> GUN_M60 = 						FlansMod.Gun(MODID, "m60");
	public static final RegistryObject<Item> GUN_M72LAW = 					FlansMod.Gun(MODID, "m72law");
	public static final RegistryObject<Item> GUN_M249 = 					FlansMod.Gun(MODID, "m249");
	public static final RegistryObject<Item> GUN_M1014 = 					FlansMod.Gun(MODID, "m1014");
	public static final RegistryObject<Item> GUN_M1887 = 					FlansMod.Gun(MODID, "m1887");
	public static final RegistryObject<Item> GUN_M1911 = 					FlansMod.Gun(MODID, "m1911");
	public static final RegistryObject<Item> GUN_MAKAROV = 					FlansMod.Gun(MODID, "makarov");
	public static final RegistryObject<Item> GUN_MINIGUN = 					FlansMod.Gun(MODID, "minigun");
	public static final RegistryObject<Item> GUN_MP5 = 						FlansMod.Gun(MODID, "mp5");
	public static final RegistryObject<Item> GUN_MTAR = 					FlansMod.Gun(MODID, "mtar");
	public static final RegistryObject<Item> GUN_BINOCULARS = 				FlansMod.Gun(MODID, "binoculars");
	public static final RegistryObject<Item> GUN_KNIFE = 					FlansMod.Gun(MODID, "knife");
	public static final RegistryObject<Item> GUN_RIOT_SHIELD = 				FlansMod.Gun(MODID, "riot_shield");
	public static final RegistryObject<Item> GUN_P90 = 						FlansMod.Gun(MODID, "p90");
	public static final RegistryObject<Item> GUN_PANZERFAUST3 = 			FlansMod.Gun(MODID, "panzerfaust3");
	public static final RegistryObject<Item> GUN_R700 = 					FlansMod.Gun(MODID, "r700");
	public static final RegistryObject<Item> GUN_R870 = 					FlansMod.Gun(MODID, "r870");
	public static final RegistryObject<Item> GUN_RPD = 						FlansMod.Gun(MODID, "rpd");
	public static final RegistryObject<Item> GUN_RPG = 						FlansMod.Gun(MODID, "rpg");
	public static final RegistryObject<Item> GUN_RPK = 						FlansMod.Gun(MODID, "rpk");
	public static final RegistryObject<Item> GUN_SG550 = 					FlansMod.Gun(MODID, "sg550");
	public static final RegistryObject<Item> GUN_SIGP226 = 					FlansMod.Gun(MODID, "sigp226");
	public static final RegistryObject<Item> GUN_SKORPION = 				FlansMod.Gun(MODID, "skorpion");
	public static final RegistryObject<Item> GUN_SPAS = 					FlansMod.Gun(MODID, "spas");
	public static final RegistryObject<Item> GUN_STINGER = 					FlansMod.Gun(MODID, "stinger");
	public static final RegistryObject<Item> GUN_USP = 						FlansMod.Gun(MODID, "usp");
	public static final RegistryObject<Item> GUN_UZI = 						FlansMod.Gun(MODID, "uzi");
	public static final RegistryObject<Item> GUN_W1200 = 					FlansMod.Gun(MODID, "w1200");

	//ITEMS.register("r700", () -> new GunItem(new ResourceLocation(MODID, "guns/r700"), new Item.Properties()));
	
	public ModernWeaponsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
	}
}
