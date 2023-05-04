package com.flansmod.common.types.attachments;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;

public class AttachmentDefinition extends JsonDefinition
{
	public static final AttachmentDefinition INVALID = new AttachmentDefinition(new ResourceLocation(FlansMod.MODID, "attachments/null"));
	public static final String TYPE = "attachment";
	@Override
	public String GetTypeName() { return TYPE; }

	public AttachmentDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}


	@JsonField
	public ModifierDefinition[] Modifiers = new ModifierDefinition[0];


}
