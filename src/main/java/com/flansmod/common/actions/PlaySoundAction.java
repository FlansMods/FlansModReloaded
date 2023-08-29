package com.flansmod.common.actions;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.SoundDefinition;

import javax.annotation.Nonnull;

public class PlaySoundAction extends Action
{
	public PlaySoundAction(@Nonnull ActionGroup group, ActionDefinition def)
	{
		super(group, def);
	}

	public boolean Finished()
	{
		return true;
	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{

	}
	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{
		for(SoundDefinition soundDef : Def.sounds)
		{
			SoundLODManager.PlaySound(soundDef, context.Shooter().Entity());
		}
	}
}
