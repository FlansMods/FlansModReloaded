package com.flansmod.common.types;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

public abstract class JsonDefinition
{
	public ResourceLocation GetLocation() { return data; }

	public ResourceLocation data;

	public JsonDefinition(ResourceLocation srcLoc)
	{
		data = srcLoc;
	}

	public abstract String GetTypeName();
	
	public void LoadExtra(JsonElement jRoot) {}

	@Override
	public int hashCode()
	{
		return data.hashCode();
	}
}
