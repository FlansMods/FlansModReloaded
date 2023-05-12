package com.flansmod.common.types.guns;

import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.guns.GunDefinition;

public class GunDefinitions extends Definitions<GunDefinition>
{
	public GunDefinitions()
	{
		super(GunDefinition.TYPE,
			  GunDefinition.class,
			  GunDefinition.INVALID,
			  GunDefinition::new);
	}
}
