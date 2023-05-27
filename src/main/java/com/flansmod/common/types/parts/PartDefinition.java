package com.flansmod.common.types.parts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;
import net.minecraft.resources.ResourceLocation;

public class PartDefinition extends JsonDefinition
{
	public static final PartDefinition INVALID = new PartDefinition(new ResourceLocation(FlansMod.MODID, "parts/null"));
	public static final String TYPE = "part";
	@Override
	public String GetTypeName() { return TYPE; }

	public PartDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}


	@JsonField
	public boolean canPlaceInMachiningTable = false;
	@JsonField
	public boolean canPlaceInModificationTable = false;


	// Does this item provide modifications to weapons?
	@JsonField(Min = 0)
	public int upgradeAttachmentLevels = 0;
	@JsonField
	public EAttachmentType upgradeAttachmentType = EAttachmentType.Generic;
	@JsonField
	public boolean canUpgradeToLegendary = false;

	// Is this a fuel item?
}
