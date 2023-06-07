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
	public boolean workbenchCanUseInventoriesOnSide = false;
	@JsonField
	public boolean acceptItems = false;
	@JsonField
	public String acceptFilter = "";
	@JsonField
	public boolean outputItems = false;
	@JsonField
	public String outputFilter = "";
}
