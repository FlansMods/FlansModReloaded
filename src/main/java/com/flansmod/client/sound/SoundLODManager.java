package com.flansmod.client.sound;

import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.common.types.elements.SoundLODDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SoundLODManager
{
	public static void PlaySound(SoundDefinition soundDef, Entity onEntity)
	{
		Entity camera = Minecraft.getInstance().cameraEntity;
		if(camera != null)
		{
			double distance = camera.distanceTo(onEntity);
			double bestLOD = 0d;
			ResourceLocation soundToPlay = soundDef.GetSoundLocation();
			for(SoundLODDefinition lodDefinition : soundDef.LODs)
			{
				if(distance >= lodDefinition.minDistance && lodDefinition.minDistance > bestLOD)
				{
					bestLOD = lodDefinition.minDistance;
					soundToPlay = lodDefinition.GetSoundLocation();
				}
			}

			Minecraft.getInstance().level.playLocalSound(
				onEntity.position().x,
				onEntity.position().y,
				onEntity.position().z,
				SoundEvent.createFixedRangeEvent(soundToPlay, soundDef.maxRange),
				SoundSource.HOSTILE,
				GetVolume(soundDef, Minecraft.getInstance().level.random),
				GetPitch(soundDef, Minecraft.getInstance().level.random),
				true);
		}
	}

	public static float GetVolume(SoundDefinition soundDef, RandomSource random)
	{
		return soundDef.minVolume
			+ random.nextFloat() * (soundDef.maxVolume - soundDef.minVolume);
	}

	public static float GetPitch(SoundDefinition soundDef, RandomSource random)
	{
		return soundDef.minPitchMultiplier
			+ random.nextFloat() * (soundDef.maxPitchMultiplier - soundDef.minVolume);
	}
}
