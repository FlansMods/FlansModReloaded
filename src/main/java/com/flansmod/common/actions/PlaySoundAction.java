package com.flansmod.common.actions;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.common.gunshots.GunContext;
import net.minecraft.world.InteractionHand;

public class PlaySoundAction extends Action
{
	public PlaySoundAction(ActionDefinition def, EActionInput inputType)
	{
		super(def, inputType);
	}

	public boolean Finished()
	{
		return true;
	}

	@Override
	public void OnStartClient(ActionContext context)
	{
		super.OnStartClient(context);

		for(SoundDefinition soundDef : actionDef.sounds)
		{
			SoundLODManager.PlaySound(soundDef, context.Shooter().Entity());
		}
	}
}
