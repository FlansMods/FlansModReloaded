package com.flansmod.common.types.npc.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class MerchantOfferDefinition
{
	@JsonField(Docs = "Relative to other offers from this merchant", Min = 0.0f)
	public float weighting = 1.0f;
	@JsonField(Docs = "The level at which this offer appears", Min = 0, Max = 10)
	public int merchantLevel = 0;

	@JsonField
	public ItemStackDefinition[] inputs = new ItemStackDefinition[0];
	@JsonField
	public ItemStackDefinition output = new ItemStackDefinition();
	@JsonField
	public int maxUses = 1;
	@JsonField
	public int merchantXP = 1;
	@JsonField
	public float priceMultiplier = 1.0f;
	@JsonField
	public int demand = 0;
}
