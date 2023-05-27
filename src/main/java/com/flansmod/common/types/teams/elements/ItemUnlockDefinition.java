package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;

public class ItemUnlockDefinition
{
	@JsonField
	public String name;
	@JsonField
	public ItemUnlockDefinition[] withExtras = new ItemUnlockDefinition[0];
}
