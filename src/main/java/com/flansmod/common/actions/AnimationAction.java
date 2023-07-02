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
	public boolean Finished()
	{
		return progress > duration;
	}

	@Override
	public void OnStartServer(ActionContext context)
	{
		super.OnStartServer(context);
	}
	@Override
	public void OnTickServer(ActionContext context)
	{
		super.OnTickServer(context);
	}
	@Override
	public void OnFinishServer(ActionContext context)
	{
		super.OnFinishServer(context);
	}

	@Override
	public void OnStartClient(ActionContext context)
	{
		super.OnStartClient(context);
	}
	@Override
	public void OnTickClient(ActionContext context)
	{
		super.OnTickClient(context);
	}
	@Override
	public void OnFinishClient(ActionContext context)
	{
		super.OnFinishClient(context);
	}
}
