package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.EPlayerInput;

public class InputDefinition
{
	@JsonField
	public EPlayerInput key = EPlayerInput.Jump;

	@JsonField
	public ArticulationInputDefinition[] articulations = new ArticulationInputDefinition[0];
	@JsonField
	public MountedGunInputDefinition[] guns = new MountedGunInputDefinition[0];
	@JsonField
	public ArmInputDefinition[] arms = new ArmInputDefinition[0];

	@JsonField(Docs = "If true, each articulation/gun/driving control will be triggered in order. If false, one press = activate all")
	public boolean alternateInputs = false;
	@JsonField
	public String switchVehicleMode = "";
}

