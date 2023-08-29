package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;

import javax.annotation.Nonnull;

public class AnimationAction extends Action
{
	public AnimationAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	private static final int HOLD_END_POSE_TICKS = 1;
	public int AnimFrame = 0;

	@Override
	public void OnTickClient(ActionGroupContext context)
	{
		AnimFrame++;
	}

	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{
		AnimFrame = 0;
	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{

	}
}
