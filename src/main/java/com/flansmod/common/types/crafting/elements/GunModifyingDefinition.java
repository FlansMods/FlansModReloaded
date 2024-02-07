package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

public class GunModifyingDefinition
{
	@JsonField
	public boolean isActive = false;

	@JsonField(Docs = "Disallows certain mods, but only if size > 0")
	public ResourceLocation[] disallowedMods = new ResourceLocation[0];
	@JsonField(Docs = "Allows only certain mods if set. If size == 0, nothing will be applied")
	public ResourceLocation[] allowedMods = new ResourceLocation[0];

	@JsonField
	public int FECostPerModify = 0;

}
