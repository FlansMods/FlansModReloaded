package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class GunCraftingEntryDefinition
{
	@JsonField
	public ItemStackDefinition[] outputs = new ItemStackDefinition[0];
	@JsonField
	public IngredientDefinition[] ingredients = new IngredientDefinition[0];
	@JsonField
	public GunCraftingEntryDefinition[] children = new GunCraftingEntryDefinition[0];
}
