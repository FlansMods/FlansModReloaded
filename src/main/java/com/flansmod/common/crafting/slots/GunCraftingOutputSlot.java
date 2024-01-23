package com.flansmod.common.crafting.slots;

import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunCraftingOutputSlot extends RestrictedSlot
{
	@Nonnull
	public final WorkbenchMenuGunCrafting Workbench;
	public GunCraftingOutputSlot(@Nonnull WorkbenchMenuGunCrafting workbench,
								 @Nonnull Container container,
								 int index, int x, int y)
	{
		super(container, index, x, y);
		Workbench = workbench;
	}

	@Nonnull
	@Override
	public ItemStack remove(int count)
	{
		if(Workbench.BlockEntity.IsGunCraftingFullyValid())
		{
			// Craft action
			ItemStack output = getItem().copyWithCount(1);
			Workbench.BlockEntity.ConsumeGunCraftingInputs();
			getItem().setCount(getItem().getCount() - 1);
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		return false;
	}
}
