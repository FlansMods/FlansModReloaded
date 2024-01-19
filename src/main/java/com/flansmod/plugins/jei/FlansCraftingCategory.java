package com.flansmod.plugins.jei;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import javax.annotation.Nonnull;

public abstract class FlansCraftingCategory<T> implements IRecipeCategory<T>
{
	private final RecipeType<T> Type;
	public FlansCraftingCategory(RecipeType<T> recipeType)
	{
		Type = recipeType;
	}

	@Override
	@Nonnull
	public RecipeType<T> getRecipeType() { return Type; }
}