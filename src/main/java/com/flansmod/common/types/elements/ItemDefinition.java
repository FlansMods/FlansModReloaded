package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ItemDefinition
{
	@JsonField
	public int maxStackSize = 64;
	@JsonField(AssetPathHint = "tags/items/")
	public ResourceLocation[] tags = new ResourceLocation[0];

	public boolean Matches(@Nonnull ResourceLocation match)
	{
		if(!JsonDefinition.IsValidLocation(match))
			return true;
		for(ResourceLocation tag : tags)
			if(tag.equals(match))
				return true;
		return false;
	}
}
