package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.common.types.vehicles.EVehicleAxis;

public class DrivingInputDefinition
{
	@JsonField
	public float force = 1.0f;
	@JsonField
	public boolean toggle = false;
}
