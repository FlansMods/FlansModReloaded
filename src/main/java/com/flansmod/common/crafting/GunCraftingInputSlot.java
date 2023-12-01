package com.flansmod.common.crafting;

import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.crafting.elements.GunCraftingEntryDefinition;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.crafting.elements.TieredIngredientDefinition;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunCraftingInputSlot extends RestrictedSlot
{
	private final WorkbenchMenu Menu;
	public GunCraftingInputSlot(WorkbenchMenu menu, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Menu = menu;
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		if(!isActive())
			return false;
		if(stack.isEmpty())
			return true;

		Either<TieredIngredientDefinition, IngredientDefinition> ingredientDef = GetIngredientDef();
		if(ingredientDef != null)
		{
			boolean canPlace = false;
			if(ingredientDef.left().isPresent())
				canPlace = ingredientDef.left().get().Matches(stack);
			if(ingredientDef.right().isPresent())
				canPlace = ingredientDef.right().get().Matches(stack);
			return canPlace;
		}

		return true;
	}
	@Nullable
	public Either<TieredIngredientDefinition, IngredientDefinition> GetIngredientDef()
	{
		GunCraftingEntryDefinition entry = Menu.GetSelectedGunCraftingEntry();
		if(entry != null)
		{
			int inputSlotIndex = 0;
			for (RecipePartDefinition part : entry.parts)
			{
				for (TieredIngredientDefinition tiered : part.tieredIngredients)
				{
					if (inputSlotIndex == getSlotIndex())
						return Either.left(tiered);

					inputSlotIndex++;
				}
				for (IngredientDefinition specific : part.additionalIngredients)
				{
					if (inputSlotIndex == getSlotIndex())
						return Either.right(specific);

					inputSlotIndex++;
				}
			}
		}
		return null;
	}

	@Override
	public void set(@Nonnull ItemStack stack)
	{
		super.set(stack);
		Menu.UpdateGunCraftingOutput();
		Menu.SwitchToGunCrafting();
	}
}
