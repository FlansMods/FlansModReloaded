package com.flansmod.common.types;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

public abstract class JsonDefinition
{
	public String GetLocationString() { return Location.toString(); }
	public ResourceLocation GetLocation() { return Location; }
	public boolean IsValid() { return !Location.getPath().contains("null"); }

	public ResourceLocation Location;
	public JsonDefinition(ResourceLocation srcLoc)
	{
		Location = srcLoc;
	}
	public abstract String GetTypeName();
	public void LoadExtra(JsonElement jRoot) {}

	@Override
	public int hashCode()
	{
		return Location.hashCode();
	}
	@Override
	public String toString() { return GetTypeName() + "@[" + Location.toString() + "]"; }
}
