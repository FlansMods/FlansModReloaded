package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
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
	private List<Item> MatchItems = null;

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
				for(String itemId : matDef.matchItems)
				{
					ResourceLocation itemResLoc = ResourceLocation.tryParse(itemId);
					if(itemResLoc != null)
					{
						Item item = ForgeRegistries.ITEMS.getValue(itemResLoc);
						if(item != null)
						{
							if(!MatchItems.contains(item))
								MatchItems.add(item);
						}
						else FlansMod.LOGGER.warn("Failed to find item with ResourceLocation " + itemResLoc + " for MaterialDefinition");
					}
					else FlansMod.LOGGER.warn("Failed to parse item ResourceLocation " + itemId + " for MaterialDefinition");
				}

				List<TagKey<Item>> tagKeys = new ArrayList<>();
				for(String tag : matDef.matchTags)
				{
					ResourceLocation tagResLoc = ResourceLocation.tryParse(tag);
					if(tagResLoc != null)
					{
						tagKeys.add(ItemTags.create(tagResLoc));
					}
					else FlansMod.LOGGER.warn("Failed to parse tag ResourceLocation " + tag + " for MaterialDefinition");
				}

				for(var kvp : ForgeRegistries.ITEMS.getEntries())
				{
					for(TagKey<Item> tagKey : tagKeys)
					{
						if(kvp.getValue().builtInRegistryHolder().is(tagKey) && !MatchItems.contains(kvp.getValue()))
							MatchItems.add(kvp.getValue());
					}
				}
			}
			else FlansMod.LOGGER.warn("MaterialDefinition " + matDef.data + " was invalid");
		}
	}


	public boolean Matches(ItemStack stack)
	{
		if(MatchItems == null)
			InitializeCache();

		for(Item item : MatchItems)
			if(stack.is(item))
				return true;

		return false;
	}
}
