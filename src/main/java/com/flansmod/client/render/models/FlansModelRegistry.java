package com.flansmod.client.render.models;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.JsonDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class FlansModelRegistry implements PreparableReloadListener
{
    //private static final Map<Item, FlanItemModelRenderer> ITEM_CUSTOM_RENDERERS = new HashMap<>();
    //private static final Map<ResourceLocation, ITurboRenderer> CUSTOM_RENDERERS = new HashMap<>();
    //private static final List<ResourceLocation> OTHER_MODEL_LOCATIONS = new ArrayList<>();

    // PreRegister functions. Call these early if you want things to be rendered custom
    //public static void PreRegisterRenderer(@Nonnull Item item, @Nonnull FlanItemModelRenderer renderer)
    //{
    //    ITEM_CUSTOM_RENDERERS.put(item, renderer);
    //}


    private record ModelLoadOp(@Nonnull Supplier<ResourceLocation> locationProvider)
    {
    }
    private static final List<ModelLoadOp> ModelLoadOps = new ArrayList<>();
    private static final Map<ResourceLocation, TurboRig> UnbakedRigs = new HashMap<>();
    private static final Map<ResourceLocation, TurboRig.Baked> BakedRigs = new HashMap<>();
    private static final Map<ResourceLocation, ITurboRenderer> RendererLookup = new HashMap<>();

    public static void PreRegisterModel(@Nonnull ResourceLocation loc)
    {
        ModelLoadOps.add(new ModelLoadOp(() -> loc));
    }
    public static void PreRegisterModel(@Nonnull Supplier<ResourceLocation> locProvider)
    {
        ModelLoadOps.add(new ModelLoadOp(locProvider));
    }
    public static void PreRegisterRenderer(@Nonnull ResourceLocation loc, @Nonnull ITurboRenderer renderer)
    {
        ModelLoadOps.add(new ModelLoadOp(() -> loc));
        RendererLookup.put(loc, renderer);
    }
    public static void PreRegisterRenderer(@Nonnull Supplier<ResourceLocation> locProvider, @Nonnull ITurboRenderer renderer)
    {
        ModelLoadOps.add(new ModelLoadOp(locProvider));
        RendererLookup.put(locProvider.get(), renderer);
    }


    @Nullable
    public static ITurboRenderer GetItemRenderer(@Nonnull Item item)
    {
        if(IClientItemExtensions.of(item) instanceof IClientFlanItemExtensions flanItemExtensions)
        {
            return flanItemExtensions.GetTurboRenderer();
        }
        return null;
    }
    @Nullable
    public static ITurboRenderer GetItemRenderer(@Nonnull ItemStack stack)
    {
        if(IClientItemExtensions.of(stack) instanceof IClientFlanItemExtensions flanItemExtensions)
        {
            return flanItemExtensions.GetTurboRenderer();
        }
        return null;
    }
    @Nullable
    public static ITurboRenderer GetItemRenderer(@Nonnull ResourceLocation modelLoc)
    {
        if(RendererLookup.containsKey(modelLoc))
            return RendererLookup.get(modelLoc);

        Item item = ForgeRegistries.ITEMS.getValue(modelLoc);
        ITurboRenderer renderer = item != null ? GetItemRenderer(item) : null;
        RendererLookup.put(modelLoc, renderer);
        return renderer;
    }

    @Nonnull
    public static TurboRenderUtility GetRigWrapperFor(@Nonnull ResourceLocation loc)
    {
        return TurboRenderUtility.of(UnbakedRigs.get(loc), BakedRigs.get(loc));
    }

    //public static void PreRegisterRenderer(@Nonnull ResourceLocation location, @Nonnull FlanItemModelRenderer renderer)
    //{
    //    CUSTOM_RENDERERS.put(location, renderer);
    //}
    //public static void PreRegisterEntityModel(@Nonnull ResourceLocation location)
    //{
    //    OTHER_MODEL_LOCATIONS.add(location);
    //}


    // -----------------------------------------------------------------------------------------------------------------
    // Forge events
    public void Hook(@Nonnull IEventBus modEventBus)
    {
        modEventBus.addListener(this::OnModelRegistry);
        modEventBus.addListener(this::OnModelBake);
        modEventBus.addListener(this::OnBakingComplete);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void OnRegisterGeometryLoaders(@Nonnull ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("turborig", TurboRig.LOADER);
    }

    public void OnModelRegistry(@Nonnull ModelEvent.RegisterAdditional event)
    {
        // Register standard Item models, with FlanItemModelRenderers
        //List<Item> invalidItems = new ArrayList<>();
        //for (var kvp : ITEM_CUSTOM_RENDERERS.entrySet())
        //{
        //    ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
        //    if (itemID != null)
        //        event.register(new ModelResourceLocation(itemID, "inventory"));
        //    else
        //    {
        //        invalidItems.add(kvp.getKey());
        //        FlansMod.LOGGER.warn("Custom renderer for item " + kvp.getKey() + " was registered, but the item never got registered");
        //    }
        //}
        //if (!invalidItems.isEmpty())
        //{
        //    FlansMod.LOGGER.warn("Removing "+invalidItems.size()+" items from the render registration");
        //    for(Item item : invalidItems)
        //        ITEM_CUSTOM_RENDERERS.remove(item);
        //}

        for(ModelLoadOp op : ModelLoadOps)
        {
            event.register(new ModelResourceLocation(op.locationProvider.get(), "inventory"));
        }

        // Register other models for the mod, entities, muzzle flashes/effects anything misc
        //for(ResourceLocation loc : CUSTOM_RENDERERS.keySet())
        //{
        //    event.register(new ModelResourceLocation(loc, "inventory"));
        //}
        //for(ResourceLocation loc : OTHER_MODEL_LOCATIONS)
        //{
        //    event.register(new ModelResourceLocation(loc, "inventory"));
        //}
    }

    public void OnBakingComplete(@Nonnull ModelEvent.BakingCompleted event)
    {
        ModelBakery bakery = Minecraft.getInstance().getModelManager().getModelBakery();
        for(ModelLoadOp op : ModelLoadOps)
        {
            ResourceLocation resLoc = op.locationProvider.get();
            ModelResourceLocation modelID = new ModelResourceLocation(resLoc, "inventory");
            UnbakedModel unbaked = bakery.getModel(modelID);
            if(unbaked instanceof BlockModel blockModel)
                if(blockModel.customData.hasCustomGeometry())
                    if(blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedRig)
                        UnbakedRigs.put(resLoc, unbakedRig);
        }


        //for(var kvp : ITEM_CUSTOM_RENDERERS.entrySet())
        //{
        //    ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
        //    if(itemID != null)
        //    {
        //        ModelResourceLocation modelID = new ModelResourceLocation(itemID, "inventory");
        //        UnbakedModel unbaked = bakery.getModel(modelID);
        //        if(unbaked instanceof BlockModel blockModel)
        //            if(blockModel.customData.hasCustomGeometry())
        //                if(blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedRig)
        //                    kvp.getValue().OnUnbakedLoaded(unbakedRig);
        //    }
        //}
        //for(var kvp : CUSTOM_RENDERERS.entrySet())
        //{
        //    ModelResourceLocation modelID = new ModelResourceLocation(kvp.getKey(), "inventory");
        //    UnbakedModel unbaked = bakery.getModel(modelID);
        //    if(unbaked instanceof BlockModel blockModel)
        //        if(blockModel.customData.hasCustomGeometry())
        //            if(blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedRig)
        //                kvp.getValue().OnUnbakedLoaded(unbakedRig);
        //}
    }

    public void OnModelBake(@Nonnull ModelEvent.ModifyBakingResult event)
    {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        TextureManager tm = Minecraft.getInstance().textureManager;

        for(ModelLoadOp op : ModelLoadOps)
        {
            ResourceLocation resLoc = op.locationProvider.get();
            ModelResourceLocation modelID = new ModelResourceLocation(resLoc, "inventory");
            BakedModel bakedModel = modelRegistry.get(modelID);
            if(bakedModel instanceof TurboRig.Baked turboBaked)
                BakedRigs.put(resLoc, turboBaked);
        }

        // Slightly hacky, but we take the result event and give the renderers a direct reference to the
        // TurboRig.Baked model instance
        //for (var kvp : ITEM_CUSTOM_RENDERERS.entrySet())
        //{
        //    ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
        //    if(itemID != null)
        //    {
        //        ModelResourceLocation modelID = new ModelResourceLocation(itemID, "inventory");
        //        BakedModel bakedModel = modelRegistry.get(modelID);
        //        if(bakedModel instanceof TurboRig.Baked turboBaked)
        //            kvp.getValue().OnBakedLoaded(turboBaked);
        //    }
        //}
        // Same goes for any other custom renderers
        //for (var kvp : CUSTOM_RENDERERS.entrySet())
        //{
        //    ModelResourceLocation modelID = new ModelResourceLocation(kvp.getKey(), "inventory");
        //    BakedModel bakedModel = modelRegistry.get(modelID);
        //    if(bakedModel instanceof TurboRig.Baked turboBaked)
        //        kvp.getValue().OnBakedLoaded(turboBaked);
        //}

        // We actually don't need to do anything with the "fire and forget" registrations
        // OTHER_MODEL_LOCATIONS
    }

    @Override
    @Nonnull
    public CompletableFuture<Void> reload(@Nonnull PreparationBarrier preparationBarrier,
                                          @Nonnull ResourceManager resourceManager,
                                          @Nonnull ProfilerFiller filler1,
                                          @Nonnull ProfilerFiller filler2,
                                          @Nonnull Executor executor1,
                                          @Nonnull Executor executor2)
    {
        return CompletableFuture.allOf();
    }
}
