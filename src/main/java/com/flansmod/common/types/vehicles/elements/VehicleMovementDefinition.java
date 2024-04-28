package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;

public class VehicleMovementDefinition
{
	@JsonField
	public String name = "default";
	@JsonField
	public EVehicleMovementType type = EVehicleMovementType.Car;



	@JsonField
	public float maxAcceleration = 1.0f;
	@JsonField
	public float maxDecceleration = -1.0f;
	@JsonField
	public float maxSpeed = 100.0f;
	@JsonField
	public float liftForce = 10.0f;

}
