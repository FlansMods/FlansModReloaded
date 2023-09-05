package com.flansmod.common.types.npc.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class MerchantOfferDefinition
{
	@JsonField(Docs = "Relative to other offers from this merchant", Min = 0.0f)
	public float weighting = 1.0f;
	@JsonField(Docs = "The level at which this offer appears", Min = 0, Max = 10)
	public int merchantLevel = 0;

	@JsonField
	public IngredientDefinition[] inputs = new IngredientDefinition[0];
	@JsonField
	public ItemStackDefinition output = new ItemStackDefinition();
	@JsonField
	public float merchantXP = 1.0f;
}
