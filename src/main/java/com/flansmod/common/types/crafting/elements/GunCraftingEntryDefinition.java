package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class GunCraftingEntryDefinition
{
	@JsonField
	public IngredientDefinition[] ingredients = new IngredientDefinition[0];
	@JsonField
	public GunCraftingEntryDefinition[] children = new GunCraftingEntryDefinition[0];
}
