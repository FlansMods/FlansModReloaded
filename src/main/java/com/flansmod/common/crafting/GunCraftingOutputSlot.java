package com.flansmod.common.crafting;

import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class GunCraftingOutputSlot extends RestrictedSlot
{

	public GunCraftingOutputSlot(Container container, int index, int x, int y)
	{
		super(container, index, x, y);
	}

	@Override
	public ItemStack remove(int count)
	{
		// Craft action




		return ItemStack.EMPTY;
	}

	@Override
	public boolean mayPlace(ItemStack stack) { return false; }
}
