package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
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

	public int AnimFrame = 0;
	public boolean WaitForNextAnim = false;

	@Override
	public boolean PropogateToServer(ActionGroupContext context) { return false; }

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
