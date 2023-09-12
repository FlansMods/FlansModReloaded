package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class MaterialSourceDefinition
{
	@JsonField
	public String[] matchItems = new String[0];
	@JsonField
	public String[] matchTags = new String[0];
	@JsonField(Docs = "For ref, nugget = 1, ingot = 9, block = 81")
	public int count = 1;
}
