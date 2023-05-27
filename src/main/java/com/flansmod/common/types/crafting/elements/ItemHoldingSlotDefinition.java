package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class ItemHoldingSlotDefinition
{
	@JsonField
	public String name = "";
	@JsonField
	public int stackSize = 1;
	@JsonField
	public String allow = "";
}
