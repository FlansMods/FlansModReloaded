package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class MagazineSlotSettingsDefinition
{
	@JsonField
	public String[] matchByNames = new String[0];

	@JsonField
	public String[] matchByTags = new String[0];
}
