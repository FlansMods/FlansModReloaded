package com.flansmod.common.crafting.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RestrictedSlot extends Slot
{
	private boolean Active = true;

	public RestrictedSlot(Container container, int index, int x, int y)
	{
		super(container, index, x, y);
	}

	public void SetActive(boolean bActive)
	{
		Active = bActive;
	}

	@Override
	public boolean isActive() {
		return Active;
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return container.canPlaceItem(getSlotIndex(), stack);
	}
}
