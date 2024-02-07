package com.flansmod.common.types;

import com.flansmod.common.FlansMod;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class JsonDefinition
{
	public static ResourceLocation InvalidLocation = new ResourceLocation(FlansMod.MODID, "null");
	public static boolean IsValidLocation(@Nonnull ResourceLocation loc) { return !loc.equals(InvalidLocation); }

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
