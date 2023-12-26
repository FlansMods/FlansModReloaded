package com.flansmod.common.crafting;

import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunCraftingOutputSlot extends RestrictedSlot
{
	public final WorkbenchMenuGunCrafting Workbench;
	public GunCraftingOutputSlot(WorkbenchMenuGunCrafting workbench, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Workbench = workbench;
	}

	@Nonnull
	@Override
	public ItemStack remove(int count)
	{
		// Craft action
		ItemStack output = getItem().copyWithCount(1);
		Workbench.ConsumeGunCraftingInputs();
		getItem().setCount(getItem().getCount() - 1);
		return output;
	}

	@Override
	public boolean mayPlace(ItemStack stack) { return false; }
}
