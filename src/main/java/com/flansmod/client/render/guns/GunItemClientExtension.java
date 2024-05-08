package com.flansmod.client.render.guns;

import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final GunItem Item;
	@Nonnull
	public GunItemRenderer ItemRenderer;

	protected GunItemClientExtension(@Nonnull GunItem item)
	{
		Item = item;
		ItemRenderer = new GunItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public GunItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static GunItemClientExtension of(@Nonnull GunItem item)
	{
		GunItemClientExtension clientExt = new GunItemClientExtension(item);
		FlansModelRegistry.PreRegisterModel(clientExt::GetLocation);
		return clientExt;
	}

	@Override
	@Nullable
	public HumanoidModel.ArmPose getArmPose(@Nonnull LivingEntity entityLiving,
											@Nonnull InteractionHand hand,
											@Nonnull ItemStack itemStack)
	{
		if(itemStack.getItem() instanceof GunItem)
			return HumanoidModel.ArmPose.BOW_AND_ARROW;
		return HumanoidModel.ArmPose.ITEM;
	}

}