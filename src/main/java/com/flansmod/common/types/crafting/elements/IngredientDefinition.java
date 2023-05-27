package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class IngredientDefinition
{
	@JsonField
	public String itemName = "minecraft:air";
	@JsonField
	public int count = 1;
	@JsonField
	public boolean compareDamage = false;
	@JsonField
	public int damage = 0;
	@JsonField
	public boolean compareNBT = false;
	@JsonField
	public String[] requiredTags = new String[0];
	@JsonField
	public String[] disallowedTags = new String[0];
	@JsonField
	public boolean checkCapabilities = false;
	@JsonField
	public String[] requiredCapabilities = new String[0];
	@JsonField
	public String[] disallowedCapabilites = new String[0];
}
