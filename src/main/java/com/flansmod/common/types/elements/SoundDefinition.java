package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoundDefinition
{
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation GetSoundLocation()
	{
		return ResourceLocation.tryParse(sound);
	}

	@JsonField
	public String sound = "";
	@JsonField(Docs = "In seconds")
	public float length = 1f;
	@JsonField
	public float minPitchMultiplier = 1f;
	@JsonField
	public float maxPitchMultiplier = 1f;
	@JsonField
	public float minVolume = 1f;
	@JsonField
	public float maxVolume = 1f;

	@JsonField
	public float maxRange = 100f;
	@JsonField
	public SoundLODDefinition[] LODs = new SoundLODDefinition[0];
}
