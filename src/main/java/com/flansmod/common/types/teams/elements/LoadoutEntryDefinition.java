package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;

public class LoadoutEntryDefinition
{
	@JsonField
	public String paintjobName = "";
	@JsonField
	public String[] itemUnlocks = new String[0];
}
