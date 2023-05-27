package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class ArmourCraftingEntryDefinition
{
	@JsonField
	public IngredientDefinition[] ingredients = new IngredientDefinition[0];
	@JsonField
	public ItemStackDefinition output = new ItemStackDefinition();
}
