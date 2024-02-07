package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialIconType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MaterialSourceDefinition
{
	@JsonField
	public String[] matchItems = new String[0];
	@JsonField
	public String[] matchTags = new String[0];
	@JsonField(Docs = "For ref, nugget = 1, ingot = 9, block = 81")
	public int count = 1;

	@JsonField
	public EMaterialIconType icon = EMaterialIconType.ingot;

	private List<ItemStack> Matches = null;
	@Nonnull
	public List<ItemStack> GetMatches()
	{
		if(Matches == null)
		{
			Matches = new ArrayList<>();
			List<ResourceLocation> matchItemLocs = new ArrayList<>(matchItems.length);
			List<TagKey<Item>> matchTagKeys = new ArrayList<>(matchTags.length);
			// Check for items by name first
			for(String name : matchItems)
				matchItemLocs.add(new ResourceLocation(name));
			for(String tag : matchTags)
				matchTagKeys.add(TagKey.create(Registries.ITEM, new ResourceLocation(tag)));


			for(var kvp : ForgeRegistries.ITEMS.getEntries())
			{
				boolean isMatch = false;
				if(matchItemLocs.contains(kvp.getKey().location()))
					isMatch = true;
				for(TagKey<Item> tagKey : matchTagKeys)
					if(kvp.getValue().builtInRegistryHolder().is(tagKey))
						isMatch = true;

				if(isMatch)
					Matches.add(new ItemStack(kvp.getValue()));
			}
		}
		return Matches;
	}


	public int AnalyzeStack(ItemStack stack)
	{
		for(ItemStack checkMatch : GetMatches())
		{
			if(ItemStack.isSameItem(stack, checkMatch))
				return count * stack.getCount();
		}
		return 0;
	}
}
