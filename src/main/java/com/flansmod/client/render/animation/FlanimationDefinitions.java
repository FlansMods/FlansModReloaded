package com.flansmod.client.render.animation;

import com.flansmod.common.types.Definitions;

public class FlanimationDefinitions extends Definitions<FlanimationDefinition>
{
	public FlanimationDefinitions()
	{
		super(FlanimationDefinition.FOLDER,
			FlanimationDefinition.class,
			FlanimationDefinition.INVALID,
			FlanimationDefinition::new);
	}


}
