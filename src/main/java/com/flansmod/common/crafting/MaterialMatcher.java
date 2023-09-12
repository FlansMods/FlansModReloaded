package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.elements.MaterialSourceDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class MaterialMatcher
{
	public final List<MaterialDefinition> MaterialDefs;

	private class MaterialMatchInfo
	{
		public Item Item;
		public int Count;

		public MaterialMatchInfo(Item item, int count)
		{
			Item = item;
			Count = count;
		}
	}
	private List<MaterialMatchInfo> MatchItems = null;

	public MaterialMatcher(List<MaterialDefinition> materialDefs)
	{
		MaterialDefs = materialDefs;
	}

	private void InitializeCache()
	{
		MatchItems = new ArrayList<>();
		for(MaterialDefinition matDef : MaterialDefs)
		{
			if(matDef.IsValid())
			{
				for(MaterialSourceDefinition source : matDef.sources)
				{
					for (String itemId : source.matchItems)
					{
						ResourceLocation itemResLoc = ResourceLocation.tryParse(itemId);
						if (itemResLoc != null)
						{
							Item item = ForgeRegistries.ITEMS.getValue(itemResLoc);
							if (item != null)
							{
								if (!MatchItems.contains(item))
									MatchItems.add(new MaterialMatchInfo(item, source.count));
							} else
								FlansMod.LOGGER.warn("Failed to find item with ResourceLocation " + itemResLoc + " for MaterialDefinition");
						} else
							FlansMod.LOGGER.warn("Failed to parse item ResourceLocation " + itemId + " for MaterialDefinition");
					}

					List<TagKey<Item>> tagKeys = new ArrayList<>();
					for (String tag : source.matchTags)
					{
						ResourceLocation tagResLoc = ResourceLocation.tryParse(tag);
						if (tagResLoc != null)
						{
							tagKeys.add(ItemTags.create(tagResLoc));
						} else
							FlansMod.LOGGER.warn("Failed to parse tag ResourceLocation " + tag + " for MaterialDefinition");
					}

					for (var kvp : ForgeRegistries.ITEMS.getEntries())
					{
						for (TagKey<Item> tagKey : tagKeys)
						{
							if (kvp.getValue().builtInRegistryHolder().is(tagKey) && !MatchItems.contains(kvp.getValue()))
								MatchItems.add(new MaterialMatchInfo(kvp.getValue(), source.count));
						}
					}
				}
			}
			else FlansMod.LOGGER.warn("MaterialDefinition " + matDef.GetLocationString() + " was invalid");
		}
	}


	public int Matches(ItemStack stack)
	{
		if(MatchItems == null)
			InitializeCache();

		for(MaterialMatchInfo kvp : MatchItems)
			if(stack.is(kvp.Item))
				return kvp.Count;

		return 0;
	}
}
