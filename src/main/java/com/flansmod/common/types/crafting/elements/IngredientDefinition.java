package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.item.ItemStack;

public class IngredientDefinition
{
	@JsonField
	public int count = 1;


	@JsonField
	public boolean compareItemName = true;
	@JsonField
	public String itemName = "minecraft:air";

	@JsonField
	public boolean compareDamage = false;
	@JsonField(Min = 0)
	public int minAllowedDamage = 0;
	@JsonField(Min = 0)
	public int maxAllowedDamage = 0;

	@JsonField
	public boolean compareItemTags = false;
	@JsonField
	public String[] requiredTags = new String[0];
	@JsonField
	public String[] materialTags = new String[0];
	@JsonField
	public String[] disallowedTags = new String[0];

	@JsonField
	public boolean compareNBT = false;
	@JsonField
	public String[] requiredNBT = new String[0];
	@JsonField
	public String[] disallowedNBT = new String[0];
}
