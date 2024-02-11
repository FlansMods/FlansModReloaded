package com.flansmod.common.types.abilities;

import com.flansmod.common.types.Definitions;

public class CraftingTraitDefinitions extends Definitions<CraftingTraitDefinition>
{
	public CraftingTraitDefinitions()
	{
		super(CraftingTraitDefinition.FOLDER,
			CraftingTraitDefinition.class,
			CraftingTraitDefinition.INVALID,
			CraftingTraitDefinition::new);
	}
}