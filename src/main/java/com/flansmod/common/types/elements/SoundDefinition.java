package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class SoundDefinition
{
	@JsonField
	public String sound = "";
	@JsonField(Docs = "In seconds")
	public float length = 1f;
	@JsonField
	public float minPitchMultiplier = 1f;
	@JsonField
	public float maxPitchMultiplier = 1f;

	@JsonField
	public float maxRange = 100f;
	@JsonField
	public SoundLODDefinition[] LODs = new SoundLODDefinition[0];
}
