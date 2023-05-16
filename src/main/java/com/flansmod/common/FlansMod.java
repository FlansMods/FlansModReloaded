package com.flansmod.common;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.crafting.GunCraftingBlock;
import com.flansmod.common.gunshots.GunshotManager;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.crafting.GunModTableBlock;
import com.flansmod.common.types.attachments.AttachmentDefinitions;
import com.flansmod.common.types.guns.GunDefinitions;
import com.flansmod.common.types.bullets.BulletDefinitions;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(FlansMod.MODID)
public class FlansMod
{
    public static final String MODID = "flansmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean DEBUG = false;

    // Core mod blocks & items
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Block> GUN_MACHINING_TABLE = BLOCKS.register("gun_machining_table", () -> new GunCraftingBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> GUN_MACHINING_TABLE_ITEM = ITEMS.register("gun_machining_table", () -> new BlockItem(GUN_MACHINING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Block> GUN_MODIFICATION_TABLE = BLOCKS.register("gun_modification_table", () -> new GunModTableBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> GUN_MODIFICATION_TABLE_ITEM = ITEMS.register("gun_modification_table", () -> new BlockItem(GUN_MODIFICATION_TABLE.get(), new Item.Properties()));

    // Creative Mode Tabs
    //public static final RegistryObject<CreativeModeTabs> GUNS_TAB = new CreativeModeTabFlansMod("");

    // Definition Repositories
    public static GunDefinitions GUNS = new GunDefinitions();
    public static BulletDefinitions BULLETS = new BulletDefinitions();
    public static AttachmentDefinitions ATTACHMENTS = new AttachmentDefinitions();

    // Server handlers
    public static GunshotManager GUNSHOTS_SERVER = new GunshotManager();

    public static RegistryObject<Item> Gun(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        RegistryObject<Item> item = itemRegister.register(name, () -> new GunItem(loc, new Item.Properties()));
        return item;
    }

    public FlansMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::CommonInit);
        modEventBus.addListener(this::ClientInit);
        MinecraftForge.EVENT_BUS.addListener(this::loadLevel);
        MinecraftForge.EVENT_BUS.addListener(this::onReloadResources);
        GUNSHOTS_SERVER.HookServer(modEventBus);
        modEventBus.addListener(this::onCreativeTabRegistry);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
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
            builder
                .title(Component.translatable("item_group." + MODID + ".creative_tab_guns"))
                .icon(() -> new ItemStack(Items.DIAMOND))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
                    populator.accept(GUN_MACHINING_TABLE_ITEM.get());
                    populator.accept(GUN_MODIFICATION_TABLE_ITEM.get());
                    for(Item item : ForgeRegistries.ITEMS.getValues())
                    {
                        if(item instanceof GunItem)
                            populator.accept(new ItemStack(item));
                    }
                });
        });
    }
    
    private void onReloadResources(AddReloadListenerEvent event)
    {
        event.addListener(GUNS);
        event.addListener(BULLETS);
        event.addListener(ATTACHMENTS);

        if(FMLEnvironment.dist == Dist.CLIENT)
            RegisterClientReloadListeners(event);
    }

    @OnlyIn(Dist.CLIENT)
    private void RegisterClientReloadListeners(AddReloadListenerEvent event)
    {
        FlansModClient.RegisterClientReloadListeners(event);
    }
}
