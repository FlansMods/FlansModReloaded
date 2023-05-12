package com.flansmod.client.render;

import com.flansmod.client.render.models.BakedModelProxy;
import com.flansmod.client.render.models.TurboModel;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.item.FlanItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FlansModRenderCore
{
	public FlansModRenderCore()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnReigsterModels(ModelEvent.RegisterAdditional event)
	{
		/*
		for(var kvp : CustomRenderers.entrySet())
		{
			//event.register();

			ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
			FlanItemModel model = kvp.getValue().createModel(null, itemID.getNamespace(), itemID.getPath());

			if(model != null)
			{
				for(var location : model.getModelLocations())
					event.register(new ModelResourceLocation(location, "inventory"));
			}
		}
		 */
	}

	@SubscribeEvent
	public void OnModelBake(ModelEvent.ModifyBakingResult event)
	{

	}


}
