package com.flansmod.common.types.crafting;

import com.flansmod.common.crafting.MaterialMatcher;
import com.flansmod.common.types.Definitions;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class MaterialDefinitions extends Definitions<MaterialDefinition>
{
	private final HashMap<Integer, MaterialMatcher> MatcherCache = new HashMap<Integer, MaterialMatcher>();

	public MaterialDefinitions()
	{
		super(MaterialDefinition.FOLDER,
			MaterialDefinition.class,
			MaterialDefinition.INVALID,
			MaterialDefinition::new);
	}

	@Nonnull
	public MaterialMatcher GetMatcherForThisTier(EMaterialType materialType, int tier, boolean allowHigherTiers)
	{
		int hash = (materialType.ordinal() << 16) | (tier << 8) | (allowHigherTiers ? 1 : 0);
		if(MatcherCache.containsKey(hash))
		{
			return MatcherCache.get(hash);
		}
		else
		{
			List<MaterialDefinition> materials =
				Find((matDef) -> {
					if(allowHigherTiers)
					{
						return matDef.materialType == materialType && matDef.craftingTier >= tier;
					}
					else
					{
						return matDef.materialType == materialType && matDef.craftingTier == tier;
					}
				} );

			MaterialMatcher matcher = new MaterialMatcher(materials);
			MatcherCache.put(hash, matcher);
			return matcher;
		}
	}
}
