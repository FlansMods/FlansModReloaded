package com.flansmod.common.types.npc.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.SoundDefinition;

public class VoiceLineDefinition
{
	@JsonField
	public EVoiceLineType type = EVoiceLineType.Chat;
	@JsonField
	public String unlocalisedString = "npc.unknown.chat";
	@JsonField
	public SoundDefinition audioClip = new SoundDefinition();
}
