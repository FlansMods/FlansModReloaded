package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;

import javax.annotation.Nonnull;

public class AimDownSightAction extends Action
{
	protected boolean shouldStop = false;

	public AimDownSightAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}
	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{

	}
}
