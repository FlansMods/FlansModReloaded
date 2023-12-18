package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;

public class ModeDefinition
{
	@JsonField
	public String key = "mode";
	@JsonField
	public String[] values = new String[] { "on", "off" };
	@JsonField
	public String defaultValue = "off";
}
