package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import net.minecraft.world.InteractionHand;

public class AnimationAction extends Action
{
	public AnimationAction(ActionDefinition def, EActionInput inputType)
	{
		super(def, inputType);
	}

	private static final int HOLD_END_POSE_TICKS = 1;
	@Override
	public boolean Finished(ActionContext context)
	{
		return progress > duration;
	}
	@Override
	protected void OnTriggerServer(ActionContext context)
	{

	}
	@Override
	protected void OnTriggerClient(ActionContext context)
	{

	}
}
