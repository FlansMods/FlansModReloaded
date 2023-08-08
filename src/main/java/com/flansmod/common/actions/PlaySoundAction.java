package com.flansmod.common.actions;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.SoundDefinition;

public class PlaySoundAction extends Action
{
	public PlaySoundAction(ActionGroupDefinition groupDef, ActionDefinition def, EActionInput inputType)
	{
		super(groupDef, def, inputType);
	}

	public boolean Finished()
	{
		return true;
	}

	@Override
	protected void OnTriggerServer(ActionGroupContext context)
	{

	}
	@Override
	protected void OnTriggerClient(ActionGroupContext context)
	{
		for(SoundDefinition soundDef : ActionDef.sounds)
		{
			SoundLODManager.PlaySound(soundDef, context.Shooter().Entity());
		}
	}
}
