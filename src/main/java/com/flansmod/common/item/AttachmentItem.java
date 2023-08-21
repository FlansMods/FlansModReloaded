package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class AttachmentItem extends FlanItem
{
	@Override
	public AttachmentDefinition Def() { return FlansMod.ATTACHMENTS.Get(DefinitionLocation); }

	public AttachmentItem(ResourceLocation defLoc, Properties properties)
	{
		super(defLoc, properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(FlanClientItemExtensions.create(this, new AttachmentItemRenderer()));
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }
}
