package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

import java.util.Map;

import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class VehicleControlOptionDefinition
{
	@JsonField
	public String key = "default";
	@JsonField
	public ResourceLocation controlScheme = InvalidLocation;
	@JsonField
	public String modalCheck = "";


	public boolean Passes(@Nonnull Map<String, String> modes)
	{
		if(modalCheck.isEmpty())
			return true;
		String[] split = modalCheck.split(":");
		if(split.length == 2)
		{
			String modalValue = modes.get(split[0]);
			if(modalValue != null)
			{
				return modalValue.equals(split[1]);
			}
		}
		return false;
	}
}
