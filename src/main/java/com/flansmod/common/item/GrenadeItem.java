package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.grenades.GrenadeDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GrenadeItem extends FlanItem
{
	private ResourceLocation definitionLocation;
	public GrenadeDefinition Def() { return FlansMod.GRENADES.get(definitionLocation); }

	public GrenadeItem(ResourceLocation defLoc, Properties properties)
	{
		super(properties);

		definitionLocation = defLoc;
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }
}
