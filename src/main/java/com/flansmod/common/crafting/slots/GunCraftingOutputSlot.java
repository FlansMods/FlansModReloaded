package com.flansmod.common.crafting.slots;

import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunCraftingOutputSlot extends RestrictedSlot
{
	@Nonnull
	public final WorkbenchMenuGunCrafting Menu;
	public GunCraftingOutputSlot(@Nonnull WorkbenchMenuGunCrafting menu,
								 @Nonnull Container container,
								 int index, int x, int y)
	{
		super(container, index, x, y);
		Menu = menu;
	}

	@Nonnull
	@Override
	public ItemStack remove(int count)
	{
		if(Menu.IsGunCraftingFullyValid())
		{
			// Craft action
			ItemStack output = getItem().copyWithCount(1);
			Menu.ConsumeGunCraftingInputs();
			getItem().setCount(getItem().getCount() - 1);
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		return stack.isEmpty();
	}
}
