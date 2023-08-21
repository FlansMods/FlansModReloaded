package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nullable;

public class GunCraftingEntryDefinition
{
	@JsonField
	public ItemStackDefinition[] outputs = new ItemStackDefinition[0];
	@JsonField
	public RecipePartDefinition[] parts = new RecipePartDefinition[0];

	@Nullable
	public Either<TieredIngredientDefinition, IngredientDefinition> GetIngredient(int index)
	{
		for(RecipePartDefinition part : parts)
		{
			if(0 <= index && index < part.tieredIngredients.length)
				return Either.left(part.tieredIngredients[index]);
			index -= part.tieredIngredients.length;
			if(0 <= index && index < part.additionalIngredients.length)
				return Either.right(part.additionalIngredients[index]);
			index -= part.additionalIngredients.length;
		}
		return null;
	}
}
