package com.flansmod.common.types.npc;

import com.flansmod.common.types.Definitions;

public class NpcDefinitions extends Definitions<NpcDefinition>
{
	public NpcDefinitions()
	{
		super(NpcDefinition.FOLDER,
			NpcDefinition.class,
			NpcDefinition.INVALID,
			NpcDefinition::new);
	}
}