package com.flansmod.common.types.abilities;

import com.flansmod.common.types.Definitions;

public class AbilityDefinitions extends Definitions<AbilityDefinition>
{
	public AbilityDefinitions()
	{
		super(AbilityDefinition.FOLDER,
			AbilityDefinition.class,
			AbilityDefinition.INVALID,
			AbilityDefinition::new);
	}
}