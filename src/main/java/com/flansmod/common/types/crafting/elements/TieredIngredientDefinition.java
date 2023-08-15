package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialType;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nonnull
	public Component GetAllowedMaterialsComponent()
	{
		if(allowedMaterials.length == 0)
			return Component.translatable("crafting.match_any_material");
		else if (allowedMaterials.length == 1)
			return Component.translatable("crafting.match_single", allowedMaterials[0].ToComponent());
		else
		{
			Object[] varargs = new Object[allowedMaterials.length];
			for(int n = 0; n < allowedMaterials.length; n++)
				varargs[n] = allowedMaterials[n].ToComponent();
			return Component.translatable("crafting.match_multiple." + allowedMaterials.length, varargs);
		}
	}
	@Nonnull
	public Component GetAllowedTiersComponent()
	{
		int high = Integer.MIN_VALUE;
		int low = Integer.MAX_VALUE;
		for(int i = 0; i < allowedTiers.length; i++)
		{
			if(allowedTiers[i] > high)
				high = allowedTiers[i];
			if(allowedTiers[i] < low)
				low = allowedTiers[i];
		}

		int range = high - low;
		// Small range, check all the numbers are there
		if(0 <= range && range < 10)
		{
			if(allowedTiers.length == range + 1)
			{
				if(low == 1)
				{
					return Component.translatable("crafting.match_tiers_above", low);
				}
				return Component.translatable("crafting.match_tiers_between", low, high);
			}
			else
			{
				StringBuilder builder = new StringBuilder();
				for(int n = 0; n < allowedTiers.length; n++)
				{
					builder.append(allowedTiers[n]);
					if(n < allowedTiers.length - 1)
						builder.append(", ");
				}

				return Component.translatable("crafting.match_mixed_tiers", builder.toString());
			}
		}

		return Component.translatable("crafting.match_tiers_above", low);
	}
}
