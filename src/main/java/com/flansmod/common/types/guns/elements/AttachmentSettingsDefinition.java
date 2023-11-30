package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;

public class AttachmentSettingsDefinition
{
	@JsonField
	public String[] matchNames = new String[0];
	@JsonField
	public String[] matchTags = new String[0];
	@JsonField
	public int numAttachmentSlots = 0;
	@JsonField
	public boolean hideDefaultMesh = true;
}
