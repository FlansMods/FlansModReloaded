package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class PaintableDefinition
{
	@JsonField
	public PaintjobDefinition[] paintjobs = new PaintjobDefinition[0];

	public static final PaintableDefinition Invalid = new PaintableDefinition();
	public boolean IsValid()
	{
		return paintjobs.length > 0;
	}
}
