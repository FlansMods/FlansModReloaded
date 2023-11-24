package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;

public class HandlerNodeDefinition
{
	@JsonField
	public String actionGroupToTrigger = "";
	@JsonField(Docs = "[TODO] If non-empty, this will check to see if the gun is in the specified mode. If you start with '!', it will check for not being in that mode")
	public String modalCheck = "";
	@JsonField
	public boolean canTriggerWhileReloading = false;

	@JsonField
	public boolean deferToAttachment = false;
	@JsonField
	public EAttachmentType attachmentType = EAttachmentType.Generic;
	@JsonField
	public int attachmentIndex = 0;
	@JsonField
	public boolean andContinueEvaluating = false;
}
