package com.flansmod.client.render.guns;

import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class AttachmentItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final AttachmentItem Item;
	@Nonnull
	public AttachmentItemRenderer ItemRenderer;

	protected AttachmentItemClientExtension(@Nonnull AttachmentItem item)
	{
		Item = item;
		ItemRenderer = new AttachmentItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public AttachmentItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static AttachmentItemClientExtension of(@Nonnull AttachmentItem item)
	{
		AttachmentItemClientExtension clientExt = new AttachmentItemClientExtension(item);
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