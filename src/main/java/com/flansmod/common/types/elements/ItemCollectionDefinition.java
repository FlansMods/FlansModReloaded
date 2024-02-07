package com.flansmod.common.types.elements;

import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.json.Json;
import java.util.ArrayList;
import java.util.List;

public class ItemCollectionDefinition
{
	@JsonField(Docs = "ItemID inclusions/exclusions - to ignore itemID, leave empty")
	public LocationFilterDefinition[] itemIDFilters = new LocationFilterDefinition[0];
	@JsonField(Docs = "ItemTag inclusions/exclusions - to ignore tags, leave empty")
	public LocationFilterDefinition[] itemTagFilters = new LocationFilterDefinition[0];
	@JsonField(Docs = "Material inclusions/exclusions - to ignore materials, leave empty")
	public MaterialFilterDefinition[] materialFilters = new MaterialFilterDefinition[0];


	private List<JsonDefinition> CachedDefinitions = null;
	@Nonnull
	public List<JsonDefinition> GetDefinitionMatches()
	{
		if(CachedDefinitions == null)
		{
			CachedDefinitions = new ArrayList<>();
			for(ItemStack stack : GetItemMatches())
				if(stack.getItem() instanceof FlanItem flanItem)
					CachedDefinitions.add(flanItem.Def());
		}
		return CachedDefinitions;
	}

	private List<ItemStack> CachedMatches = null;
	@Nonnull
	public List<ItemStack> GetItemMatches()
	{
		if(CachedMatches == null)
		{
			CachedMatches = new ArrayList<>();
			ForgeRegistries.ITEMS.forEach((item) -> {

				// ---------------------------------------------
				// Process inclusions first
				boolean included = false;
				for (LocationFilterDefinition idFilter : itemIDFilters)
				{
					if (idFilter.Allows(item.builtInRegistryHolder().key().location()))
					{
						included = true;
						break;
					}
				}
				for(LocationFilterDefinition tagFilter : itemTagFilters)
				{
					if(item.builtInRegistryHolder().tags().anyMatch((itemTag) -> { return tagFilter.Allows(itemTag.location()); }))
					{
						included = true;
						break;
					}
				}
				if(item instanceof PartItem partItem)
				{
					for(MaterialFilterDefinition matFilter : materialFilters)
					{
						if(matFilter.Allows(partItem.Def().material))
						{
							included = true;
							break;
						}
					}
				}
				if(!included)
					return;

				// ---------------------------------------------
				// Now process any exclusions
				for (LocationFilterDefinition idFilter : itemIDFilters)
					if(idFilter.Disallows(item.builtInRegistryHolder().key().location()))
						return;
				for(LocationFilterDefinition tagFilter : itemTagFilters)
					if(item.builtInRegistryHolder().tags().anyMatch((itemTag) -> { return tagFilter.Disallows(itemTag.location()); }))
						return;
				if(item instanceof PartItem partItem)
					for(MaterialFilterDefinition matFilter : materialFilters)
						if(matFilter.Disallows(partItem.Def().material))
							return;

				// We passed inclusion and exclusion checks, add the item
				CachedMatches.add(new ItemStack(item));
			});
		}
		return CachedMatches;
	}
}
