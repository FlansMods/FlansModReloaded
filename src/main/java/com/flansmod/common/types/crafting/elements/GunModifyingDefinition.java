package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class GunModifyingDefinition
{
	@JsonField
	public boolean isActive = false;

	@JsonField(Docs = "Disallows certain mods, but only if size > 0")
	public String[] disallowedMods = new String[0];
	@JsonField(Docs = "Allows only certain mods if set. If size == 0, nothing will be applied")
	public String[] allowedMods = new String[0];

	@JsonField
	public int FECostPerModify = 0;

}
