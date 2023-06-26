package com.flansmod.client.render;

import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.item.FlanItem;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FlanModelRegistration implements PreparableReloadListener
{
    private static final HashMap<Item, FlanItemModelRenderer> ITEMS_TO_REGISTER = new HashMap<>();
    private static final Set<Item> ITEMS = new HashSet<>();

    public static void preRegisterRenderer(Item item, FlanItemModelRenderer renderer)
    {
        ITEMS_TO_REGISTER.put(item, renderer);
        ITEMS.add(item);
    }

    public void hook(IEventBus modEventBus)
    {
        modEventBus.addListener(this::onModelRegistry);
        modEventBus.addListener(this::onModelBake);
        modEventBus.addListener(this::OnBakingComplete);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void OnRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("turborig", TurboRig.LOADER);
    }

    public void onModelRegistry(ModelEvent.RegisterAdditional event)
    {
        for(var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            event.register(new ModelResourceLocation(itemID, "inventory"));

            //UnbakedModel unbaked = Minecraft.getInstance().getModelManager().getModelBakery().getModel(itemID);



            //FlanItemModel model = kvp.getValue().createModel(null, itemID.getNamespace(), itemID.getPath());

            //if(model != null)
            //{
             //   for(var location : model.getModelLocations())
              //      event.register(new ModelResourceLocation(location, "inventory"));
            //}
        }
    }

    public void onModelBake(ModelEvent.ModifyBakingResult event)
    {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        TextureManager tm = Minecraft.getInstance().textureManager;

        for (var kvp : ITEMS_TO_REGISTER.entrySet())
        {
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
            ModelResourceLocation modelID = new ModelResourceLocation(itemID, "inventory");

            BakedModel bakedModel = modelRegistry.get(modelID);
            //UnbakedModel unbaked = event.getModelBakery().getModel(modelID);
            if (bakedModel instanceof TurboRig.Baked turboBakedModel)
            {
                // if(unbaked instanceof TurboRig turboUnbakedModel)
                //  {
                kvp.getValue().OnBakeComplete(turboBakedModel);
                // }
                //for(var texLocation : turboBakedModel.GetTextureLocations())
                //    tm.register(texLocation, new SimpleTexture(texLocation));
            }
            //FlanItemModel compoundModel = kvp.getValue().createModel(defaultModel, itemID.getNamespace(), itemID.getPath());
            //compoundModel.bakeParts(event);

            //for(var texLocation : compoundModel.GetTextureLocations())
            //    tm.register(texLocation, new SimpleTexture(texLocation));


            //modelRegistry.put(modelID, compoundModel);
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
            UnbakedModel unbaked = bakery.getModel(kvp.getKey());
            if(unbaked instanceof BlockModel blockModel)
            {
                if(blockModel.customData.hasCustomGeometry() && blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedTurbo)
                {
                    if (ITEMS_TO_REGISTER.containsKey(item))
                    {
                        ITEMS_TO_REGISTER.get(item).OnUnbakedModelLoaded(unbakedTurbo);
                    }
                }
            }
            else if(unbaked instanceof TurboRig unbakedTurbo)
            {
                if (ITEMS_TO_REGISTER.containsKey(item))
                {
                    ITEMS_TO_REGISTER.get(item).OnUnbakedModelLoaded(unbakedTurbo);
                }
            }
            else
            {
            }
        }
    }

    @SubscribeEvent
    public void OnRenderHands(RenderHandEvent event)
    {

        if(event.getItemStack().getItem() instanceof FlanItem flanItem)
        {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(flanItem);
            if(ITEMS_TO_REGISTER.containsKey(flanItem))
            {
                ITEMS_TO_REGISTER.get(flanItem).RenderFirstPerson(
                    Minecraft.getInstance().player,
                    event.getItemStack(),
                    MinecraftHelpers.GetArm(event.getHand()),
                    event.getHand() == InteractionHand.OFF_HAND ? ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
                    event.getPoseStack(),
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    0,
                    event.getEquipProgress());
            }

            event.setCanceled(true);
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
