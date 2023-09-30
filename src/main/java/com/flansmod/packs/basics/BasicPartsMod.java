package com.flansmod.packs.basics;

import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.packs.basics.client.DistillationTowerScreen;
import com.flansmod.packs.basics.common.DistillationRecipe;
import com.flansmod.packs.basics.common.DistillationTowerBlock;
import com.flansmod.packs.basics.common.DistillationTowerBlockEntity;
import com.flansmod.packs.basics.common.DistillationTowerMenu;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@SuppressWarnings("unused")
@Mod(BasicPartsMod.MODID)
public class BasicPartsMod
{
	public static final String MODID = "flansbasicparts";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

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
	public static final RegistryObject<Item> PHANTASMAL_RESIDUE = ITEMS.register("phantasmal_residue", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> PHANTEX_SHEET = ITEMS.register("phantex_sheet", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Block> PHANTEX_BLOCK = BLOCKS.register("phantex_block", () -> new Block(BlockBehaviour.Properties.of(Material.CLAY)));
	public static final RegistryObject<Item> PHANTEX_BLOCK_ITEM = ITEMS.register("phantex_block", () -> new BlockItem(PHANTEX_BLOCK.get(), new Item.Properties()));
	// Tier III = Carbon Fiber
	public static final RegistryObject<Item> CARBON_HEAVY_COAL = ITEMS.register("carbon_heavy_coal", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> CARBON_PRECURSOR_CRYSTAL = ITEMS.register("carbon_precursor_crystal", () -> new Item(new Item.Properties()));
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

	// Transparent
	// Tier I - Glass
	// Tier II - Plexiglass
	// Tier III - Bulletproof Glass

	// Electronics
	// Tier I = Redstone
	public static final RegistryObject<Item> REDSTONE_CIRCUITRY = ITEMS.register("redstone_circuitry", () -> new Item(new Item.Properties()));
	// Tier II = Quartz
	public static final RegistryObject<Item> QUARTZ_CIRCUITRY = ITEMS.register("quartz_circuitry", () -> new Item(new Item.Properties()));
	// Tier III = Sculk
	public static final RegistryObject<Item> SCULK_CIRCUITRY = ITEMS.register("sculk_circuitry", () -> new Item(new Item.Properties()));

	// Engines in Metal or Electronic
	public static final RegistryObject<Item> PART_IRON_COMBUSTION_ENGINE = 					FlansMod.Part(ITEMS, MODID, "iron_combustion_engine");
	public static final RegistryObject<Item> PART_STEEL_COMBUSTION_ENGINE = 				FlansMod.Part(ITEMS, MODID, "steel_combustion_engine");
	public static final RegistryObject<Item> PART_ALUMINIUM_COMBUSTION_ENGINE = 			FlansMod.Part(ITEMS, MODID, "aluminium_combustion_engine");
	public static final RegistryObject<Item> PART_NETHERSTEEL_COMBUSTION_ENGINE = 			FlansMod.Part(ITEMS, MODID, "nethersteel_combustion_engine");
	public static final RegistryObject<Item> PART_REDSTONE_ENGINE = 						FlansMod.Part(ITEMS, MODID, "redstone_engine");
	public static final RegistryObject<Item> PART_QUARTZ_ENGINE = 							FlansMod.Part(ITEMS, MODID, "quartz_engine");
	public static final RegistryObject<Item> PART_SCULK_ENGINE = 							FlansMod.Part(ITEMS, MODID, "sculk_engine");

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
	public static final RegistryObject<Item> BULLET_PROJECTILE_GRENADE_IMPACT =				FlansMod.Bullet(ITEMS, MODID, "projectile_grenade_impact");
	public static final RegistryObject<Item> BULLET_PROJECTILE_GRENADE_STICKY =				FlansMod.Bullet(ITEMS, MODID, "projectile_grenade_sticky");
	public static final RegistryObject<Item> BULLET_PROJECTILE_GRENADE_EX =					FlansMod.Bullet(ITEMS, MODID, "projectile_grenade_ex");
	public static final RegistryObject<Item> BULLET_PROJECTILE_GRENADE_INCENDIARY =			FlansMod.Bullet(ITEMS, MODID, "projectile_grenade_incendiary");

	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_BUCKSHOT =				FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_buckshot");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_BIRDSHOT =				FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_birdshot");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_AP =						FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_ap");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_EX =						FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_ex");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_INCENDIARY =				FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_incendiary");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_FLECHETTE =				FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_flechette");
	public static final RegistryObject<Item> BULLET_SHOTGUN_SHELL_FLECHETTE_POISONED =		FlansMod.Bullet(ITEMS, MODID, "shotgun_shell_flechette_poisoned");


	public static final RegistryObject<Block> WORKBENCH_BLOCK_PARTS = 						FlansMod.Workbench_Block(BLOCKS, MODID, "part_fabricator");
	public static final RegistryObject<Item> WORKBENCH_ITEM_PARTS = 						FlansMod.Workbench_Item(ITEMS, MODID, "part_fabricator", WORKBENCH_BLOCK_PARTS);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_PARTS =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "part_fabricator");
	public static final RegistryObject<Block> WORKBENCH_BLOCK_BULLETS = 					FlansMod.Workbench_Block(BLOCKS, MODID, "bullet_fabricator");
	public static final RegistryObject<Item> WORKBENCH_ITEM_BULLETS = 						FlansMod.Workbench_Item(ITEMS, MODID, "bullet_fabricator", WORKBENCH_BLOCK_BULLETS);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_BULLETS =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "bullet_fabricator");


	public static final RegistryObject<Block> DISTILLATION_TOWER_TOP = 						BLOCKS.register("distillation_tower_top", () -> new DistillationTowerBlock(true, BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Block> DISTILLATION_TOWER = 							BLOCKS.register("distillation_tower", () -> new DistillationTowerBlock(false, BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
	public static final RegistryObject<Item> DISTILLATION_TOWER_TOP_ITEM = 					ITEMS.register("distillation_tower_top", () -> new BlockItem(DISTILLATION_TOWER_TOP.get(), new Item.Properties()));
	public static final RegistryObject<Item> DISTILLATION_TOWER_ITEM = 						ITEMS.register("distillation_tower", () -> new BlockItem(DISTILLATION_TOWER.get(), new Item.Properties()));
	public static final RegistryObject<BlockEntityType<DistillationTowerBlockEntity>> DISTILLATION_TOWER_TOP_TILE_ENTITY =
		TILE_ENTITIES.register("distillation_tower_top", () -> new DistillationTowerBlockEntity.DistillationTowerBlockEntityTypeHolder(true).CreateType());
	public static final RegistryObject<BlockEntityType<DistillationTowerBlockEntity>> DISTILLATION_TOWER_TILE_ENTITY =
		TILE_ENTITIES.register("distillation_tower", () -> new DistillationTowerBlockEntity.DistillationTowerBlockEntityTypeHolder(false).CreateType());
	public static final RegistryObject<MenuType<DistillationTowerMenu>> DISTILLATION_TOWER_MENU = MENUS.register("distillation_tower", () -> IForgeMenuType.create(DistillationTowerMenu::new));
	public static final RegistryObject<RecipeType<DistillationRecipe>> DISTILLATION_RECIPE_TYPE = RECIPE_TYPES.register("distillation", () -> RecipeType.simple(new ResourceLocation(MODID, "distillation")));
	public static final RegistryObject<RecipeSerializer<DistillationRecipe>> DISTILLATION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("distillation", DistillationRecipe.Serializer::new);


	public BasicPartsMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		MENUS.register(modEventBus);
		RECIPE_TYPES.register(modEventBus);
		RECIPE_SERIALIZERS.register(modEventBus);
		modEventBus.register(this);

	}

	@SubscribeEvent
	public void OnCreativeTabs(CreativeModeTabEvent.BuildContents event)
	{
		if(event.getTab().getDisplayName() == FlansMod.CREATIVE_TAB_NAME_BULLETS)
		{
			event.accept(WORKBENCH_ITEM_BULLETS);
		}
		else if(event.getTab().getDisplayName() == FlansMod.CREATIVE_TAB_NAME_PARTS)
		{
			event.accept(DISTILLATION_TOWER_ITEM);
			event.accept(DISTILLATION_TOWER_TOP_ITEM);

			event.accept(WORKBENCH_ITEM_PARTS);

			event.accept(PHANTASMAL_RESIDUE);
			event.accept(CARBON_HEAVY_COAL);
			event.accept(CARBON_PRECURSOR_CRYSTAL);

			event.accept(STEEL_NUGGET);
			event.accept(STEEL_INGOT);
			event.accept(STEEL_BLOCK_ITEM);

			event.accept(ALUMINIUM_NUGGET);
			event.accept(ALUMINIUM_INGOT);
			event.accept(ALUMINIUM_BLOCK_ITEM);

			event.accept(NETHERSTEEL_NUGGET);
			event.accept(NETHERSTEEL_INGOT);
			event.accept(NETHERSTEEL_BLOCK_ITEM);

			event.accept(FIBERGLASS);
			event.accept(FIBERGLASS_BLOCK_ITEM);

			event.accept(PHANTEX_SHEET);
			event.accept(PHANTEX_BLOCK_ITEM);

			event.accept(CARBON_FIBER_SHEET);
			event.accept(CARBON_FIBER_BLOCK_ITEM);

			event.accept(POLYESTER_THREAD);
			event.accept(POLYESTER_BLOCK_ITEM);

			event.accept(KEVLAR_PANEL);
			event.accept(KEVLAR_BLOCK_ITEM);

			event.accept(Items.GUNPOWDER);
			event.accept(GUNPOWDER_BLOCK_ITEM);

			event.accept(REDSTONE_CIRCUITRY);
			event.accept(QUARTZ_CIRCUITRY);
			event.accept(SCULK_CIRCUITRY);
		}
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		static
		{
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/artillery_shell", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/artillery_shell_ap", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/artillery_shell_ex", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/artillery_shell_incendiary", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/pistol_bullet", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/pistol_bullet_ap", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/pistol_bullet_ex", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/pistol_bullet_incendiary", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/projectile_grenade_impact", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/projectile_grenade_sticky", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/projectile_grenade_ex", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/projectile_grenade_incendiary", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/rifle_bullet", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/rifle_bullet_ap", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/rifle_bullet_ex", "inventory"));
			FlanModelRegistration.PreRegisterEntityModel(new ModelResourceLocation(MODID, "entity/rifle_bullet_incendiary", "inventory"));
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void ClientInit(final FMLClientSetupEvent event)
		{
			// Screens
			MenuScreens.register(BasicPartsMod.DISTILLATION_TOWER_MENU.get(), DistillationTowerScreen::new);
		}

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
