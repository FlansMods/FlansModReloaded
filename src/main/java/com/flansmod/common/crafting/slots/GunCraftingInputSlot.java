package com.flansmod.common.crafting.slots;

import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunCraftingInputSlot extends RestrictedSlot
{
	private final WorkbenchMenuGunCrafting Menu;
	public GunCraftingInputSlot(WorkbenchMenuGunCrafting menu, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Menu = menu;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		if(!isActive())
			return false;
		if(stack.isEmpty())
			return true;

		GunFabricationRecipe gunRecipe = Menu.GetSelectedGunRecipe();
		if(gunRecipe == null)
			return false;

		int slotIndex = getSlotIndex();
		if(slotIndex >= gunRecipe.InputIngredients.size())
			return false;

		return gunRecipe.InputIngredients.get(slotIndex).test(stack);
	}
	@Override
	public void set(@Nonnull ItemStack stack)
	{
		super.set(stack);
		Menu.UpdateGunCraftingOutputSlot();
	}
}
