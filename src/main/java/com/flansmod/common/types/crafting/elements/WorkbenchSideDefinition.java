package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.core.Direction;

public class WorkbenchSideDefinition
{
	@JsonField
	public Direction side;
	@JsonField
	public int EUInputPerTick = 0;
	@JsonField
	public int EUOutputPerTick = 0;

	@JsonField
	public WorkbenchIOSettingDefinition[] ioSettings = new WorkbenchIOSettingDefinition[0];

	@JsonField(Docs = "To-Do, potential option to directly access neighbour inventories when crafting")
	public boolean workbenchCanUseInventoriesOnSide = false;
}
