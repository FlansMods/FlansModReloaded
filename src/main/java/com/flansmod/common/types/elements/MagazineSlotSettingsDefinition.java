package com.flansmod.common.types.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.magazines.MagazineDefinition;

import java.util.ArrayList;
import java.util.List;

public class MagazineSlotSettingsDefinition
{
	private List<MagazineDefinition> MatchingMagazineReferences = null;
	public List<MagazineDefinition> GetMatchingMagazines()
	{
		if(MatchingMagazineReferences == null)
		{
			MatchingMagazineReferences = new ArrayList<>(matchByNames.length);
			FlansMod.MAGAZINES.RunOnMatches(
				(magazine) -> {
					// First check for exact name matches. These do not also need to pass tag checks
					for (String matchBulletName : matchByNames)
					{
						if(magazine.Location.equals(matchBulletName))
							return true;
					}
					// Then check the tags of this mag
					if(matchByTags.length > 0)
					{
						for (final String tag : matchByTags)
						{
							if (!magazine.HasTag(tag))
								return false;
						}
						return true;
					}
					return false;
				},
				(magazine) ->
				{
					if (!MatchingMagazineReferences.contains(magazine))
						MatchingMagazineReferences.add(magazine);
				});

		}
		return MatchingMagazineReferences;
	}

	@JsonField
	public String[] matchByNames = new String[0];

	@JsonField
	public String[] matchByTags = new String[0];

	@JsonField(Docs = "How many upgrades are needed to perform a swap (not including the ones associated to the mags themselves)")
	public int baseCostToSwap = 0;
}
