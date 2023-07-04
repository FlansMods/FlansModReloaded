package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import net.minecraft.world.InteractionHand;

public class AimDownSightAction extends Action
{
	protected boolean shouldStop = false;

	public AimDownSightAction(ActionDefinition def, EActionInput inputType)
	{
		super(def, inputType);
	}

	@Override
	public boolean CanStart(ActionContext context)
	{
		// TODO: 2H check
		return true;
	}

	@Override
	protected void OnTriggerClient(ActionContext context)
	{

	}

	@Override
	protected void OnTriggerServer(ActionContext context)
	{

	}

	@Override
	public void OnTickClient(ActionContext context)
	{
		super.OnTickClient(context);
		CancelActionIfDuplicate(context);
	}

	@Override
	public void OnTickServer(ActionContext context)
	{
		super.OnTickClient(context);
		CancelActionIfDuplicate(context);
	}

	private void CancelActionIfDuplicate(ActionContext context)
	{
		int numScopeActions = 0;
		for(Action action : context.ActionStack().GetActions())
		{
			if(action instanceof AimDownSightAction)
				numScopeActions++;
		}
		if(numScopeActions > 1)
		{
			for(Action action : context.ActionStack().GetActions())
			{
				if(action instanceof AimDownSightAction scopeAction)
					scopeAction.shouldStop = true;
			}
		}
	}

	@Override
	public boolean Finished(ActionContext context)
	{
		return shouldStop;
	}
}
