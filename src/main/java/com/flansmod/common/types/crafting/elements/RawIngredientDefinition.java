package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class RawIngredientDefinition
{
	@JsonField(Docs = "1 = nugget, 9 = ingot, 81 = block or similar ratios for other materials")
	public int amount;
	@JsonField
	public String materialName;
}
