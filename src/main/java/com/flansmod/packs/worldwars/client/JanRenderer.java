package com.flansmod.packs.worldwars.client;

import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.packs.worldwars.WorldWarsMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class JanRenderer extends LivingEntityRenderer<ShopkeeperEntity, JanModel<ShopkeeperEntity>>
{
	private static final ResourceLocation JAN_SKIN_LOCATION = new ResourceLocation(WorldWarsMod.MODID, "textures/entity/jan.png");

	public JanRenderer(EntityRendererProvider.Context context)
	{
		super(context, new JanModel<>(context.bakeLayer(JanModel.MODEL_LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(ShopkeeperEntity shopkeeper)
	{
		return JAN_SKIN_LOCATION;
	}
}
