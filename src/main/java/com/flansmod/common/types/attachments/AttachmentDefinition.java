package com.flansmod.common.types.attachments;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.*;
import com.flansmod.common.types.guns.ERepeatMode;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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

	public void GetOverrides(EActionInput inputType, List<ActionGroupOverrideDefinition> matchingOverrides)
	{
		for(ActionGroupOverrideDefinition override : actionOverrides)
		{
			if(override.inputType == inputType)
				matchingOverrides.add(override);
		}
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
	public ActionGroupOverrideDefinition[] actionOverrides = new ActionGroupOverrideDefinition[0];

	@JsonField
	public ERepeatMode modeOverride = ERepeatMode.FullAuto;
	@JsonField
	public boolean overrideFireMode = false;
}
