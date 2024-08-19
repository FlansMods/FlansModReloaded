package com.flansmod.common.types.vehicles;

import com.flansmod.common.types.Definitions;

public class VehicleDefinitions extends Definitions<VehicleDefinition>
{
	public VehicleDefinitions()
	{
		super(VehicleDefinition.FOLDER,
			  VehicleDefinition.class,
			  VehicleDefinition.INVALID,
			  VehicleDefinition::new);
	}
}
