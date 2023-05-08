package com.flansmod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class  FlanModelRegistration implements PreparableReloadListener
{
    private static final HashMap<Item, FlanItemModelRenderer<?>> ITEMS_TO_REGISTER = new HashMap<>();
    private static final Set<Item> ITEMS = new HashSet<>();

    public static void preRegisterRenderer(Item item, FlanItemModelRenderer<?> renderer)
    {
        ITEMS_TO_REGISTER.put(item, renderer);
        ITEMS.add(item);
    }

    public void hook(IEventBus modEventBus)
    {
        modEventBus.addListener(this::onModelRegistry);
        modEventBus.addListener(this::onModelBake);
    }

    public void onModelRegistry(ModelEvent.RegisterAdditional event)
    {
        for(var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            FlanItemModel model = kvp.getValue().createModel(null, itemID.getNamespace(), itemID.getPath());

            if(model != null)
            {
                for(var location : model.getModelLocations())
                    event.register(new ModelResourceLocation(location, "inventory"));
            }
        }
    }

    public void onModelBake(ModelEvent.ModifyBakingResult event)
    {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        TextureManager tm = Minecraft.getInstance().textureManager;

        for(var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            ModelResourceLocation modelID = new ModelResourceLocation(itemID, "inventory");

            BakedModel defaultModel = modelRegistry.get(modelID);
            FlanItemModel compoundModel = kvp.getValue().createModel(defaultModel, itemID.getNamespace(), itemID.getPath());
            compoundModel.bakeParts(event);

            for(var texLocation : compoundModel.GetTextureLocations())
                tm.register(texLocation, new SimpleTexture(texLocation));


            modelRegistry.put(modelID, compoundModel);
        }
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier,
                                          ResourceManager resourceManager,
                                          ProfilerFiller filler1,
                                          ProfilerFiller filler2,
                                          Executor executor1,
                                          Executor executor2)
    {

        return null;
    }
}
