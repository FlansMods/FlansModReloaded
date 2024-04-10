package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemCollectionDefinition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PartCraftingDefinition
{
	@JsonField
	public boolean isActive = false;

	@JsonField
	public int inputSlots = 8;
	@JsonField
	public int outputSlots = 8;
	@JsonField(Docs = "In seconds")
	public float timePerCraft = 1.0f;
	@JsonField
	public float FECostPerCraft = 0.0f;

	@JsonField
	public ItemCollectionDefinition craftableParts = new ItemCollectionDefinition();

	@Nonnull
	public List<ItemStack> GetAllOutputs()
	{
		return craftableParts.GetItemMatches();
	}

	private final HashMap<Level, List<PartFabricationRecipe>> RecipeCaches = new HashMap<>();
	@Nonnull
	public List<PartFabricationRecipe> GetAllRecipes(Level level)
	{
		List<ItemStack> allOutputs = GetAllOutputs();

		if(!RecipeCaches.containsKey(level))
		{
			List<PartFabricationRecipe> recipeCache = new ArrayList<>();
			if (level != null)
			{
				List<PartFabricationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(FlansMod.PART_FABRICATION_RECIPE_TYPE.get());
				for (PartFabricationRecipe recipe : allRecipes)
				{
					boolean allowed = false;
					for (ItemStack validOutput : allOutputs)
						if (ItemStack.isSameItem(validOutput, recipe.getResultItem(RegistryAccess.EMPTY)))
							allowed = true;
					if (allowed)
						recipeCache.add(recipe);
				}
			}
			recipeCache.sort((o1, o2) ->
			{
				if(o1 == null)
					return -1;
				if(o2 == null)
					return +1;
				String hash1 = o1.getResultItem(RegistryAccess.EMPTY).getItem().builtInRegistryHolder().key().location().toString();
				String hash2 = o2.getResultItem(RegistryAccess.EMPTY).getItem().builtInRegistryHolder().key().location().toString();
				int outputComparison = hash1.compareTo(hash2);
				if(outputComparison != 0)
					return outputComparison;

				return o1.getId().compareTo(o2.getId());
			});
			RecipeCaches.put(level, recipeCache);
		}
		return RecipeCaches.get(level);
	}


}
