package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class SoundDefinition
{
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation GetSoundLocation()
	{
		return sound;
	}

	@JsonField(AssetPathHint = "sounds/")
	public ResourceLocation sound = InvalidLocation;
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

	public float PickRangedVolume(float distance){
		return minVolume + ((maxVolume-minVolume)*(1-((distance)/(maxRange))));
	}

	public float PickRandomPitch(RandomSource random)
	{
		return minPitchMultiplier + random.nextFloat() * (maxPitchMultiplier - minPitchMultiplier);
	}

	public void PlayUnLODedOnEntity(Entity entity)
	{
		entity.playSound(
			SoundEvent.createFixedRangeEvent(sound, maxRange),
			PickRandomVolume(entity.level().random),
			PickRandomPitch(entity.level().random)
		);
	}

	public void PlayUnLODed(Level level, Vec3 pos)
	{
		level.playSound(
			null,
			pos.x, pos.y, pos.z,
			SoundEvent.createFixedRangeEvent(sound, maxRange),
			SoundSource.PLAYERS,
			PickRandomVolume(level.random),
			PickRandomPitch(level.random));
	}
}
