package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class ArmourCraftingDefinition
{
	@JsonField
	public boolean isActive = false;
	@JsonField
	public ArmourCraftingPageDefinition[] pages = new ArmourCraftingPageDefinition[0];
	@JsonField
	public int FECostPerCraft = 0;
}
