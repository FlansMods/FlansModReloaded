package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;

public class AimDownSightAction extends Action
{
	protected boolean shouldStop = false;

	public AimDownSightAction(ActionGroupDefinition groupDef, ActionDefinition def, EActionInput inputType)
	{
		super(groupDef, def, inputType);
	}

	@Override
	public boolean CanStart(ActionGroupContext context)
	{
		// TODO: 2H check
		return true;
	}

	@Override
	protected void OnTriggerClient(ActionGroupContext context)
	{

	}

	@Override
	protected void OnTriggerServer(ActionGroupContext context)
	{

	}

	@Override
	public void OnTickClient(ActionGroupContext context)
	{
		super.OnTickClient(context);
		CancelActionIfDuplicate(context);
	}

	@Override
	public void OnTickServer(ActionGroupContext context)
	{
		super.OnTickClient(context);
		CancelActionIfDuplicate(context);
	}

	private void CancelActionIfDuplicate(ActionGroupContext context)
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
	public boolean Finished(ActionGroupContext context)
	{
		return shouldStop;
	}
}
