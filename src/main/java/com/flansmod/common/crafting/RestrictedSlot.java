package com.flansmod.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RestrictedSlot extends Slot
{
	public RestrictedSlot(Container container, int index, int x, int y)
	{
		super(container, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return container.canPlaceItem(getSlotIndex(), stack);
	}
}
