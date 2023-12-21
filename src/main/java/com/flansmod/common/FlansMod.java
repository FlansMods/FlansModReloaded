package com.flansmod.common;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextCache;
import com.flansmod.common.actions.ServerActionManager;
import com.flansmod.common.actions.contexts.GunContextPlayer;
import com.flansmod.common.crafting.*;
import com.flansmod.common.entity.NpcRelationshipCapabilityAttacher;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.item.*;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.projectiles.BulletEntity;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.abilities.AbilityDefinitions;
import com.flansmod.common.types.abilities.elements.AbilityProviderDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityEffect;
import com.flansmod.common.types.attachments.AttachmentDefinitions;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.MaterialDefinitions;
import com.flansmod.common.types.crafting.WorkbenchDefinitions;
import com.flansmod.common.types.grenades.GrenadeDefinitions;
import com.flansmod.common.types.guns.GunDefinitions;
import com.flansmod.common.types.bullets.BulletDefinitions;
import com.flansmod.common.types.magazines.MagazineDefinitions;
import com.flansmod.common.types.npc.NpcDefinitions;
import com.flansmod.common.types.parts.PartDefinitions;
import com.flansmod.common.worldgen.loot.LootPopulator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Mod(FlansMod.MODID)
public class FlansMod
{
    public static final String MODID = "flansmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean DEBUG = false;

    // Registers
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    // Core mod blocks & items
    public static final RegistryObject<EntityType<BulletEntity>> ENT_TYPE_BULLET = ENTITY_TYPES.register(
        "bullet",
        () -> EntityType.Builder.of(
            BulletEntity::new,
            MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .build("bullet"));

    public static final RegistryObject<Item> RAINBOW_PAINT_CAN_ITEM = ITEMS.register("rainbow_paint_can", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MAG_UPGRADE_ITEM = ITEMS.register("magazine_upgrade", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Block> GUN_MOD_TABLE_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "gun_modification_table");
    public static final RegistryObject<Block> DIESEL_GENERATOR_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "portable_diesel_generator");
    public static final RegistryObject<Block> COAL_GENERATOR_BLOCK = FlansMod.Workbench_Block(BLOCKS, MODID, "portable_coal_generator");
    public static final RegistryObject<Item> GUN_MOD_TABLE_ITEM = ITEMS.register("gun_modification_table", () -> new BlockItem(GUN_MOD_TABLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> DIESEL_GENERATOR_ITEM = ITEMS.register("portable_diesel_generator", () -> new BlockItem(DIESEL_GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> COAL_GENERATOR_ITEM = ITEMS.register("portable_coal_generator", () -> new BlockItem(COAL_GENERATOR_BLOCK.get(), new Item.Properties()));

    // Tile entities
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> DIESEL_GENERATOR_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "portable_diesel_generator");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> COAL_GENERATOR_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "portable_coal_generator");
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> GUN_MODIFICATION_TILE_ENTITY = Workbench_TileEntityType(TILE_ENTITIES, MODID, "gun_modification_table");

    // Menus
    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH_MENU = MENUS.register("workbench", () -> IForgeMenuType.create(WorkbenchMenu::new));

    // Recipes
    public static final RegistryObject<RecipeType<PartFabricationRecipe>> PART_FABRICATION_RECIPE_TYPE = RECIPE_TYPES.register("part_fabrication", () -> RecipeType.simple(new ResourceLocation(MODID, "part_fabrication")));
    public static final RegistryObject<RecipeSerializer<PartFabricationRecipe>> PART_FABRICATION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("part_fabrication", PartFabricationRecipe.Serializer::new);

    // Loot Modifiers
    public static final RegistryObject<Codec<LootPopulator>> LOOT_POPULATOR =               LOOT_MODIFIERS.register("loot_populator", LootPopulator.CODEC);

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
    public static final AbilityDefinitions ABILITIES = new AbilityDefinitions();

    // Server handlers
    public static final ServerActionManager ACTIONS_SERVER = new ServerActionManager();
    public static final GunContextCache GUN_CONTEXTS_SERVER = new GunContextCache();

    public static RegistryObject<Item> Gun(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new GunItem(loc, new Item.Properties().stacksTo(1)));
    }

    public static RegistryObject<Item> Tool(DeferredRegister<Item> itemRegister, String modID, String name)
    {
        // TODO: Check that this actually works correctly
        return Gun(itemRegister, modID, name);
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
        return blockRegister.register(name, () -> new WorkbenchBlock(loc, BlockBehaviour.Properties.of(Material.STONE).dynamicShape()));
    }

    public static RegistryObject<Item> Workbench_Item(DeferredRegister<Item> itemRegister, String modID, String name, RegistryObject<Block> block)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return itemRegister.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static RegistryObject<BlockEntityType<WorkbenchBlockEntity>> Workbench_TileEntityType(DeferredRegister<BlockEntityType<?>> tileEntityTypeRegister, String modID, String name)
    {
        ResourceLocation loc = new ResourceLocation(modID, name);
        return tileEntityTypeRegister.register(name, () -> new WorkbenchBlockEntity.WorkbenchBlockEntityTypeHolder(loc).CreateType());
    }

    public static GunContextCache GetGunContextCache(boolean client)
    {
        if(client)
        {
            return GetClientGunContextCache();
        }
        return GUN_CONTEXTS_SERVER;
    }
    private static GunContextCache GetClientGunContextCache() { return FlansModClient.GUN_CONTEXTS_CLIENT; }

    public FlansMod()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::CommonInit);
        ACTIONS_SERVER.HookServer(modEventBus);
        FlansModPacketHandler.RegisterMessages();
        modEventBus.addListener(this::OnCreativeTabRegistry);
        modEventBus.addListener(this::OnRegsiterEvent);

        new NpcRelationshipCapabilityAttacher();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);
    }

    private void CommonInit(final FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void OnLevelLoad(LevelEvent.Load event)
    {
        new Raytracer(event.getLevel()).hook();
        ShooterContext.OnLevelLoaded();
    }

    public static final Component CREATIVE_TAB_NAME_GUNS = Component.translatable("item_group." + MODID + ".creative_tab_guns");
    public static final Component CREATIVE_TAB_NAME_PARTS = Component.translatable("item_group." + MODID + ".creative_tab_parts");
    public static final Component CREATIVE_TAB_NAME_BULLETS = Component.translatable("item_group." + MODID + ".creative_tab_bullets");
    public static final Component CREATIVE_TAB_NAME_MODIFIERS = Component.translatable("item_group." + MODID + ".creative_tab_modifiers");

    private void OnCreativeTabRegistry(CreativeModeTabEvent.Register event)
    {
        event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_guns"), builder ->
        {
            List<ItemStack> stacks = new ArrayList<>();
            for(Item item : ForgeRegistries.ITEMS.getValues())
            {
                if(item instanceof GunItem)
                    stacks.add(new ItemStack(item));
            }

            builder
                .title(CREATIVE_TAB_NAME_GUNS)
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.DIAMOND))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
                    populator.accept(GUN_MOD_TABLE_ITEM.get());
                    populator.accept(DIESEL_GENERATOR_ITEM.get());
                    populator.accept(COAL_GENERATOR_ITEM.get());
                   for(ItemStack stack : stacks)
                       populator.accept(stack);
                });
        });

        event.registerCreativeModeTab(new ResourceLocation(MODID, "creative_tab_bullets"), builder ->
        {
            List<ItemStack> stacks = new ArrayList<>();
            for(Item item : ForgeRegistries.ITEMS.getValues())
            {
                if(item instanceof BulletItem || item instanceof GrenadeItem)
                    stacks.add(new ItemStack(item));
            }

            builder
                .title(CREATIVE_TAB_NAME_BULLETS)
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.STICK))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
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
                .title(CREATIVE_TAB_NAME_PARTS)
                .icon(() -> stacks.size() > 0 ? stacks.get(0) : new ItemStack(Items.STICK))
                .displayItems((enabledFlags, populator, hasPermissions) ->
                {
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
                .title(CREATIVE_TAB_NAME_MODIFIERS)
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
    }

    private void OnRegsiterEvent(RegisterEvent event)
    {
        if(event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
        {
            CraftingHelper.register(
                new ResourceLocation(MODID, "tiered_material"),
                TieredMaterialIngredient.Serializer.INSTANCE);
            CraftingHelper.register(
                new ResourceLocation(MODID, "stacked_vanilla"),
                StackedVanillaIngredient.Serializer.INSTANCE);
        }
    }

    @SubscribeEvent
    public void OnReloadResources(AddReloadListenerEvent event)
    {
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER)
        {
            RegisterCommonReloadListeners(event::addListener);
        }
    }

    @SubscribeEvent
    public void OnLivingDeath(LivingDeathEvent deathEvent)
    {
        DamageSource damageSource = deathEvent.getSource();
        LivingEntity target = deathEvent.getEntity();
        if(!damageSource.isBypassInvul())
        {
            if(target instanceof ServerPlayer player)
            {
                ShooterContext shooterContext = ShooterContext.GetOrCreate(player);
                for(GunContext gunContext : shooterContext.GetAllGunContexts(false))
                {
                    if(gunContext.IsValid() && gunContext instanceof GunContextPlayer gunContextPlayer)
                    {
                        // We ONLY check for this ability on attachments, because we need something to consume to stop it going infinite
                        for(EAttachmentType attachmentType : EAttachmentType.values())
                        {
                            for(int i = 0; i < gunContext.GetNumAttachmentStacks(attachmentType); i++)
                            {
                                ItemStack attachmentStack = gunContext.GetAttachmentStack(attachmentType, i);
                                if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
                                {
                                    for(AbilityProviderDefinition abilityProvider : attachmentItem.Def().abilities)
                                    {
                                        AbilityDefinition abilityDef = abilityProvider.GetAbility();
                                        if(abilityDef.effectType == EAbilityEffect.TotemOfUndying)
                                        {
                                            if(net.minecraftforge.common.ForgeHooks.onLivingUseTotem(player, damageSource, attachmentStack, gunContextPlayer.GetHand()))
                                            {
                                                gunContext.SetAttachmentStack(attachmentType, i, ItemStack.EMPTY);

                                                player.awardStat(Stats.ITEM_USED.get(attachmentStack.getItem()), 1);
                                                CriteriaTriggers.USED_TOTEM.trigger(player, attachmentStack);

                                                player.setHealth(1.0f);
                                                player.removeAllEffects();
                                                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                                                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                                                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                                                player.level.broadcastEntityEvent(player, (byte)35);

                                                deathEvent.setCanceled(true);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void RegisterCommonReloadListeners(Consumer<PreparableReloadListener> registerFunc)
    {
        registerFunc.accept(GUNS);
        registerFunc.accept(BULLETS);
        registerFunc.accept(ATTACHMENTS);
        registerFunc.accept(PARTS);
        registerFunc.accept(WORKBENCHES);
        registerFunc.accept(MATERIALS);
        registerFunc.accept(MAGAZINES);
        registerFunc.accept(NPCS);
        registerFunc.accept(ABILITIES);
    }
}
