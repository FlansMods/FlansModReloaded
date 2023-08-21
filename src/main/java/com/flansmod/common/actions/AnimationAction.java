package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;

public class AnimationAction extends Action
{
	public AnimationAction(ActionGroupDefinition groupDef, ActionDefinition def, EActionInput inputType)
	{
		super(groupDef, def, inputType);
	}

	private static final int HOLD_END_POSE_TICKS = 1;
	@Override
	public boolean Finished(ActionGroupContext context)
	{
		return Progress > Duration;
	}
	@Override
	protected void OnTriggerServer(ActionGroupContext context)
	{

	}
	@Override
	protected void OnTriggerClient(ActionGroupContext context)
	{

	}
}
