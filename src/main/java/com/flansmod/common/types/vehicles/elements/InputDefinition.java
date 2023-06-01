package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.EPlayerInput;

public class InputDefinition
{
	@JsonField
	public EPlayerInput key = EPlayerInput.Jump;

	@JsonField
	public ArticulationInputDefinition[] articulations = new ArticulationInputDefinition[0];
	@JsonField
	public MountedGunInputDefinition[] guns = new MountedGunInputDefinition[0];
	@JsonField
	public DrivingInputDefinition[] driving = new DrivingInputDefinition[0];
	@JsonField
	public String switchVehicleMode = "";
}

