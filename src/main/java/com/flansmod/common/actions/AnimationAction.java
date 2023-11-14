package com.flansmod.common.actions;

import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class AnimationAction extends ActionInstance
{
	public AnimationAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	public int AnimFrame = 0;
	public boolean WaitForNextAnim = false;

	@Override
	public boolean PropogateToServer() { return false; }

	@Override
	public void OnTickClient()
	{
		AnimFrame++;
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{
		AnimFrame = 0;
	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}
}
