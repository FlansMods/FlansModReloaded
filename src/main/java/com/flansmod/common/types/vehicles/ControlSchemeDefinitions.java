package com.flansmod.common.types.vehicles;

import com.flansmod.common.types.Definitions;

public class ControlSchemeDefinitions extends Definitions<ControlSchemeDefinition>
{
	public ControlSchemeDefinitions()
	{
		super(ControlSchemeDefinition.FOLDER,
			  ControlSchemeDefinition.class,
			  ControlSchemeDefinition.INVALID,
			  ControlSchemeDefinition::new);
	}
}
