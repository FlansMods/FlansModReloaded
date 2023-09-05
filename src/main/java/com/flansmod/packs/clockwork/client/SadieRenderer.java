package com.flansmod.packs.clockwork.client;

import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.packs.clockwork.ClockworkCalamityMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class SadieRenderer extends LivingEntityRenderer<ShopkeeperEntity, SadieModel<ShopkeeperEntity>>
{
	private static final ResourceLocation SADIE_SKIN_LOCATION = new ResourceLocation(ClockworkCalamityMod.MODID, "textures/entity/sadie.png");

	public SadieRenderer(EntityRendererProvider.Context context)
	{
		super(context, new SadieModel<>(context.bakeLayer(SadieModel.MODEL_LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(ShopkeeperEntity shopkeeper)
	{
		return SADIE_SKIN_LOCATION;
	}
}

