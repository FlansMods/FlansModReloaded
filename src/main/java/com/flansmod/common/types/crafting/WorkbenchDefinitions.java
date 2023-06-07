package com.flansmod.common.types.crafting;

import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.parts.PartDefinition;

public class WorkbenchDefinitions extends Definitions<WorkbenchDefinition>
{
	public WorkbenchDefinitions()
	{
		super(WorkbenchDefinition.FOLDER,
			WorkbenchDefinition.class,
			WorkbenchDefinition.INVALID,
			WorkbenchDefinition::new);
	}
}
