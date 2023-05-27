package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;

public class EnergyBlockDefinition
{
	@JsonField
	public int maxFE = 0;
	@JsonField
	public int acceptFEPerTick = 0;
	@JsonField
	public int disperseFEPerTick = 0;
}
