package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.physics.common.util.Maths;
import org.jetbrains.annotations.NotNull;

public class CookGrenadeAction extends ActionInstance
{
	public CookGrenadeAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	public int TotalTicks;
	public int TicksRemaining;

	@Override
	public void SkipTicks(int ticks)
	{
		TicksRemaining -= ticks;
	}

	@Override
	public void OnStartClient()
	{
		super.OnStartClient();
		TotalTicks = TicksRemaining = Maths.Ceil(Fuse() * 20.0f);
	}

	@Override
	public void OnTickClient()
	{
		super.OnTickClient();
		TicksRemaining--;
		if(TicksRemaining <= 0)
		{
			Group.SetFinished();
		}
	}

	@Override
	public void OnFinishClient()
	{
		// Trigger the other action group on the other input
		ActionGroupContext otherActionContext = Group.Context.Gun.GetActionGroupContext(OtherActionName());
		ActionStack actionStack = Group.Context.Gun.GetActionStack();
		ActionGroupInstance groupInstance = actionStack.GetOrCreateGroupInstance(otherActionContext);
		actionStack.Server_TryStartGroupInstance(otherActionContext);
		groupInstance.SkipTicks(TotalTicks - TicksRemaining);
	}

	@Override
	public void OnStartServer()
	{
		super.OnStartServer();
		TotalTicks = TicksRemaining = Maths.Ceil(Fuse() * 20.0f);
	}

	@Override
	public void OnTickServer()
	{
		super.OnTickServer();
		TicksRemaining--;
		if(TicksRemaining <= 0)
		{
			Group.SetFinished();
		}
	}

	@Override
	public void OnFinishServer()
	{
		ActionGroupContext otherActionContext = Group.Context.Gun.GetActionGroupContext(OtherActionName());
		ActionStack actionStack = Group.Context.Gun.GetActionStack();
		ActionGroupInstance groupInstance = actionStack.GetOrCreateGroupInstance(otherActionContext);
		actionStack.Server_TryStartGroupInstance(otherActionContext);
		groupInstance.SkipTicks(TotalTicks - TicksRemaining);
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}

	public String OtherActionName() { return ModifyString(Constants.KEY_ACTION_KEY, Actions.DefaultPrimaryActionKey); }
	public float Fuse() { return ModifyFloat(Constants.STAT_DURATION, Def.duration);}
}
