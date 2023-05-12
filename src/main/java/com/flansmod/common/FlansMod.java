package com.flansmod.common;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.gunshots.GunshotManager;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinitions;
import com.flansmod.common.types.guns.GunDefinitions;
import com.flansmod.common.types.bullets.BulletDefinitions;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.codehaus.plexus.util.CachedMap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(FlansMod.MODID)
public class FlansMod
{
    public static final String MODID = "flansmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static boolean DEBUG = false;

    // Examples
    //public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    //public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    //public static final RegistryObject<Item> R700 = ITEMS.register("r700", () -> new GunItem(new ResourceLocation(FlansMod.MODID, "guns/r700"), new Item.Properties()));
    public static GunDefinitions GUNS = new GunDefinitions();
    public static BulletDefinitions BULLETS = new BulletDefinitions();
    public static AttachmentDefinitions ATTACHMENTS = new AttachmentDefinitions();

    public static GunshotManager GUNSHOTS = new GunshotManager();

    public static RegistryObject<Item> Gun(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        RegistryObject<Item> item = itemRegister.register(name, () -> new GunItem(loc, new Item.Properties()));
        return item;
    }
    //public static final RegistryObject<CreativeModeTabs> GUNS_TAB = new CreativeModeTabFlansMod("");

    public FlansMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.addListener(this::loadLevel);
        MinecraftForge.EVENT_BUS.addListener(this::onReloadResources);

        GUNSHOTS.Hook(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onCreativeTabRegistry);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    private void loadLevel(LevelEvent.Load event)
    {
        new Raytracer(event.getLevel()).hook();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event)
    {
        FlansModClient.Init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GUNSHOTS.HookClient(modEventBus);
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
                    for(Item item : ForgeRegistries.ITEMS.getValues())
                    {
                        if(item instanceof GunItem)
                            populator.accept(new ItemStack(item));
                    }
                });
        });
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event)
    {


        //if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS)
        //    event.accept(R700);
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
