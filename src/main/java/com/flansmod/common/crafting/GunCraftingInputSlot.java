package com.flansmod.common.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class GunCraftingInputSlot extends RestrictedSlot
{
	private final WorkbenchMenu Menu;
	public GunCraftingInputSlot(WorkbenchMenu menu, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Menu = menu;
	}

	@Override
	public void set(ItemStack stack)
	{
		super.set(stack);
		Menu.UpdateCraftingOutput();
		Menu.SwitchToGunCrafting();
	}
}
