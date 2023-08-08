package com.flansmod.packs.basics;

import com.flansmod.common.FlansMod;
import com.flansmod.packs.modern.ModernWeaponsMod;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
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

@Mod(BasicPartsMod.MODID)
public class BasicPartsMod
{
	public static final String MODID = "flansbasicparts";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

	// Storage helper blocks
	public static final RegistryObject<Block> GUNPOWDER_BLOCK = BLOCKS.register("gunpowder_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> GUNPOWDER_BLOCK_ITEM = ITEMS.register("gunpowder_block", () -> new BlockItem(GUNPOWDER_BLOCK.get(), new Item.Properties()));

	// MATERIALS
	// Metals
	// Tier I = Iron, Gold
	// Tier II = Steel, Aluminium
	public static final RegistryObject<Item> STEEL_NUGGET = ITEMS.register("steel_nugget", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> STEEL_SHEET = ITEMS.register("steel_sheet", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> STEEL_BLOCK = BLOCKS.register("steel_block", () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Item> STEEL_BLOCK_ITEM = ITEMS.register("steel_block", () -> new BlockItem(STEEL_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<Item> ALUMINIUM_NUGGET = ITEMS.register("aluminium_nugget", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> ALUMINIUM_INGOT = ITEMS.register("aluminium_ingot", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> ALUMINIUM_SHEET = ITEMS.register("aluminium_sheet", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> ALUMINIUM_BLOCK = BLOCKS.register("aluminium_block", () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Item> ALUMINIUM_BLOCK_ITEM = ITEMS.register("aluminium_block", () -> new BlockItem(ALUMINIUM_BLOCK.get(), new Item.Properties()));
	// Tier III = Nethersteel
	public static final RegistryObject<Item> NETHERSTEEL_NUGGET = ITEMS.register("nethersteel_nugget", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> NETHERSTEEL_INGOT = ITEMS.register("nethersteel_ingot", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> NETHERSTEEL_SHEET = ITEMS.register("nethersteel_sheet", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> NETHERSTEEL_BLOCK = BLOCKS.register("nethersteel_block", () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Item> NETHERSTEEL_BLOCK_ITEM = ITEMS.register("nethersteel_block", () -> new BlockItem(NETHERSTEEL_BLOCK.get(), new Item.Properties()));

	// Composites
	// Tier I = Fiberglass
	public static final RegistryObject<Item> CLAY_AND_SAND_MIXTURE = ITEMS.register("clay_and_sand_mixture", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FIBERGLASS = ITEMS.register("fiberglass", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> FIBERGLASS_BLOCK = BLOCKS.register("fiberglass_block", () -> new Block(BlockBehaviour.Properties.of(Material.CLAY)));
	public static final RegistryObject<Item> FIBERGLASS_BLOCK_ITEM = ITEMS.register("fiberglass_block", () -> new BlockItem(FIBERGLASS_BLOCK.get(), new Item.Properties()));
	// Tier II = Phantom Membrane
	public static final RegistryObject<Item> PHANTEX = ITEMS.register("phantex", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> PHANTEX_BLOCK = BLOCKS.register("phantex_block", () -> new Block(BlockBehaviour.Properties.of(Material.CLAY)));
	public static final RegistryObject<Item> PHANTEX_BLOCK_ITEM = ITEMS.register("phantex_block", () -> new BlockItem(PHANTEX_BLOCK.get(), new Item.Properties()));
	// Tier III = Carbon Fiber
	public static final RegistryObject<Item> CARBON_HEAVY_SOLUTION = ITEMS.register("carbon_heavy_solution", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> CARBON_FIBER_SHEET = ITEMS.register("carbon_fiber_sheet", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> CARBON_FIBER_BLOCK = BLOCKS.register("carbon_fiber_block", () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Item> CARBON_FIBER_BLOCK_ITEM = ITEMS.register("carbon_fiber_block", () -> new BlockItem(CARBON_FIBER_BLOCK.get(), new Item.Properties()));

	// Fabrics
	// Tier I = Wool
	// Tier II = Leather, Polyester
	public static final RegistryObject<Item> POLYESTER_THREAD = ITEMS.register("polyester_thread", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> POLYESTER_BLOCK = BLOCKS.register("polyester_block", () -> new Block(BlockBehaviour.Properties.of(Material.WOOL)));
	public static final RegistryObject<Item> POLYESTER_BLOCK_ITEM = ITEMS.register("polyester_block", () -> new BlockItem(POLYESTER_BLOCK.get(), new Item.Properties()));
	// Tier III = Shulker, Kevlar
	public static final RegistryObject<Item> KEVLAR_PANEL = ITEMS.register("kevlar_panel", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> KEVLAR_BLOCK = BLOCKS.register("kevlar_block", () -> new Block(BlockBehaviour.Properties.of(Material.WOOL)));
	public static final RegistryObject<Item> KEVLAR_BLOCK_ITEM = ITEMS.register("kevlar_block", () -> new BlockItem(KEVLAR_BLOCK.get(), new Item.Properties()));

	// Woods
	// Tier I = Soft Wood = { Oak, Birch, Acacia }
	// Tier II = Hard Wood = { Spruce, Jungle, Mangrove, Cherry }
	// Tier III = Dense Wood = { Warped, Crimson }

	// Transparents
	// Tier I - Glass
	// Tier II - Plexiglass
	// Tier III - Bulletproof Glass

	// Electronics
	// Tier I = Redstone
	public static final RegistryObject<Item> REDSTONE_CIRCUITRY = ITEMS.register("redstone_circuitry", () -> new Item(new Item.Properties()));
	// Tier II = Quartz
	public static final RegistryObject<Item> QUARTZ_CIRCUITRY = ITEMS.register("quartz_circuitry", () -> new Item(new Item.Properties()));
	// Tier III = Skulk
	public static final RegistryObject<Item> SKULK_CIRCUITRY = ITEMS.register("skulk_circuitry", () -> new Item(new Item.Properties()));

	// Engines in Metal or Electronic
	public static final RegistryObject<Item> PART_IRON_COMBUSTION_ENGINE = 					FlansMod.Part(ITEMS, MODID, "iron_combustion_engine");
	public static final RegistryObject<Item> PART_STEEL_COMBUSTION_ENGINE = 				FlansMod.Part(ITEMS, MODID, "steel_combustion_engine");
	public static final RegistryObject<Item> PART_ALUMINIUM_COMBUSTION_ENGINE = 			FlansMod.Part(ITEMS, MODID, "aluminium_combustion_engine");
	public static final RegistryObject<Item> PART_NETHERSTEEL_COMBUSTION_ENGINE = 			FlansMod.Part(ITEMS, MODID, "nethersteel_combustion_engine");
	public static final RegistryObject<Item> PART_REDSTONE_ENGINE = 						FlansMod.Part(ITEMS, MODID, "redstone_engine");
	public static final RegistryObject<Item> PART_QUARTZ_ENGINE = 							FlansMod.Part(ITEMS, MODID, "quartz_engine");
	public static final RegistryObject<Item> PART_SKULK_ENGINE = 							FlansMod.Part(ITEMS, MODID, "skulk_engine");

	// Vehicle parts in Metal or Wood
	public static final RegistryObject<Item> PART_IRON_MECHANICAL_PARTS = 					FlansMod.Part(ITEMS, MODID, "iron_mechanical_parts");
	public static final RegistryObject<Item> PART_STEEL_MECHANICAL_PARTS = 					FlansMod.Part(ITEMS, MODID, "steel_mechanical_parts");
	public static final RegistryObject<Item> PART_ALUMINIUM_MECHANICAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "aluminium_mechanical_parts");
	public static final RegistryObject<Item> PART_NETHERSTEEL_MECHANICAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "nethersteel_mechanical_parts");
	public static final RegistryObject<Item> PART_SOFT_WOOD_MECHANICAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "soft_wood_mechanical_parts");
	public static final RegistryObject<Item> PART_HARD_WOOD_MECHANICAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "hard_wood_mechanical_parts");
	public static final RegistryObject<Item> PART_DENSE_WOOD_MECHANICAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "dense_wood_mechanical_parts");

	public static final RegistryObject<Item> PART_IRON_ARMOUR = 							FlansMod.Part(ITEMS, MODID, "iron_armour");
	public static final RegistryObject<Item> PART_STEEL_ARMOUR = 							FlansMod.Part(ITEMS, MODID, "steel_armour");
	public static final RegistryObject<Item> PART_ALUMINIUM_ARMOUR = 						FlansMod.Part(ITEMS, MODID, "aluminium_armour");
	public static final RegistryObject<Item> PART_NETHERSTEEL_ARMOUR = 						FlansMod.Part(ITEMS, MODID, "nethersteel_armour");
	public static final RegistryObject<Item> PART_SOFT_WOOD_ARMOUR = 						FlansMod.Part(ITEMS, MODID, "soft_wood_armour");
	public static final RegistryObject<Item> PART_HARD_WOOD_ARMOUR = 						FlansMod.Part(ITEMS, MODID, "hard_wood_armour");
	public static final RegistryObject<Item> PART_DENSE_WOOD_ARMOUR = 						FlansMod.Part(ITEMS, MODID, "dense_wood_armour");

	// Vehicle parts in Metal, Composite or Wood
	public static final RegistryObject<Item> PART_SOFT_WOOD_STRUCTURAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "soft_wood_structural_parts");
	public static final RegistryObject<Item> PART_HARD_WOOD_STRUCTURAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "hard_wood_structural_parts");
	public static final RegistryObject<Item> PART_DENSE_WOOD_STRUCTURAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "dense_wood_structural_parts");
	public static final RegistryObject<Item> PART_IRON_STRUCTURAL_PARTS = 					FlansMod.Part(ITEMS, MODID, "iron_structural_parts");
	public static final RegistryObject<Item> PART_STEEL_STRUCTURAL_PARTS = 					FlansMod.Part(ITEMS, MODID, "steel_structural_parts");
	public static final RegistryObject<Item> PART_ALUMINIUM_STRUCTURAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "aluminium_structural_parts");
	public static final RegistryObject<Item> PART_NETHERSTEEL_STRUCTURAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "nethersteel_structural_parts");
	public static final RegistryObject<Item> PART_FIBERGLASS_STRUCTURAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "fiberglass_structural_parts");
	public static final RegistryObject<Item> PART_PHANTEX_STRUCTURAL_PARTS = 				FlansMod.Part(ITEMS, MODID, "phantex_structural_parts");
	public static final RegistryObject<Item> PART_CARBON_FIBER_STRUCTURAL_PARTS = 			FlansMod.Part(ITEMS, MODID, "carbon_fiber_structural_parts");

	public static final RegistryObject<Item> PART_SOFT_WOOD_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "soft_wood_propeller");
	public static final RegistryObject<Item> PART_HARD_WOOD_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "hard_wood_propeller");
	public static final RegistryObject<Item> PART_DENSE_WOOD_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "dense_wood_propeller");
	public static final RegistryObject<Item> PART_IRON_PROPELLER = 							FlansMod.Part(ITEMS, MODID, "iron_propeller");
	public static final RegistryObject<Item> PART_STEEL_PROPELLER = 						FlansMod.Part(ITEMS, MODID, "steel_propeller");
	public static final RegistryObject<Item> PART_ALUMINIUM_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "aluminium_propeller");
	public static final RegistryObject<Item> PART_NETHERSTEEL_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "nethersteel_propeller");
	public static final RegistryObject<Item> PART_FIBERGLASS_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "fiberglass_propeller");
	public static final RegistryObject<Item> PART_PHANTEX_PROPELLER = 						FlansMod.Part(ITEMS, MODID, "phantex_propeller");
	public static final RegistryObject<Item> PART_CARBON_FIBER_PROPELLER = 					FlansMod.Part(ITEMS, MODID, "carbon_fiber_propeller");

	// Vehicle parts in Fabric
	public static final RegistryObject<Item> PART_WOOL_CANVAS = 							FlansMod.Part(ITEMS, MODID, "wool_canvas");
	public static final RegistryObject<Item> PART_LEATHER_CANVAS = 							FlansMod.Part(ITEMS, MODID, "leather_canvas");
	public static final RegistryObject<Item> PART_POLYESTER_CANVAS = 						FlansMod.Part(ITEMS, MODID, "polyester_canvas");
	public static final RegistryObject<Item> PART_KEVLAR_CANVAS = 							FlansMod.Part(ITEMS, MODID, "kevlar_canvas");

	public static final RegistryObject<Item> PART_WOOL_CLOTHING = 							FlansMod.Part(ITEMS, MODID, "wool_clothing");
	public static final RegistryObject<Item> PART_LEATHER_CLOTHING = 						FlansMod.Part(ITEMS, MODID, "leather_clothing");
	public static final RegistryObject<Item> PART_POLYESTER_CLOTHING = 						FlansMod.Part(ITEMS, MODID, "polyester_clothing");
	public static final RegistryObject<Item> PART_KEVLAR_CLOTHING = 						FlansMod.Part(ITEMS, MODID, "kevlar_clothing");

	public static final RegistryObject<Item> PART_WOOL_SEAT = 								FlansMod.Part(ITEMS, MODID, "wool_seat");
	public static final RegistryObject<Item> PART_LEATHER_SEAT = 							FlansMod.Part(ITEMS, MODID, "leather_seat");
	public static final RegistryObject<Item> PART_POLYESTER_SEAT = 							FlansMod.Part(ITEMS, MODID, "polyester_seat");
	public static final RegistryObject<Item> PART_KEVLAR_SEAT = 							FlansMod.Part(ITEMS, MODID, "kevlar_seat");

	public static final RegistryObject<Item> PART_WOOL_WHEEL = 								FlansMod.Part(ITEMS, MODID, "wool_wheel");
	public static final RegistryObject<Item> PART_LEATHER_WHEEL = 							FlansMod.Part(ITEMS, MODID, "leather_wheel");
	public static final RegistryObject<Item> PART_POLYESTER_WHEEL = 						FlansMod.Part(ITEMS, MODID, "polyester_wheel");
	public static final RegistryObject<Item> PART_KEVLAR_WHEEL = 							FlansMod.Part(ITEMS, MODID, "kevlar_wheel");

	// Gun parts in Metal or Composite
	public static final RegistryObject<Item> PART_IRON_UPPER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "iron_upper_receiver");
	public static final RegistryObject<Item> PART_ALUMINIUM_UPPER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "aluminium_upper_receiver");
	public static final RegistryObject<Item> PART_STEEL_UPPER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "steel_upper_receiver");
	public static final RegistryObject<Item> PART_NETHERSTEEL_UPPER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "nethersteel_upper_receiver");
	public static final RegistryObject<Item> PART_FIBERGLASS_UPPER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "fiberglass_upper_receiver");
	public static final RegistryObject<Item> PART_PHANTEX_UPPER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "phantex_upper_receiver");
	public static final RegistryObject<Item> PART_CARBON_FIBER_UPPER_RECEIVER = 			FlansMod.Part(ITEMS, MODID, "carbon_fiber_upper_receiver");

	public static final RegistryObject<Item> PART_IRON_LOWER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "iron_lower_receiver");
	public static final RegistryObject<Item> PART_ALUMINIUM_LOWER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "aluminium_lower_receiver");
	public static final RegistryObject<Item> PART_STEEL_LOWER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "steel_lower_receiver");
	public static final RegistryObject<Item> PART_NETHERSTEEL_LOWER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "nethersteel_lower_receiver");
	public static final RegistryObject<Item> PART_FIBERGLASS_LOWER_RECEIVER = 				FlansMod.Part(ITEMS, MODID, "fiberglass_lower_receiver");
	public static final RegistryObject<Item> PART_PHANTEX_LOWER_RECEIVER = 					FlansMod.Part(ITEMS, MODID, "phantex_lower_receiver");
	public static final RegistryObject<Item> PART_CARBON_FIBER_LOWER_RECEIVER = 			FlansMod.Part(ITEMS, MODID, "carbon_fiber_lower_receiver");

	public static final RegistryObject<Item> PART_IRON_BARREL = 							FlansMod.Part(ITEMS, MODID, "iron_barrel");
	public static final RegistryObject<Item> PART_ALUMINIUM_BARREL = 						FlansMod.Part(ITEMS, MODID, "aluminium_barrel");
	public static final RegistryObject<Item> PART_STEEL_BARREL = 							FlansMod.Part(ITEMS, MODID, "steel_barrel");
	public static final RegistryObject<Item> PART_NETHERSTEEL_BARREL = 						FlansMod.Part(ITEMS, MODID, "nethersteel_barrel");
	public static final RegistryObject<Item> PART_FIBERGLASS_BARREL = 						FlansMod.Part(ITEMS, MODID, "fiberglass_barrel");
	public static final RegistryObject<Item> PART_PHANTEX_BARREL = 							FlansMod.Part(ITEMS, MODID, "phantex_barrel");
	public static final RegistryObject<Item> PART_CARBON_FIBER_BARREL = 					FlansMod.Part(ITEMS, MODID, "carbon_fiber_barrel");

	// Gun parts in Metal, Composite or Wood
	public static final RegistryObject<Item> PART_SOFT_WOOD_STOCK = 						FlansMod.Part(ITEMS, MODID, "soft_wood_stock");
	public static final RegistryObject<Item> PART_HARD_WOOD_STOCK = 						FlansMod.Part(ITEMS, MODID, "hard_wood_stock");
	public static final RegistryObject<Item> PART_DENSE_WOOD_STOCK =						FlansMod.Part(ITEMS, MODID, "dense_wood_stock");
	public static final RegistryObject<Item> PART_IRON_STOCK = 								FlansMod.Part(ITEMS, MODID, "iron_stock");
	public static final RegistryObject<Item> PART_ALUMINIUM_STOCK = 						FlansMod.Part(ITEMS, MODID, "aluminium_stock");
	public static final RegistryObject<Item> PART_STEEL_STOCK = 							FlansMod.Part(ITEMS, MODID, "steel_stock");
	public static final RegistryObject<Item> PART_NETHERSTEEL_STOCK = 						FlansMod.Part(ITEMS, MODID, "nethersteel_stock");
	public static final RegistryObject<Item> PART_FIBERGLASS_STOCK = 						FlansMod.Part(ITEMS, MODID, "fiberglass_stock");
	public static final RegistryObject<Item> PART_PHANTEX_STOCK = 							FlansMod.Part(ITEMS, MODID, "phantex_stock");
	public static final RegistryObject<Item> PART_CARBON_FIBER_STOCK = 						FlansMod.Part(ITEMS, MODID, "carbon_fiber_stock");

	public static final RegistryObject<Item> PART_SOFT_WOOD_GRIP = 							FlansMod.Part(ITEMS, MODID, "soft_wood_grip");
	public static final RegistryObject<Item> PART_HARD_WOOD_GRIP = 							FlansMod.Part(ITEMS, MODID, "hard_wood_grip");
	public static final RegistryObject<Item> PART_DENSE_WOOD_GRIP =							FlansMod.Part(ITEMS, MODID, "dense_wood_grip");
	public static final RegistryObject<Item> PART_IRON_GRIP = 								FlansMod.Part(ITEMS, MODID, "iron_grip");
	public static final RegistryObject<Item> PART_ALUMINIUM_GRIP = 							FlansMod.Part(ITEMS, MODID, "aluminium_grip");
	public static final RegistryObject<Item> PART_STEEL_GRIP = 								FlansMod.Part(ITEMS, MODID, "steel_grip");
	public static final RegistryObject<Item> PART_NETHERSTEEL_GRIP = 						FlansMod.Part(ITEMS, MODID, "nethersteel_grip");
	public static final RegistryObject<Item> PART_FIBERGLASS_GRIP = 						FlansMod.Part(ITEMS, MODID, "fiberglass_grip");
	public static final RegistryObject<Item> PART_PHANTEX_GRIP = 							FlansMod.Part(ITEMS, MODID, "phantex_grip");
	public static final RegistryObject<Item> PART_CARBON_FIBER_GRIP = 						FlansMod.Part(ITEMS, MODID, "carbon_fiber_grip");

	// Bullets
	public static final RegistryObject<Item> BULLET_PISTOL =								FlansMod.Bullet(ITEMS, MODID, "pistol_bullet");
	public static final RegistryObject<Item> BULLET_PISTOL_AP =								FlansMod.Bullet(ITEMS, MODID, "pistol_bullet_ap");
	public static final RegistryObject<Item> BULLET_PISTOL_EX =								FlansMod.Bullet(ITEMS, MODID, "pistol_bullet_ex");
	public static final RegistryObject<Item> BULLET_PISTOL_INCENDIARY =						FlansMod.Bullet(ITEMS, MODID, "pistol_bullet_incendiary");
	public static final RegistryObject<Item> BULLET_RIFLE =									FlansMod.Bullet(ITEMS, MODID, "rifle_bullet");
	public static final RegistryObject<Item> BULLET_RIFLE_AP =								FlansMod.Bullet(ITEMS, MODID, "rifle_bullet_ap");
	public static final RegistryObject<Item> BULLET_RIFLE_EX =								FlansMod.Bullet(ITEMS, MODID, "rifle_bullet_ex");
	public static final RegistryObject<Item> BULLET_RIFLE_INCENDIARY =						FlansMod.Bullet(ITEMS, MODID, "rifle_bullet_incendiary");
	public static final RegistryObject<Item> BULLET_ARTILLERY =								FlansMod.Bullet(ITEMS, MODID, "artillery_shell");
	public static final RegistryObject<Item> BULLET_ARTILLERY_AP =							FlansMod.Bullet(ITEMS, MODID, "artillery_shell_ap");
	public static final RegistryObject<Item> BULLET_ARTILLERY_EX =							FlansMod.Bullet(ITEMS, MODID, "artillery_shell_ex");
	public static final RegistryObject<Item> BULLET_ARTILLERY_INCENDIARY =					FlansMod.Bullet(ITEMS, MODID, "artillery_shell_incendiary");




	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL =							FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_birdshot");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_AP =						FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_ap");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_EX =						FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_ex");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_INCENDIARY =				FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_incendiary");


	public BasicPartsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
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
