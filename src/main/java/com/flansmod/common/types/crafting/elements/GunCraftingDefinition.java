package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class GunCraftingDefinition
{
	@JsonField
	public boolean isActive = false;
	@JsonField
	public GunCraftingPageDefinition[] pages = new GunCraftingPageDefinition[0];
	@JsonField
	public int FECostPerCraft = 0;

	public int GetMaxInputSlots()
	{
		int max = 0;
		for(GunCraftingPageDefinition page : pages)
		{
			for(GunCraftingEntryDefinition entry : page.entries)
			{
				int count = 0;
				for(RecipePartDefinition part : entry.parts)
				{
					count += part.tieredIngredients.length;
					count += part.additionalIngredients.length;
				}
				if(count > max)
					max = count;
			}
		}
		return max;
	}
}
