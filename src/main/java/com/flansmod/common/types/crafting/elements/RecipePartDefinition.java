package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.ERecipePart;

public class RecipePartDefinition
{
	@JsonField
	public ERecipePart part = ERecipePart.misc;
	@JsonField
	public TieredIngredientDefinition[] tieredIngredients = new TieredIngredientDefinition[0];
	@JsonField
	public IngredientDefinition[] additionalIngredients = new IngredientDefinition[0];
}
