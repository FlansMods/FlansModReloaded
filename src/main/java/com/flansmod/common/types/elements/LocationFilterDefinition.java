package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nonnull;

public class LocationFilterDefinition
{
	@JsonField
	public EFilterType filterType = EFilterType.Allow;
	@JsonField
	public ResourceLocation[] matchResourceLocations = new ResourceLocation[0];

	public boolean Allows(@Nonnull ResourceLocation test)
	{
		if(filterType == EFilterType.Allow)
			for(ResourceLocation match : matchResourceLocations)
				if(match.equals(test))
					return true;
		return false;
	}

	public boolean Disallows(@Nonnull ResourceLocation test)
	{
		if(filterType == EFilterType.Disallow)
			for(ResourceLocation match : matchResourceLocations)
				if(match.equals(test))
					return true;
		return false;
	}


	public boolean Matches(@Nonnull ResourceLocation test)
	{
		for(ResourceLocation match : matchResourceLocations)
			if(match.equals(test))
				return true;
		return false;
	}
}
