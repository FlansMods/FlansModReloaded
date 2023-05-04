package com.flansmod.common.types.bullets;

import com.flansmod.common.types.Definitions;

public class BulletDefinitions extends Definitions<BulletDefinition>
{
	public BulletDefinitions()
	{
		super(BulletDefinition.TYPE,
			  BulletDefinition.class,
			  BulletDefinition.INVALID,
			  (key) -> { return new BulletDefinition(key); } );
	}
}
