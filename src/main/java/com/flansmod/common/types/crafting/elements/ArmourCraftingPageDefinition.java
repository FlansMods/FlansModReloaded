package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class ArmourCraftingPageDefinition
{
	@JsonField
	public String name = "";
	@JsonField
	public ArmourCraftingEntryDefinition[] entries = new ArmourCraftingEntryDefinition[0];
}
