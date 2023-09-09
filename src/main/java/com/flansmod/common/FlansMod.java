package com.flansmod.common;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.crafting.*;
import com.flansmod.common.gunshots.ActionManager;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.item.*;
import com.flansmod.common.projectiles.BulletEntity;
import com.flansmod.common.types.attachments.AttachmentDefinitions;
import com.flansmod.common.types.crafting.MaterialDefinitions;
import com.flansmod.common.types.crafting.WorkbenchDefinitions;
import com.flansmod.common.types.grenades.GrenadeDefinitions;
import com.flansmod.common.types.guns.GunDefinitions;
import com.flansmod.common.types.bullets.BulletDefinitions;
import com.flansmod.common.types.magazines.MagazineDefinitions;
import com.flansmod.common.types.npc.NpcDefinitions;
import com.flansmod.common.types.parts.PartDefinitions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(FlansMod.MODID)
public class FlansMod
{
    public static final String MODID = "flansmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean DEBUG = false;

    // Core mod blocks & items
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);


    public static final RegistryObject<EntityType<BulletEntity>> ENT_TYPE_BULLET = ENTITY_TYPES.register(
        "bullet",
        () -> EntityType.Builder.of(
            BulletEntity::new,
            MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .build("bullet"));

    public static final RegistryObject<Item> RAINBOW_PAINT_CAN_ITEM = ITEMS.register("rainbow_paint_can", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MAG_UPGRADE_ITEM = ITEMS.register("magazine_upgrade", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Block> GUN_MACHINING_TABLE_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "gun_machining_table");
    public static final RegistryObject<Block> GUN_MOD_TABLE_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "gun_modification_table");
    public static final RegistryObject<Block> DIESEL_GENERATOR_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "portable_diesel_generator");
    public static final RegistryObject<Block> COAL_GENERATOR_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "portable_coal_generator");
    public static final RegistryObject<Block> WEAPON_CRATE = FlansMod.Workbench_Block(BLOCKS, MODID, "weapon_crate");


    public static final RegistryObject<Item> GUN_MACHINING_TABLE_ITEM = ITEMS.register("gun_machining_table", () -> new BlockItem(GUN_MACHINING_TABLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GUN_MOD_TABLE_ITEM = ITEMS.register("gun_modification_table", () -> new BlockItem(GUN_MOD_TABLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> DIESEL_GENERATOR_ITEM = ITEMS.register("portable_diesel_generator", () -> new BlockItem(DIESEL_GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> COAL_GENERATOR_ITEM = ITEMS.register("portable_coal_generator", () -> new BlockItem(COAL_GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> WEAPON_CRATE_ITEM = ITEMS.register("weapon_crate", () -> new BlockItem(WEAPON_CRATE.get(), new Item.Properties()));

    // Tile entities
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    //public static final RegistryObject<BlockEntityType<GunModBlockEntity>> GUN_MOD_TILE_ENTITY = TILE_ENTITIES.register("gun_modification_table", () -> BlockEntityType.Builder.of(GunModBlockEntity::new, GUN_MOD_TABLE_BLOCK.get()).build(null));
    //public static final RegistryObject<BlockEntityType<GeneratorBlockEntity>> DIESEL_GENERATOR_TILE_ENTITY = TILE_ENTITIES.register("diesel_generator", () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, DIESEL_GENERATOR_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> GUN_CRAFTING_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "gun_machining_table");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> DIESEL_GENERATOR_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "portable_diesel_generator");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> COAL_GENERATOR_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "portable_coal_generator");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> GUN_MODIFICATION_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "gun_modification_table");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WEAPON_CRATE_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "weapon_crate");


    // Creative Mode Tabs
    //public static final RegistryObject<CreativeModeTabs> GUNS_TAB = new CreativeModeTabFlansMod("");
    // Menus
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH_MENU = MENUS.register("workbench", () -> IForgeMenuType.create(WorkbenchMenu::new));

    // Definition Repositories
    public static final GunDefinitions GUNS = new GunDefinitions();
    public static final BulletDefinitions BULLETS = new BulletDefinitions();
    public static final GrenadeDefinitions GRENADES = new GrenadeDefinitions();
    public static final AttachmentDefinitions ATTACHMENTS = new AttachmentDefinitions();
    public static final PartDefinitions PARTS = new PartDefinitions();
    public static final WorkbenchDefinitions WORKBENCHES = new WorkbenchDefinitions();
    public static final MaterialDefinitions MATERIALS = new MaterialDefinitions();
    public static final MagazineDefinitions MAGAZINES = new MagazineDefinitions();
    public static final NpcDefinitions NPCS = new NpcDefinitions();

    // Server handlers
    public static final ActionManager ACTIONS_SERVER = new ActionManager(false);

    public static RegistryObject<Item> Gun(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new GunItem(loc, new Item.Properties().stacksTo(1)));
    }

    public static RegistryObject<Item> Bullet(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new BulletItem(loc, new Item.Properties()));
    }

    public static RegistryObject<Item> Attachment(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new AttachmentItem(loc, new Item.Properties()));
    }

    public static RegistryObject<Item> Part(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new PartItem(loc, new Item.Properties()));
    }

    public static RegistryObject<Block> Workbench_Block(DeferredRegister<Block> blockRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return blockRegister.register(name, () -> new WorkbenchBlock(loc, BlockBehaviour.Properties.of(Material.STONE)));
    }

    public static RegistryObject<Item> Workbench_Item(DeferredRegister<Item> itemRegister, String modID, String name, RegistryObject<Block> block)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static RegistryObject<BlockEntityType<WorkbenchBlockEntity>> Workbench_TileEntityType(DeferredRegister<BlockEntityType<?>> tileEntityTypeRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return tileEntityTypeRegister.register(name, () -> { return new WorkbenchBlockEntity.WorkbenchBlockEntityTypeHolder(loc).CreateType(); });
    }


    public FlansMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::CommonInit);
        modEventBus.addListener(this::ClientInit);
        MinecraftForge.EVENT_BUS.addListener(this::loadLevel);
        MinecraftForge.EVENT_BUS.addListener(this::onReloadResources);
        ACTIONS_SERVER.HookServer(modEventBus);
        modEventBus.addListener(this::onCreativeTabRegistry);
        modEventBus.addListener(this::OnRegsiterEvent);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

    }

    private void CommonInit(final FMLCommonSetupEvent event)
    {
    }

    private void loadLevel(LevelEvent.Load event)
    {
        new Raytracer(event.getLevel()).hook();
    }

    @OnlyIn(Dist.CLIENT)
    private void ClientInit(final FMLClientSetupEvent event)
    {
        FlansModClient.Init();
    }

    private void onCreativeTabRegistry(CreativeModeTabEvent.Register event)
    {
        event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_guns"), builder ->
        {
            List<ItemStack> stacks = new ArrayList<>();
            for(Item item : ForgeRegistries.ITEMS.getValues())
            {
                if(item instanceof GunItem || item instanceof BulletItem || item instanceof GrenadeItem)
                    stacks.add(new ItemStack(item));
            }

            builder
                .title(Component.translatable("item_group." + MODID + ".creative_tab_guns"))
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.DIAMOND))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
                    populator.accept(GUN_MACHINING_TABLE_ITEM.get());
                    populator.accept(GUN_MOD_TABLE_ITEM.get());
                    populator.accept(DIESEL_GENERATOR_ITEM.get());
                    populator.accept(COAL_GENERATOR_ITEM.get());
                    populator.accept(WEAPON_CRATE_ITEM.get());
                   for(ItemStack stack : stacks)
                       populator.accept(stack);
                });
        });

        event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_parts"), builder ->
        {
            List<ItemStack> stacks = new ArrayList<>();
            for(Item item : ForgeRegistries.ITEMS.getValues())
            {
                if(item instanceof PartItem)
                    stacks.add(new ItemStack(item));
            }

            builder
                .title(Component.translatable("item_group." + MODID + ".creative_tab_parts"))
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.STICK))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
                    populator.accept(GUN_MACHINING_TABLE_ITEM.get());
                    for(ItemStack stack : stacks)
                            populator.accept(stack);
                });
        });

        event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_modifiers"), builder ->
        {
            List<ItemStack> stacks = new ArrayList<>();
            for(Item item : ForgeRegistries.ITEMS.getValues())
            {
                if(item instanceof AttachmentItem)
                    stacks.add(new ItemStack(item));
            }

            builder
                .title(Component.translatable("item_group." + MODID + ".creative_tab_modifiers"))
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.BOOK))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
                    populator.accept(GUN_MOD_TABLE_ITEM.get());
                    populator.accept(RAINBOW_PAINT_CAN_ITEM.get());
                    populator.accept(MAG_UPGRADE_ITEM.get());
                    for(ItemStack stack : stacks)
                        populator.accept(stack);
                });
        });

        //event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_vehicles"), builder ->
        //{
        //    builder
        //        .title(Component.translatable("item_group." + MODID + ".creative_tab_vehicles"))
        //        .icon(() -> new ItemStack(Items.DIAMOND))
        //        .displayItems((enabledFlags, populator, hasPermissions) ->
        //        {
        //
        //        });
        //});
    }

    private void OnRegsiterEvent(RegisterEvent event)
    {
        if(event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
        {
            //CraftingHelper.register(new ResourceLocation(MODID, "tiered_material"), FlansModIngredient.Serializer.INSTANCE);
        }
    }
    
    private void onReloadResources(AddReloadListenerEvent event)
    {
        event.addListener(GUNS);
        event.addListener(BULLETS);
        event.addListener(ATTACHMENTS);
        event.addListener(PARTS);
        event.addListener(WORKBENCHES);
        event.addListener(MATERIALS);
        event.addListener(MAGAZINES);
        event.addListener(NPCS);

        if(FMLEnvironment.dist == Dist.CLIENT)
            RegisterClientReloadListeners(event);
    }

    @OnlyIn(Dist.CLIENT)
    private void RegisterClientReloadListeners(AddReloadListenerEvent event)
    {
        FlansModClient.RegisterClientDataReloadListeners(event);
    }
}
