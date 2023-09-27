package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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

	public float PickRandomVolume(RandomSource random)
	{
		return minVolume + random.nextFloat() * (maxVolume - minVolume);
	}

	public float PickRandomPitch(RandomSource random)
	{
		return minPitchMultiplier + random.nextFloat() * (maxPitchMultiplier - minPitchMultiplier);
	}

	public void PlayUnLODdedOnEntity(Entity entity)
	{
		entity.playSound(
			SoundEvent.createFixedRangeEvent(new ResourceLocation(sound), maxRange),
			PickRandomVolume(entity.level.random),
			PickRandomPitch(entity.level.random)
		);
	}
}
