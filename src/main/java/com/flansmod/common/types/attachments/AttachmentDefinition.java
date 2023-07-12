package com.flansmod.common.types.attachments;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import net.minecraft.resources.ResourceLocation;

public class AttachmentDefinition extends JsonDefinition
{
	public static final AttachmentDefinition INVALID = new AttachmentDefinition(new ResourceLocation(FlansMod.MODID, "attachments/null"));
	public static final String TYPE = "attachment";
	public static final String FOLDER = "attachments";
	@Override
	public String GetTypeName() { return TYPE; }

	public AttachmentDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public EAttachmentType attachmentType = EAttachmentType.Generic;
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	@JsonField
	public EMechaEffect[] mechaEffects = new EMechaEffect[0];
	@JsonField
	public String mechaEffectFilter = "";



	@JsonField
	public ActionDefinition[] primaryActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];
	@JsonField(Docs = "If true, adding this attachment will swap the primaryActions array. Otherwise, it will be additive")
	public boolean replacePrimaryAction = false;
	@JsonField(Docs = "If true, adding this attachment will swap the secondaryActions array. Otherwise, it will be additive")
	public boolean replaceSecondaryAction = false;




	@JsonField
	public ERepeatMode modeOverride = ERepeatMode.FullAuto;
	@JsonField
	public boolean overrideFireMode = false;
}
