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
	@JsonField
	public int numBatterySlots = 0;
	@JsonField
	public int batterySlotStackSize = 1;

	@JsonField(Docs = "In millibuckets")
	public int liquidFuelStorage = 0;
	@JsonField
	public String liquidFuelFilter = "";
	@JsonField
	public int liquidFEPerMillibucket = 0;

	@JsonField
	public int numSolidFuelSlots = 0;
	@JsonField
	public String solidFuelFilter = "";
	@JsonField
	public int solidFEPerFuelTime = 0;
}
