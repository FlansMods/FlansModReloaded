package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;

public class MountedGunInputDefinition
{
	@JsonField
	public String gunName = "";
	@JsonField(Docs = "If not set to toggle, it is instead only pressed for as long as you hold it")
	public boolean toggle = false;
}
