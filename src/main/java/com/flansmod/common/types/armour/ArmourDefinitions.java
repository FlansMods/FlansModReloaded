package com.flansmod.common.types.armour;

import com.flansmod.common.types.Definitions;

public class ArmourDefinitions extends Definitions<ArmourDefinition>
{
	public ArmourDefinitions()
	{
		super(ArmourDefinition.FOLDER,
			ArmourDefinition.class,
			ArmourDefinition.INVALID,
			ArmourDefinition::new);
	}
}
