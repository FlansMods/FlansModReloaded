package com.flansmod.common.types.attachments;

import com.flansmod.common.types.Definitions;

public class AttachmentDefinitions extends Definitions<AttachmentDefinition>
{
	public AttachmentDefinitions()
	{
		super(AttachmentDefinition.FOLDER,
			  AttachmentDefinition.class,
			  AttachmentDefinition.INVALID,
			  (key) -> { return new AttachmentDefinition(key); } );
	}
}
