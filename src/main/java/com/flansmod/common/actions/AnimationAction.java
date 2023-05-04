package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;

public class AnimationAction extends Action
{
	public AnimationAction(ActionDefinition def)
	{
		super(def);
	}

	@Override
	protected void OnStartServer(GunContext context) {}
	@Override
	protected void OnTickServer(GunContext context) {}
	@Override
	protected void OnFinishServer(GunContext context) {}

	@Override
	protected void OnStartClient(GunContext context)
	{

	}

	@Override
	protected void OnTickClient(GunContext context)
	{

	}

	@Override
	protected void OnFinishClient(GunContext context)
	{

	}
}
