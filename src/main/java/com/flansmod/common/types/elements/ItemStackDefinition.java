package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class ItemStackDefinition
{
	@JsonField
	public String item = "minecraft:air";
	@JsonField
	public int count = 0;
	@JsonField
	public int damage = 0;
	@JsonField
	public String tags = "{}";
}
