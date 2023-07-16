package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialType;

public class TieredIngredientDefinition
{
	@JsonField(Docs = "Tag will be of the format 'flansmod:items/wing'")
	public String tag;
	@JsonField
	public int[] allowedTiers = new int[] { 1, 2, 3 };
	@JsonField
	public EMaterialType[] allowedMaterials = new EMaterialType[] { EMaterialType.Misc };

	public int GetLowestAllowedTier()
	{
		int lowest = Integer.MAX_VALUE;
		for (int allowedTier : allowedTiers)
			if (allowedTier < lowest)
				lowest = allowedTier;
		return lowest;
	}
}
