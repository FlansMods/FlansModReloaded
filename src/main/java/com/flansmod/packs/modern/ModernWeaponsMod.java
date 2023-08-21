package com.flansmod.packs.modern;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.GunItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

	/*
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


	public static final RegistryObject<Item> BULLET_A91 = 						FlansMod.Bullet(ITEMS, MODID, "a91ammo");
	public static final RegistryObject<Item> BULLET_ACR = 						FlansMod.Bullet(ITEMS, MODID, "acr_ammo");
	public static final RegistryObject<Item> BULLET_AK47 = 						FlansMod.Bullet(ITEMS, MODID, "ak47ammo");
	public static final RegistryObject<Item> BULLET_AK74 = 						FlansMod.Bullet(ITEMS, MODID, "ak74ammo");
	public static final RegistryObject<Item> BULLET_AT4 = 						FlansMod.Bullet(ITEMS, MODID, "at4ammo");
	public static final RegistryObject<Item> BULLET_AUG = 						FlansMod.Bullet(ITEMS, MODID, "aug_ammo");
	public static final RegistryObject<Item> BULLET_BARRETT = 					FlansMod.Bullet(ITEMS, MODID, "barrett_ammo");
	public static final RegistryObject<Item> BULLET_BIZON = 					FlansMod.Bullet(ITEMS, MODID, "bizon_ammo");
	public static final RegistryObject<Item> BULLET_DESERT_EAGLE = 				FlansMod.Bullet(ITEMS, MODID, "desert_eagle_ammo");
	public static final RegistryObject<Item> BULLET_DRAGUNOV = 					FlansMod.Bullet(ITEMS, MODID, "dragunov_ammo");
	public static final RegistryObject<Item> BULLET_FNSCAR = 					FlansMod.Bullet(ITEMS, MODID, "fnscar_ammo");
	public static final RegistryObject<Item> BULLET_G3 = 						FlansMod.Bullet(ITEMS, MODID, "g3ammo");
	public static final RegistryObject<Item> BULLET_G36 = 						FlansMod.Bullet(ITEMS, MODID, "g36ammo");
	public static final RegistryObject<Item> BULLET_GL1 = 						FlansMod.Bullet(ITEMS, MODID, "gl1ammo");
	public static final RegistryObject<Item> BULLET_GL6 = 						FlansMod.Bullet(ITEMS, MODID, "gl6ammo");
	public static final RegistryObject<Item> BULLET_GLOCK = 					FlansMod.Bullet(ITEMS, MODID, "glock_ammo");
	public static final RegistryObject<Item> BULLET_L86 = 						FlansMod.Bullet(ITEMS, MODID, "l86ammo");
	public static final RegistryObject<Item> BULLET_L96 = 						FlansMod.Bullet(ITEMS, MODID, "l96ammo");
	public static final RegistryObject<Item> BULLET_M9 = 						FlansMod.Bullet(ITEMS, MODID, "m9ammo");
	public static final RegistryObject<Item> BULLET_M14 = 						FlansMod.Bullet(ITEMS, MODID, "m14ammo");
	public static final RegistryObject<Item> BULLET_M16A4 = 					FlansMod.Bullet(ITEMS, MODID, "m16a4ammo");
	public static final RegistryObject<Item> BULLET_M21 = 						FlansMod.Bullet(ITEMS, MODID, "m21ammo");
	public static final RegistryObject<Item> BULLET_M40A3 = 					FlansMod.Bullet(ITEMS, MODID, "m40a3ammo");
	public static final RegistryObject<Item> BULLET_M60 = 						FlansMod.Bullet(ITEMS, MODID, "m60ammo");
	public static final RegistryObject<Item> BULLET_M72LAW = 					FlansMod.Bullet(ITEMS, MODID, "m72law_ammo");
	public static final RegistryObject<Item> BULLET_M249 = 						FlansMod.Bullet(ITEMS, MODID, "m249ammo");
	public static final RegistryObject<Item> BULLET_M1014 = 					FlansMod.Bullet(ITEMS, MODID, "m1014ammo");
	public static final RegistryObject<Item> BULLET_M1887 = 					FlansMod.Bullet(ITEMS, MODID, "m1887ammo");
	public static final RegistryObject<Item> BULLET_M1911 = 					FlansMod.Bullet(ITEMS, MODID, "m1911ammo");
	public static final RegistryObject<Item> BULLET_MAKAROV = 					FlansMod.Bullet(ITEMS, MODID, "makarov_ammo");
	public static final RegistryObject<Item> BULLET_MINIGUN = 					FlansMod.Bullet(ITEMS, MODID, "minigun_ammo");
	public static final RegistryObject<Item> BULLET_MP5 = 						FlansMod.Bullet(ITEMS, MODID, "mp5ammo");
	public static final RegistryObject<Item> BULLET_MTAR = 						FlansMod.Bullet(ITEMS, MODID, "mtar_ammo");
	public static final RegistryObject<Item> BULLET_P90 = 						FlansMod.Bullet(ITEMS, MODID, "p90ammo");
	public static final RegistryObject<Item> BULLET_PANZERFAUST3 = 				FlansMod.Bullet(ITEMS, MODID, "panzerfaust3ammo");
	public static final RegistryObject<Item> BULLET_R700 = 						FlansMod.Bullet(ITEMS, MODID, "r700ammo");
	public static final RegistryObject<Item> BULLET_R870 = 						FlansMod.Bullet(ITEMS, MODID, "r870ammo");
	public static final RegistryObject<Item> BULLET_RPD = 						FlansMod.Bullet(ITEMS, MODID, "rpd_ammo");
	public static final RegistryObject<Item> BULLET_RPG = 						FlansMod.Bullet(ITEMS, MODID, "rpg_ammo");
	public static final RegistryObject<Item> BULLET_RPK = 						FlansMod.Bullet(ITEMS, MODID, "rpk_ammo");
	public static final RegistryObject<Item> BULLET_SG550 = 					FlansMod.Bullet(ITEMS, MODID, "sg550ammo");
	public static final RegistryObject<Item> BULLET_SIGP226 = 					FlansMod.Bullet(ITEMS, MODID, "sigp226ammo");
	public static final RegistryObject<Item> BULLET_SKORPION = 					FlansMod.Bullet(ITEMS, MODID, "skorpion_ammo");
	public static final RegistryObject<Item> BULLET_SPAS = 						FlansMod.Bullet(ITEMS, MODID, "spas_ammo");
	public static final RegistryObject<Item> BULLET_STINGER = 					FlansMod.Bullet(ITEMS, MODID, "stinger_ammo");
	public static final RegistryObject<Item> BULLET_USP = 						FlansMod.Bullet(ITEMS, MODID, "usp_ammo");
	public static final RegistryObject<Item> BULLET_UZI = 						FlansMod.Bullet(ITEMS, MODID, "uzi_ammo");
	public static final RegistryObject<Item> BULLET_W1200 = 					FlansMod.Bullet(ITEMS, MODID, "w1200ammo");

	public static final RegistryObject<Item> BULLET_AP_PISTOL = 						FlansMod.Bullet(ITEMS, MODID, "appistolammo");
	//public static final RegistryObject<Item> BULLET_AP_REVOLVER = 					FlansMod.Bullet(ITEMS, MODID, "ammo");
	public static final RegistryObject<Item> BULLET_BARRET_COMPACT = 					FlansMod.Bullet(ITEMS, MODID, "barrett_compact_ammo");
	public static final RegistryObject<Item> BULLET_BARRET_EXPLOSIVE = 					FlansMod.Bullet(ITEMS, MODID, "barrett_explosive_ammo");
	public static final RegistryObject<Item> BULLET_BETA_C_MAG = 						FlansMod.Bullet(ITEMS, MODID, "beta_cmag");
	public static final RegistryObject<Item> BULLET_HE_PISTOL = 						FlansMod.Bullet(ITEMS, MODID, "highexpistolammo");
	public static final RegistryObject<Item> BULLET_MINIGUN_EXPLOSIVE = 				FlansMod.Bullet(ITEMS, MODID, "minigun_explosive_ammo");
	public static final RegistryObject<Item> BULLET_MINIGUN_PORTABLE = 					FlansMod.Bullet(ITEMS, MODID, "minigun_compact_ammo");
	public static final RegistryObject<Item> BULLET_SEEKER_RIFLE = 						FlansMod.Bullet(ITEMS, MODID, "seekerrifleammo");
	public static final RegistryObject<Item> BULLET_SHULKER_SNIPER = 					FlansMod.Bullet(ITEMS, MODID, "shulkersniperammo");
	public static final RegistryObject<Item> BULLET_SMG_100 = 							FlansMod.Bullet(ITEMS, MODID, "smg100ammo");
	public static final RegistryObject<Item> BULLET_W1200_INCENDIARY = 					FlansMod.Bullet(ITEMS, MODID, "w1200incendiary_ammo");
	public static final RegistryObject<Item> BULLET_WITHERING_AK = 						FlansMod.Bullet(ITEMS, MODID, "witheringakammo");

	public static final RegistryObject<Item> BULLET_MK4_ROCKET = 						FlansMod.Bullet(ITEMS, MODID, "mk4rocket");
	public static final RegistryObject<Item> BULLET_HYDRA70 = 							FlansMod.Bullet(ITEMS, MODID, "hydra70");
	public static final RegistryObject<Item> BULLET_HELLFIRE = 							FlansMod.Bullet(ITEMS, MODID, "hellfire");
	public static final RegistryObject<Item> BULLET_TRIGAT = 							FlansMod.Bullet(ITEMS, MODID, "trigat");

	public static final RegistryObject<Item> ATTACHMENT_4X_SCOPE = 						FlansMod.Attachment(ITEMS, MODID, "4x_scope");
	public static final RegistryObject<Item> ATTACHMENT_ACOG_SCOPE = 					FlansMod.Attachment(ITEMS, MODID, "acog");
	public static final RegistryObject<Item> ATTACHMENT_AUTO_FIRE = 					FlansMod.Attachment(ITEMS, MODID, "auto_fire");
	public static final RegistryObject<Item> ATTACHMENT_BURST_FIRE = 					FlansMod.Attachment(ITEMS, MODID, "burst_fire");
	public static final RegistryObject<Item> ATTACHMENT_DRAGONS_BREATH_INFUSER = 		FlansMod.Attachment(ITEMS, MODID, "dragonsbreathinfuser");
	public static final RegistryObject<Item> ATTACHMENT_FLASHLIGHT = 					FlansMod.Attachment(ITEMS, MODID, "flashlight");
	public static final RegistryObject<Item> ATTACHMENT_FOREGRIP = 						FlansMod.Attachment(ITEMS, MODID, "foregrip");
	public static final RegistryObject<Item> ATTACHMENT_LONG_BARREL = 					FlansMod.Attachment(ITEMS, MODID, "long_barrel");
	public static final RegistryObject<Item> ATTACHMENT_PISTOL_FLASHLIGHT = 			FlansMod.Attachment(ITEMS, MODID, "pistol_flashlight");
	public static final RegistryObject<Item> ATTACHMENT_QUICK_RELOAD_GRIP = 			FlansMod.Attachment(ITEMS, MODID, "quickreloadgrip");
	public static final RegistryObject<Item> ATTACHMENT_RED_DOT = 						FlansMod.Attachment(ITEMS, MODID, "red_dot");
	public static final RegistryObject<Item> ATTACHMENT_REINFORCED_UPPER_RECEIVER = 	FlansMod.Attachment(ITEMS, MODID, "reinforcedupperreceiver");
	public static final RegistryObject<Item> ATTACHMENT_SILENCER = 						FlansMod.Attachment(ITEMS, MODID, "silencer");
	public static final RegistryObject<Item> ATTACHMENT_SINGLE_FIRE = 					FlansMod.Attachment(ITEMS, MODID, "single_fire");
	public static final RegistryObject<Item> ATTACHMENT_SQUEAKY_UPPER_RECEIVER = 		FlansMod.Attachment(ITEMS, MODID, "squeakyupperreceiver");


	public static final RegistryObject<Block> WORKBENCH_BLOCK_MODERN = 				FlansMod.Workbench_Block(BLOCKS, MODID, "modern");
	public static final RegistryObject<Item> WORKBENCH_ITEM_MODERN = 				FlansMod.Workbench_Item(ITEMS, MODID, "modern", WORKBENCH_BLOCK_MODERN);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_MODERN =
																					FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "modern");
	public static final RegistryObject<Block> WORKBENCH_BLOCK_MODERN_ADVANCED = 	FlansMod.Workbench_Block(BLOCKS, MODID, "modern_advanced");
	public static final RegistryObject<Item> WORKBENCH_ITEM_MODERN_ADVANCED = 		FlansMod.Workbench_Item(ITEMS, MODID, "modern_advanced", WORKBENCH_BLOCK_MODERN_ADVANCED);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_MODERN_ADVANCED =
																					FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "modern_advanced");
	*/
	
	public ModernWeaponsMod()
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

			for(var entry : ITEMS.getEntries())
			{
				event.register(new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
				shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
			}
		}
		/*
		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.BakingCompleted event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for(var entry : ITEMS.getEntries())
			{
				shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
			}
		}*/
	}
}
