package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class GunCraftingDefinition
{
	@JsonField
	public boolean isActive = false;
	@JsonField
	public GunCraftingPageDefinition[] pages = new GunCraftingPageDefinition[0];
	@JsonField
	public int FECostPerCraft = 0;
}
