package com.flansmod.common.actions.nodes;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.SoundDefinition;

import javax.annotation.Nonnull;

public class PlaySoundAction extends ActionInstance
{
	public PlaySoundAction(@Nonnull ActionGroupInstance group, ActionDefinition def)
	{
		super(group, def);
	}

	public boolean Finished()
	{
		return true;
	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}
	@Override
	public void OnTriggerClient(int triggerIndex)
	{
		for(SoundDefinition soundDef : Def.sounds)
		{
			float volume = Group.Context.Volume();
			float pitch = Group.Context.Pitch();
			SoundLODManager.PlaySound(soundDef, Group.Context.Gun.GetShooter().Entity(), volume, pitch);
		}
	}
}
