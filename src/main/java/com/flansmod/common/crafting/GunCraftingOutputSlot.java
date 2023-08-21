package com.flansmod.common.crafting;

import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class GunCraftingOutputSlot extends RestrictedSlot
{
	public final WorkbenchMenu Workbench;
	public GunCraftingOutputSlot(WorkbenchMenu workbench, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Workbench = workbench;
	}

	@Override
	public ItemStack remove(int count)
	{
		// Craft action
		ItemStack output = getItem().copyWithCount(1);
		Workbench.ConsumeCraftingInputs();
		getItem().setCount(getItem().getCount() - 1);
		return output;
	}

	@Override
	public boolean mayPlace(ItemStack stack) { return false; }
}
