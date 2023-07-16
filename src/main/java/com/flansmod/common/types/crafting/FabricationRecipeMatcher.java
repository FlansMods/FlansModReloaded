package com.flansmod.common.types.crafting;

import com.flansmod.common.types.crafting.elements.GunCraftingEntryDefinition;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.elements.ItemStackDefinition;

import java.util.ArrayList;
import java.util.List;

public class FabricationRecipeMatcher
{
	public static class RecipePartMatcher
	{
		public final RecipePartDefinition Def;

		public RecipePartMatcher(RecipePartDefinition def)
		{
			Def = def;
		}


	}

	public static class IngredientMatcher
	{
		public final IngredientDefinition Def;
		public final List<EMaterialType.MaterialAndTier> MatchTiers = new ArrayList<>();


		public IngredientMatcher(IngredientDefinition ingredientDefinition)
		{
			Def = ingredientDefinition;
			if(Def.compareItemTags)
			{
				for (String tag : Def.materialTags)
				{
					EMaterialType.MaterialAndTier matTier = EMaterialType.Resolve(tag);
					if (matTier.IsValid())
					{
						MatchTiers.add(matTier);
					}
				}
			}
		}

		public boolean IsMaterialTierIngredient()
		{
			return MatchTiers.size() > 0;
		}
	}

	public final ItemStackDefinition[] outputs;
	public final List<RecipePartMatcher> partMatchers;

	public FabricationRecipeMatcher(GunCraftingEntryDefinition gunCraftingEntryDefinition)
	{
		outputs = gunCraftingEntryDefinition.outputs;
		partMatchers = new ArrayList<>();

		for(RecipePartDefinition partDef : gunCraftingEntryDefinition.parts)
		{
			RecipePartMatcher matcher = new RecipePartMatcher(partDef);
			partMatchers.add(matcher);
		}
	}

}
