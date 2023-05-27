package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class PartItem extends FlanItem
{
	private ResourceLocation definitionLocation;
	public PartDefinition Def() { return FlansMod.PARTS.get(definitionLocation); }

	public PartItem(ResourceLocation defLoc, Properties props)
	{
		super(props);
	}
}
