package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.GunshotContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import org.jetbrains.annotations.NotNull;

public class CookGrenadeAction extends Action
{
	public CookGrenadeAction(@NotNull ActionGroup group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	public int TotalTicks;
	public int TicksRemaining;

	@Override
	public void SkipTicks(ActionGroupContext context, int ticks)
	{
		TicksRemaining -= ticks;
	}

	@Override
	public void OnStartClient(ActionGroupContext context)
	{
		super.OnStartClient(context);
		TotalTicks = TicksRemaining = Maths.Ceil(Fuse(context) * 20.0f);
	}

	@Override
	public void OnTickClient(ActionGroupContext context)
	{
		super.OnTickClient(context);
		TicksRemaining--;
		if(TicksRemaining <= 0)
		{
			Group.SetFinished();
		}
	}

	@Override
	public void OnFinishClient(ActionGroupContext context)
	{
		// Trigger the other action group on the other input
		EActionInput actionToStart = context.InputType.GetOpposite();
		ActionGroupContext otherActionContext = context.Gun.GetOrCreate(actionToStart);
		ActionGroup otherActionGroup = otherActionContext.CreateActionGroup();
		if(otherActionGroup.CanStart(otherActionContext))
		{
			otherActionGroup.SkipTicks(otherActionContext,TotalTicks - TicksRemaining);
			context.ActionStack().AddActionGroup(otherActionContext, otherActionGroup);
		}
	}

	@Override
	public void OnStartServer(ActionGroupContext context)
	{
		super.OnStartServer(context);
		TotalTicks = TicksRemaining = Maths.Ceil(Fuse(context) * 20.0f);
	}

	@Override
	public void OnTickServer(ActionGroupContext context)
	{
		super.OnTickServer(context);
		TicksRemaining--;
		if(TicksRemaining <= 0)
		{
			Group.SetFinished();
		}
	}

	@Override
	public void OnFinishServer(ActionGroupContext context)
	{
		// Trigger the other action group on the other input
		EActionInput actionToStart = context.InputType.GetOpposite();
		ActionGroupContext otherActionContext = context.Gun.GetOrCreate(actionToStart);
		ActionGroup otherActionGroup = otherActionContext.CreateActionGroup();
		if(otherActionGroup.CanStart(otherActionContext))
		{
			otherActionGroup.SkipTicks(otherActionContext,TotalTicks - TicksRemaining);
			context.ActionStack().AddActionGroup(otherActionContext, otherActionGroup);
		}
	}

	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{

	}

	public float Fuse(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_DURATION, Def.duration);}
}
