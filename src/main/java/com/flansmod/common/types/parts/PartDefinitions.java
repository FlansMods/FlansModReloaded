package com.flansmod.common.types.parts;

import com.flansmod.common.types.Definitions;

public class PartDefinitions extends Definitions<PartDefinition>
{
	public PartDefinitions()
	{
		super(PartDefinition.FOLDER,
			PartDefinition.class,
			PartDefinition.INVALID,
			(key) -> { return new PartDefinition(key); } );
	}
}
