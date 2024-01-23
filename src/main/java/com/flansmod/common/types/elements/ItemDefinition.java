package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;

import javax.annotation.Nonnull;

public class ItemDefinition
{
	@JsonField
	public int maxStackSize = 64;
	@JsonField
	public String[] tags = new String[0];

	public boolean Matches(@Nonnull String match)
	{
		if(match.isEmpty())
			return true;
		for(String tag : tags)
			if(tag.equals(match))
				return true;
		return false;
	}
}
