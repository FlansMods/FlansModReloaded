package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PartCraftingDefinition
{
	@JsonField
	public boolean isActive = false;

	@JsonField
	public String[] partsByName = new String[0];

	@JsonField
	public TieredIngredientDefinition[] partsByTier = new TieredIngredientDefinition[0];

	private List<PartDefinition> Parts = null;
	public List<PartDefinition> GetPartList()
	{
		if(Parts == null)
		{
			Parts = FlansMod.PARTS.Find((part) ->
			{
				for (String name : partsByName)
				{
					ResourceLocation resLoc = new ResourceLocation(name);
					if (part.Location.equals(resLoc))
						return true;
				}
				for(TieredIngredientDefinition tieredDef : partsByTier)
				{
					if(tieredDef.Matches(part))
						return true;
				}
				return false;
			});
		}
		return Parts;
	}
}
