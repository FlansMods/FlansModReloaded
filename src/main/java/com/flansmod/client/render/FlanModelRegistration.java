package com.flansmod.client.render;

import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.FlansMod;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FlanModelRegistration implements PreparableReloadListener
{
    private static final HashMap<Item, FlanItemModelRenderer> ITEMS_TO_REGISTER = new HashMap<>();
    private static final HashMap<ModelResourceLocation, BakedModel> ENTITY_MODELS_TO_REGISTER = new HashMap<>();

    public static void PreRegisterRenderer(Item item, FlanItemModelRenderer renderer)
    {
        ITEMS_TO_REGISTER.put(item, renderer);
    }

    public static void PreRegisterEntityModel(ResourceLocation location)
    {
        ModelResourceLocation modelLoc = new ModelResourceLocation(location, "inventory");
        ENTITY_MODELS_TO_REGISTER.put(modelLoc, null);
    }

    public void Hook(IEventBus modEventBus)
    {
        modEventBus.addListener(this::OnModelRegistry);
        modEventBus.addListener(this::OnModelBake);
        modEventBus.addListener(this::OnBakingComplete);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void OnRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("turborig", TurboRig.LOADER);
    }

    public void OnModelRegistry(ModelEvent.RegisterAdditional event)
    {
        for(var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            if(itemID != null)
                event.register(new ModelResourceLocation(itemID, "inventory"));
        }

        for(ModelResourceLocation modelLoc : ENTITY_MODELS_TO_REGISTER.keySet())
        {
            event.register(modelLoc);
        }
    }

    public void OnModelBake(ModelEvent.ModifyBakingResult event)
    {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        TextureManager tm = Minecraft.getInstance().textureManager;

        for (var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            if(itemID != null)
            {
                ModelResourceLocation modelID = new ModelResourceLocation(itemID, "inventory");
                BakedModel bakedModel = modelRegistry.get(modelID);
                if (bakedModel instanceof TurboRig.Baked turboBakedModel)
                {
                    kvp.getValue().OnBakeComplete(turboBakedModel);
                }
            }
        }

        List<ModelResourceLocation> locations = new ArrayList<>(ENTITY_MODELS_TO_REGISTER.keySet());
        for(ModelResourceLocation loc : locations)
        {
            BakedModel bakedModel = modelRegistry.get(loc);
            if(bakedModel != null)
            {
                ENTITY_MODELS_TO_REGISTER.put(loc, bakedModel);
            }
            else
            {
                FlansMod.LOGGER.warn("Failed to load entity model " + loc);
            }
        }
    }

    public void OnBakingComplete(ModelEvent.BakingCompleted event)
    {
        ModelBakery bakery = Minecraft.getInstance().getModelManager().getModelBakery();
        for(var kvp : bakery.getBakedTopLevelModels().entrySet())
        {
            if(kvp.getKey().getNamespace().equals("minecraft"))
                continue;
            ResourceLocation key = kvp.getKey();
            if(key instanceof  ModelResourceLocation modelKey)
                key = modelKey.withPath(modelKey.getPath().split("#")[0]);
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (ITEMS_TO_REGISTER.containsKey(item))
            {
                UnbakedModel unbaked = bakery.getModel(kvp.getKey());
                if (unbaked instanceof BlockModel blockModel)
                {
                    if (blockModel.customData.hasCustomGeometry())
                    {
                        if(blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedTurbo)
                        {
                            ITEMS_TO_REGISTER.get(item).OnUnbakedModelLoaded(unbakedTurbo);
                        }
                    }
                }
                else
                {
                    FlansMod.LOGGER.info("Removed item " + item + " from the custom renderers");
                    ITEMS_TO_REGISTER.remove(item);
                }
            }
        }
    }

    public FlanItemModelRenderer GetModelRenderer(JsonDefinition def)
    {
        Item item = ForgeRegistries.ITEMS.getValue(def.Location);
        if(item instanceof FlanItem flanItem)
        {
            if(ITEMS_TO_REGISTER.containsKey(flanItem))
            {
                return ITEMS_TO_REGISTER.get(flanItem);
            }
        }
        return null;
    }

    public FlanItemModelRenderer GetModelRenderer(ItemStack stack)
    {
        if(stack.getItem() instanceof FlanItem flanItem)
        {
            if(ITEMS_TO_REGISTER.containsKey(flanItem))
            {
                return ITEMS_TO_REGISTER.get(flanItem);
            }
        }
        return null;
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
