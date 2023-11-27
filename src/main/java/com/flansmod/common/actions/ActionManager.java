package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public abstract class ActionManager
{
	protected final boolean IsClient;
	protected HashMap<GunContext, ActionStack> ActionStacks = new HashMap<GunContext, ActionStack>();

	public ActionManager(boolean client)
	{
		IsClient = client;
	}

	@Nonnull
	public ActionStack GetActionStack(GunContext context)
	{
		if(!context.IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for invalid context");
			return ActionStack.Invalid;
		}
		if(!context.GetShooter().IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for a valid context, but without an entity. This may be supported later");
			return ActionStack.Invalid;
		}

		ActionStack entitysActionStack = ActionStacks.get(context);
		if(entitysActionStack == null)
		{
			entitysActionStack = new ActionStack(IsClient);
			ActionStacks.put(context, entitysActionStack);
		}
		return entitysActionStack;
	}

	@Nonnull
	public static ActionStack SafeGetActionStack(GunContext context)
	{
		Level level = context.Level();
		if(level != null)
		{
			if (level.isClientSide)
			{
				return FlansModClient.ACTIONS_CLIENT.GetActionStack(context);
			}
			else
			{
				return FlansMod.ACTIONS_SERVER.GetActionStack(context);
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Could not get ActionStack for GunContext " + context.toString());
			return ActionStack.Invalid;
		}
	}



	protected void IteratePossibleActionGroups(GunInputContext inputContext, Function<ActionGroupContext, EActionResult> func)
	{
		ActionStack actionStack = inputContext.Gun.GetActionStack();
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.GetOrCreateGroupInstance(kvp.getFirst());
			EActionResult funcResult = func.apply(kvp.getFirst());
			boolean keepEvaluating = true;
			switch(funcResult)
			{
				case CanProcess -> // We processed the function, all good
				{
					keepEvaluating = kvp.getSecond();
				}
				case TryNextAction ->
				{
					actionStack.CancelGroupInstance(kvp.getFirst());
				}
				case Wait -> // This function is telling us to stop, do not continue
				{
					actionStack.CancelGroupInstance(kvp.getFirst());
					keepEvaluating = false;
				}
			}
			if(!keepEvaluating)
				return;
		}
	}

	protected void IterateActiveActionGroups(GunInputContext inputContext, Function<ActionGroupContext, EActionResult> func)
	{
		ActionStack actionStack = inputContext.Gun.GetActionStack();
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.TryGetGroupInstance(kvp.getFirst());
			if(groupInstance != null)
				func.apply(kvp.getFirst());
		}
	}
}
