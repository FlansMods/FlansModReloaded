package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;

public class ItemDefinition
{
	@JsonField
	public int maxStackSize = 64;
	@JsonField
	public String[] tags = new String[0];
}
