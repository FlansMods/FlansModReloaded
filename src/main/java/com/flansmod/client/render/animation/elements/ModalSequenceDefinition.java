package com.flansmod.client.render.animation.elements;

import com.flansmod.common.types.JsonField;

public class ModalSequenceDefinition
{
	@JsonField
	public String modeName = "";
	@JsonField(Min = 1)
	public int ticksBetweenFrames = 20;
	@JsonField
	public SequenceEntryDefinition[] frames = new SequenceEntryDefinition[0];
}
