package com.flansmod.common.types.grenades;

import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.guns.GunDefinition;

public class GrenadeDefinitions extends Definitions<GrenadeDefinition>
{
	public GrenadeDefinitions()
	{
		super(GrenadeDefinition.FOLDER,
			GrenadeDefinition.class,
			GrenadeDefinition.INVALID,
			GrenadeDefinition::new);
	}
}
