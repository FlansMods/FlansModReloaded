package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemCollectionDefinition;

public class ArmourCraftingDefinition
{
	@JsonField
	public boolean isActive = false;
	@JsonField
	public ItemCollectionDefinition craftableArmour = new ItemCollectionDefinition();
	@JsonField
	public int FECostPerCraft = 0;
}
