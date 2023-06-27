package com.flansmod.common.actions;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.world.InteractionHand;

public class PlaySoundAction extends Action
{
	public PlaySoundAction(ActionStack stack, ActionDefinition def, InteractionHand hand)
	{
		super(stack, def, hand);
	}

	public boolean Finished()
	{
		return true;
	}

	@Override
	public void OnStartClient(GunContext context)
	{
		super.OnStartClient(context);

		for(SoundDefinition soundDef : actionDef.sounds)
		{
			SoundLODManager.PlaySound(soundDef, context.shootFrom);
		}
	}
}
