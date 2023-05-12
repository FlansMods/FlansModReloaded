package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.world.InteractionHand;

public class AnimationAction extends Action
{
	public AnimationAction(ActionDefinition def, InteractionHand hand)
	{
		super(def, hand);
	}

	@Override
	public void OnStartServer(GunContext context)
	{
		super.OnStartServer(context);
	}
	@Override
	public void OnTickServer(GunContext context)
	{
		super.OnTickServer(context);
	}
	@Override
	public void OnFinishServer(GunContext context)
	{
		super.OnFinishServer(context);
	}

	@Override
	public void OnStartClient(GunContext context)
	{
		super.OnStartClient(context);
	}
	@Override
	public void OnTickClient(GunContext context)
	{
		super.OnTickClient(context);
	}
	@Override
	public void OnFinishClient(GunContext context)
	{
		super.OnFinishClient(context);
	}
}
