package com.flansmod.packs.vendersgame.client;

import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.packs.vendersgame.VendersGameMod;
import com.flansmod.packs.vendersgame.client.VenderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class VenderRenderer extends LivingEntityRenderer<ShopkeeperEntity, VenderModel<ShopkeeperEntity>>
{
	private static final ResourceLocation VENDER_SKIN_LOCATION = new ResourceLocation(VendersGameMod.MODID, "textures/entity/vender.png");

	public VenderRenderer(EntityRendererProvider.Context context)
	{
		super(context, new VenderModel<>(context.bakeLayer(VenderModel.MODEL_LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(ShopkeeperEntity shopkeeper)
	{
		return VENDER_SKIN_LOCATION;
	}
}
