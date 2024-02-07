package com.flansmod.common.types.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.MaterialDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MaterialFilterDefinition
{
	@JsonField
	public EFilterType filterType = EFilterType.Allow;

	@JsonField
	public EMaterialType[] materialTypes = new EMaterialType[0];
	@JsonField
	public int minTier = 1;
	@JsonField
	public int maxTier = 5;

	public boolean Allows(@Nonnull ResourceLocation materialLoc)
	{
		if(filterType == EFilterType.Allow)
			for(MaterialDefinition match : GetMatches())
				if(match.Location.equals(materialLoc))
					return true;
		return false;
	}

	public boolean Disallows(@Nonnull ResourceLocation materialLoc)
	{
		if(filterType == EFilterType.Disallow)
			for(MaterialDefinition match : GetMatches())
				if(match.Location.equals(materialLoc))
					return true;
		return false;
	}

	public boolean Matches(@Nonnull ResourceLocation materialLoc)
	{
		for(MaterialDefinition match : GetMatches())
			if(match.Location.equals(materialLoc))
				return true;
		return false;
	}


	public boolean is(@Nonnull EMaterialType matType)
	{
		for(EMaterialType allowed : materialTypes)
			if(allowed == matType)
				return true;
		return false;
	}

	@Nullable
	private List<MaterialDefinition> MaterialCache = null;
	@Nonnull
	public List<MaterialDefinition> GetMatches()
	{
		if(MaterialCache == null)
		{
			MaterialCache = FlansMod.MATERIALS.Find((mat) ->
				mat.IsValid()
				&& mat.craftingTier >= minTier
				&& mat.craftingTier <= maxTier
				&& this.is(mat.materialType));
		}
		return MaterialCache;
	}
}
