package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.EDrivingControl;

public class DrivingInputDefinition
{
	@JsonField
	public EDrivingControl control = EDrivingControl.Brake;
	@JsonField
	public float force = 1.0f;
	@JsonField
	public boolean toggle = false;
}
