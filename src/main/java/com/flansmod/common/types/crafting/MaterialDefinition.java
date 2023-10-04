package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.MaterialSourceDefinition;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MaterialDefinition extends JsonDefinition
{
	public static final MaterialDefinition INVALID = new MaterialDefinition(new ResourceLocation(FlansMod.MODID, "materials/null"));
	public static final String TYPE = "material";
	public static final String FOLDER = "materials";
	@Override
	public String GetTypeName() { return TYPE; }

	public MaterialDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public MaterialSourceDefinition[] sources = new MaterialSourceDefinition[0];
	@JsonField
	public int craftingTier = 1;
	@JsonField
	public EMaterialType materialType = EMaterialType.Misc;


	private List<MaterialSourceDefinition> SortedByValue = null;
	public List<MaterialSourceDefinition> GetSortedByValue()
	{
		if(SortedByValue == null)
		{
			SortedByValue = new ArrayList<>(sources.length);
			SortedByValue.addAll(Arrays.asList(sources));
			SortedByValue.sort(Comparator.comparingInt(a -> a.count));
		}
		return SortedByValue;
	}


	public List<Pair<MaterialSourceDefinition, Integer>> ResolveAmount(int count)
	{
		List<Pair<MaterialSourceDefinition, Integer>> list = new ArrayList<>(sources.length);
		List<MaterialSourceDefinition> sortedSources = GetSortedByValue();
		for(int i = sortedSources.size() - 1; i >= 0; i--)
		{
			int numOfType = count / sortedSources.get(i).count;
			if (numOfType > 0)
			{
				list.add(Pair.of(sortedSources.get(i), numOfType));
				count -= numOfType * sortedSources.get(i).count;
			}
		}
		return list;
	}

	public String GenerateString(int count)
	{
		List<MaterialSourceDefinition> sortedSources = GetSortedByValue();
		if(sortedSources.size() == 0)
			return "0";
		if(count == 0)
			return "0" + sortedSources.get(0).icon.GetIcon();

		StringBuilder builder = new StringBuilder();
		for(int i = sortedSources.size() - 1; i >= 0; i--)
		{
			int numOfType = count / sortedSources.get(i).count;
			if (numOfType > 0)
			{
				count -= numOfType * sortedSources.get(i).count;

				// n[symbol]_
				builder.append(numOfType);
				builder.append(sortedSources.get(i).icon.GetIcon());
				if(i > 0 && count > 0)
					builder.append(" ");
			}
		}
		return builder.toString();
	}
}
