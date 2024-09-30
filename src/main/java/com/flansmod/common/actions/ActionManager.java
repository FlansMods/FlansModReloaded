package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunInputContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.common.types.guns.elements.ERepeatMode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ActionManager
{
	protected final boolean IsClient;
	protected HashMap<UUID, ActionStack> ActionStacks = new HashMap<UUID, ActionStack>();

	public ActionManager(boolean client)
	{
		IsClient = client;
	}

	public void Clear()
	{
		ActionStacks.clear();
	}
	public void ClearGun(@Nonnull UUID gunID)
	{
		ActionStacks.remove(gunID);
	}


	@Nonnull
	public ActionStack GetActionStack(GunContext context)
	{
		return GetActionStack(context.GetUUID());
	}
	@Nonnull
	public ActionStack GetActionStack(UUID gunID)
	{
		if(gunID.equals(FlanItem.InvalidGunUUID))
		{
			FlansMod.LOGGER.warn("Tried to get action stack for invalid context");
			return ActionStack.Invalid;
		}

		ActionStack entitysActionStack = ActionStacks.get(gunID);
		if(entitysActionStack == null)
		{
			entitysActionStack = new ActionStack(IsClient);
			ActionStacks.put(gunID, entitysActionStack);
		}
		return entitysActionStack;
	}

	@Nonnull
	public static ActionStack SafeGetActionStack(GunContext context)
	{
		Level level = context.GetLevel();
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

	@Nonnull
	protected abstract EActionResult TryStartGroupInstance(@Nonnull ActionStack actionStack, @Nonnull ActionGroupContext context);
	@Nonnull
	protected abstract EActionResult TryUpdateGroupInstanceHeld(@Nonnull ActionStack actionStack, @Nonnull ActionGroupContext context);
	@Nonnull
	protected abstract EActionResult TryUpdateGroupInstanceNotHeld(@Nonnull ActionStack actionStack, @Nonnull ActionGroupContext context);



	public void KeyPressed(@Nonnull ShooterContext shooter, @Nonnull EPlayerInput inputType)
	{
		// See if pressing this button should trigger any actions
		// First on our main hand, then on our off hand
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);

			IteratePossibleActionGroups(inputContext, actionStack, this::TryStartGroupInstance);
		}
	}

	public void KeyHeld(@Nonnull ShooterContext shooter, @Nonnull EPlayerInput inputType)
	{
		// See if any of the in-progress actions on this gun should stop on release
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IterateActionGroupsThatRespondToHeld(inputContext, actionStack, this::TryUpdateGroupInstanceHeld);
		}
	}

	public void KeyReleased(@Nonnull ShooterContext shooter, @Nonnull EPlayerInput inputType, int ticksSinceHeld)
	{
		// See if any of the in-progress actions on this gun should stop on release
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IterateActiveActionGroups(inputContext, actionStack, this::TryUpdateGroupInstanceNotHeld);
		}
	}

	protected void IteratePossibleActionGroups(@Nonnull GunInputContext inputContext,
											   @Nonnull ActionStack actionStack,
											   @Nonnull BiFunction<ActionStack, ActionGroupContext, EActionResult> func)
	{
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.GetOrCreateGroupInstance(kvp.getFirst());
			EActionResult funcResult = func.apply(actionStack, kvp.getFirst());
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

	// Needs a better name, but this should pass "Still holding" to anything we are holding
	// And then pass "holding" to any full-auto groups that are waiting, so they can start asap
	protected void IterateActionGroupsThatRespondToHeld(@Nonnull GunInputContext inputContext,
														@Nonnull ActionStack actionStack,
														@Nonnull BiFunction<ActionStack, ActionGroupContext, EActionResult> func)
	{
		// First, do active AGs
		IterateActiveActionGroups(inputContext, actionStack, func);

		// Now do a custom two-layer iterate, where we only allow FullAuto AGs to be updated
		IteratePossibleActionGroups(inputContext, actionStack, (actionStackInner, possibleActionGroup) -> {
			EActionResult baseResult = func.apply(actionStackInner, possibleActionGroup);
			if(baseResult == EActionResult.CanProcess)
			{
				if(possibleActionGroup.RepeatMode() == ERepeatMode.FullAuto || possibleActionGroup.RepeatMode() == ERepeatMode.Minigun)
				{
					return EActionResult.CanProcess;
				}
				else return EActionResult.TryNextAction;
			}
			return baseResult;
		});
	}

	protected void IterateActiveActionGroups(@Nonnull GunInputContext inputContext,
											 @Nonnull ActionStack actionStack,
											 @Nonnull BiFunction<ActionStack, ActionGroupContext, EActionResult> func)
	{
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.TryGetGroupInstance(kvp.getFirst());
			if(groupInstance != null)
				func.apply(actionStack, kvp.getFirst());
		}
	}
}
