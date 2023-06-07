package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class ItemUnlockDefinition
{
	@JsonField
	public String name;
	@JsonField
	public ItemStackDefinition[] items = new ItemStackDefinition[0];
}
