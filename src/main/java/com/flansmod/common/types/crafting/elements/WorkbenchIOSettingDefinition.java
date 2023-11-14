package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EWorkbenchInventoryType;

public class WorkbenchIOSettingDefinition
{
	@JsonField
	public EWorkbenchInventoryType type = EWorkbenchInventoryType.AllTypes;
	@JsonField
	public boolean allowInput = false;
	@JsonField
	public boolean allowExtract = false;
}
