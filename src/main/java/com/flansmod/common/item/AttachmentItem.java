package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AttachmentItem extends FlanItem
{
	private ResourceLocation definitionLocation;
	public AttachmentDefinition Def() { return FlansMod.ATTACHMENTS.Get(definitionLocation); }


	public AttachmentItem(ResourceLocation defLoc, Properties properties)
	{
		super(properties);

		definitionLocation = defLoc;
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }
}
