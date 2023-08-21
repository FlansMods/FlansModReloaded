package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class IngredientDefinition
{
	@JsonField
	public int count = 1;


	@JsonField
	public boolean compareItemName = true;
	@JsonField
	public String itemName = "minecraft:air";

	@JsonField
	public boolean compareDamage = false;
	@JsonField(Min = 0)
	public int minAllowedDamage = 0;
	@JsonField(Min = 0)
	public int maxAllowedDamage = 0;

	@JsonField
	public boolean compareItemTags = false;
	@JsonField
	public String[] requiredTags = new String[0];
	@JsonField
	public String[] materialTags = new String[0];
	@JsonField
	public String[] disallowedTags = new String[0];

	@JsonField
	public boolean compareNBT = false;
	@JsonField
	public String[] requiredNBT = new String[0];
	@JsonField
	public String[] disallowedNBT = new String[0];

	public void GenerateMatches(List<ItemStack> potentialMatches)
	{
		if (compareItemName)
		{
			try
			{
				ResourceLocation resLoc = new ResourceLocation(itemName);
				Item item = ForgeRegistries.ITEMS.getValue(resLoc);
				potentialMatches.add(new ItemStack(item, count));
			} catch (Exception e)
			{
				FlansMod.LOGGER.error("Failed to match ingredient " + itemName);
			}
		}
		else
		{
			try
			{
				List<TagKey<Item>> requiredItemTags = new ArrayList<>();
				for (String tag : requiredTags)
					requiredItemTags.add(ItemTags.create(new ResourceLocation(tag)));
				List<TagKey<Item>> disallowedItemTags = new ArrayList<>();
				for (String tag : disallowedTags)
					disallowedItemTags.add(ItemTags.create(new ResourceLocation(tag)));

				for (var kvp : ForgeRegistries.ITEMS.getEntries())
				{
					if (compareItemTags)
					{
						boolean passes = true;
						for(TagKey<Item> tag : disallowedItemTags)
							if(kvp.getValue().builtInRegistryHolder().is(tag))
								passes = false;

						for(TagKey<Item> tag : requiredItemTags)
							if(!kvp.getValue().builtInRegistryHolder().is(tag))
								passes = false;

						if(passes)
						{
							potentialMatches.add(new ItemStack(kvp.getValue(), count));
						}
					}
				}
			}
			catch(Exception e)
			{
				FlansMod.LOGGER.error("Failed to match ingredient by tags");
			}
		}
	}

	public boolean Matches(ItemStack stack)
	{
		if(stack.getCount() < count)
			return false;
		if(compareItemName)
		{
			if(!stack.getItem().builtInRegistryHolder().is(new ResourceLocation(itemName)))
				return false;
		}
		if(compareItemTags)
		{
			for (String tag : requiredTags)
				if(!stack.getItem().builtInRegistryHolder().is(ItemTags.create(new ResourceLocation(tag))))
					return false;
			for (String tag : disallowedTags)
				if(stack.getItem().builtInRegistryHolder().is(ItemTags.create(new ResourceLocation(tag))))
					return false;

		}
		return true;

	}
}
