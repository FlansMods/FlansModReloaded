package com.flansmod.packs.clockwork.client;

import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.packs.clockwork.ClockworkCalamityMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChipRenderer extends LivingEntityRenderer<ShopkeeperEntity, ChipModel<ShopkeeperEntity>>
{
	private static final ResourceLocation CHIP_SKIN_LOCATION = new ResourceLocation(ClockworkCalamityMod.MODID, "textures/entity/chip.png");

	public ChipRenderer(EntityRendererProvider.Context context)
	{
		super(context, new ChipModel<>(context.bakeLayer(ChipModel.MODEL_LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(ShopkeeperEntity shopkeeper)
	{
		return CHIP_SKIN_LOCATION;
	}
}

