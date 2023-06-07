package com.flansmod.common.types.parts.elements;

import com.flansmod.common.types.JsonField;

public class EngineDefinition
{
	@JsonField
	public float maxAcceleration = 1.0f;
	@JsonField
	public float maxDeceleration = 1.0f;

	@JsonField
	public float maxSpeed = 1.0f;


	// Fuel settings
	@JsonField
	public EFuelType fuelType = EFuelType.Creative;

	@JsonField
	public float fuelConsumptionRate = 1.0f;
	@JsonField
	public int solidFuelSlots = 0;
	@JsonField(Docs = "In millibuckets")
	public int liquidFuelCapacity = 1000;
	@JsonField
	public int batterySlots = 0;
	@JsonField
	public int FECapacity = 0;

}
