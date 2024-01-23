package com.flansmod.common.crafting.ingredients;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.AbstractIngredient;

import javax.annotation.Nullable;

public abstract class StackedIngredient extends AbstractIngredient
{
	public final int Count;
	public StackedIngredient(int count)
	{
		Count = count;
	}

	@Override
	public boolean test(@Nullable ItemStack target)
	{
		return Count(target) >= Count;
	}

	public abstract int Count(@Nullable ItemStack target);
}
