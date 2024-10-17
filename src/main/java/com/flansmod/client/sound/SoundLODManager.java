package com.flansmod.client.sound;

import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.common.types.elements.SoundLODDefinition;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class SoundLODManager
{
	public static void PlaySound(@Nonnull SoundDefinition soundDef, @Nonnull Entity onEntity)
	{
		PlaySound(soundDef, onEntity, 1f, 1f);
	}
	public static void PlaySound(@Nonnull SoundDefinition soundDef, @Nonnull Entity onEntity, float volumeMulti, float pitchMulti)
	{
		PlaySound(soundDef, onEntity.position(), volumeMulti, pitchMulti);
	}
	public static void PlaySound(@Nonnull SoundDefinition soundDef, @Nonnull Transform transform, float volumeMulti, float pitchMulti)
	{
		PlaySound(soundDef, transform.positionVec3(), volumeMulti, pitchMulti);
	}
	public static void PlaySound(@Nonnull SoundDefinition soundDef, @Nonnull Vec3 position, float volumeMulti, float pitchMulti)
	{

		Entity camera = Minecraft.getInstance().cameraEntity;
		if(camera != null)
		{
			double distance = Maths.sqrt(camera.distanceToSqr(position));
			double bestLOD = 0d;
			ResourceLocation soundToPlay = soundDef.GetSoundLocation();
			for(SoundLODDefinition lodDefinition : soundDef.LODs)
			{
				if(distance >= lodDefinition.minDistance && lodDefinition.minDistance > bestLOD)
				{
					bestLOD = lodDefinition.minDistance;
					ResourceLocation lodSoundToPlay = lodDefinition.GetSoundLocation();
					if(lodSoundToPlay != null)
						soundToPlay = lodSoundToPlay;
				}
			}

			if(soundToPlay != null)
			{
				Minecraft.getInstance().level.playLocalSound(
					position.x,
					position.y,
					position.z,
					SoundEvent.createFixedRangeEvent(soundToPlay, soundDef.maxRange),
					SoundSource.HOSTILE,
					volumeMulti * soundDef.PickRangedVolume((float)distance),
					pitchMulti * soundDef.PickRandomPitch(Minecraft.getInstance().level.random),
					true);

				//FlansMod.LOGGER.info("Played " + soundToPlay + " at " + onEntity.position());
			}
		}
	}

}
