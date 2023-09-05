package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.util.Maths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActionGroup
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	public static final int NO_NET_SYNC = -1;

	private final List<Action> Actions;
	public final ActionGroupDefinition Def;
	public final EActionInput InputType;

	protected boolean Finished = false;
	protected int Progress = 0;
	protected int Duration = 0;
	protected long StartedTick = 0;
	protected int TriggerCount = 0;
	protected int NetSyncedTriggers = NO_NET_SYNC;

	// "Minigun" Charge-up / cool-down mode
	protected float Charge = 0.0f;
	protected boolean IsCharging = false;
	// Burst fire mode
	protected int NumBurstsRemaining = 0;

	public long GetStartedTick() { return StartedTick; }
	public int GetProgressTicks() { return Progress; }
	public int GetDurationPerTriggerTicks() { return Duration; }
	public float GetProgressSeconds() { return Progress * TICK_RATE; }
	public float GetDurationPerTriggerSeconds() { return Duration * TICK_RATE; }
	public int GetTriggerCount() { return TriggerCount; }

	public ActionGroup(ActionGroupDefinition groupDef, EActionInput inputType)
	{
		InputType = inputType;
		Def = groupDef;
		Duration = Maths.Floor(Def.repeatDelay * 20.0f);
		for(ActionDefinition actionDefinition : groupDef.actions)
			if(actionDefinition.duration * 20.0f > Duration)
				Duration = Maths.Floor(actionDefinition.duration * 20.0f);
		Actions = new ArrayList<>();
	}

	protected void AddAction(Action action)
	{
		Actions.add(action);
	}
	public List<Action> GetActions()
	{
		return Actions;
	}
	@Nullable
	public ShootAction GetShootAction()
	{
		for(Action action : Actions)
			if(action instanceof ShootAction shootAction)
				return shootAction;
		return null;
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  CLIENT
	// -----------------------------------------------------------------------------------------------------------------
	public void OnStartClient(ActionGroupContext context)
	{
		Progress = 0;
		StartedTick = 0L;
		Level level = context.Level();
		if(level != null)
			StartedTick = level.getLevelData().getGameTime();

		for(Action action : Actions)
			action.OnStartClient(context);

		DoInitialTrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerClient(ac, TriggerCount);
			TriggerCount++;
		});
	}
	public void OnStartClientFromNetwork(ActionGroupContext context, long startedTick)
	{
		StartedTick = startedTick;
		for(Action action : Actions)
			action.OnStartClient(context);
	}
	public void OnTickClient(ActionGroupContext context)
	{
		Progress++;

		for(Action action : Actions)
			action.OnTickClient(context);

		CheckRetrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerClient(ac, TriggerCount);
			TriggerCount++;
		});
	}
	public void OnFinishClient(ActionGroupContext context)
	{
		for(Action action : Actions)
			action.OnFinishClient(context);
	}
	private void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{
		for(Action action : Actions)
			action.OnTriggerClient(context, triggerIndex);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  SERVER
	// -----------------------------------------------------------------------------------------------------------------
	public void OnStartServer(ActionGroupContext context)
	{
		Progress = 0;
		StartedTick = 0L;
		Level level = context.Level();
		if(level != null)
			StartedTick = level.getLevelData().getGameTime();

		for(Action action : Actions)
			action.OnStartServer(context);

		DoInitialTrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerServer(ac, TriggerCount);
			TriggerCount++;
		});
	}
	public void OnStartServerFromNetwork(ActionGroupContext context, long startedTick)
	{
		StartedTick = startedTick;
		for(Action action : Actions)
			action.OnStartServer(context);
	}
	public void OnTickServer(ActionGroupContext context)
	{
		Progress++;

		for(Action action : Actions)
			action.OnTickServer(context);

		CheckRetrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerServer(ac, TriggerCount);
			TriggerCount++;
		});
	}
	public void OnFinishServer(ActionGroupContext context)
	{
		for(Action action : Actions)
			action.OnFinishServer(context);
	}
	private void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{
		for(Action action : Actions)
			action.OnTriggerServer(context, triggerIndex);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  SHARED
	// -----------------------------------------------------------------------------------------------------------------
	public boolean CanStart(ActionGroupContext context)
	{
		// Check two handed settings
		if(Def.twoHanded)
		{
			if (!context.Gun().CanPerformTwoHandedAction())
				return false;
		}
		if(!Def.canActUnderwater)
		{
			if(context.Shooter().Entity().level.isWaterAt(new BlockPos(context.Shooter().GetShootOrigin().PositionVec3())))
				return false;
		}
		if(!Def.canActUnderOtherLiquid)
		{
			if(context.Shooter().Entity().level.isFluidAtPosition(new BlockPos(context.Shooter().GetShootOrigin().PositionVec3()), (fluidState) -> { return !fluidState.isEmpty() && !fluidState.isSourceOfType(Fluids.WATER); }))
				return false;
		}

		// Check child actions
		for(Action action : Actions)
			if(!action.CanStart(context))
				return false;

		return true;
	}


	public boolean Finished(ActionGroupContext context)
	{
		if(Finished)
			return true;

		switch(RepeatMode(context))
		{
			// These modes are all set to wait until a SetFinished call happens externally
			case Toggle, FullAuto -> { return Finished; }
			// When the minigun has spun down completely, this action finishes
			case Minigun -> { return !IsCharging && Charge <= 0.0f; }
			case SemiAuto, BurstFire ->  { return Progress > Duration * TriggerCount; }
		}
		return false;
	}
	public void UpdateInputHeld(ActionGroupContext context, boolean held)
	{
		switch(RepeatMode(context))
		{
			case FullAuto ->
			{
				if(!held)
					SetFinished();
			}
			case Minigun ->
			{
				IsCharging = held;
			}
		}
	}
	public void SetFinished() { Finished = true; }

	private void DoInitialTrigger(ActionGroupContext context, Consumer<ActionGroupContext> triggerFunc)
	{
		// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
		float repeatDelay = RepeatDelaySeconds(context);
		if(repeatDelay <= 0.0f)
		{
			triggerFunc.accept(context);
		}
		else
		{
			int count = context.ActionStack().TryShootMultiple(repeatDelay);
			for (int i = 0; i < count; i++)
				triggerFunc.accept(context);

			NumBurstsRemaining = RepeatMode(context) == ERepeatMode.BurstFire ? RepeatCount(context) - 1 : 0;
		}
	}

	private void CheckRetrigger(ActionGroupContext context, Consumer<ActionGroupContext> triggerFunc)
	{
		boolean tryRetrigger = false;
		switch(RepeatMode(context))
		{
			case FullAuto -> {
				tryRetrigger = true;
			}
			case BurstFire -> {
				if (NumBurstsRemaining > 0)
				{
					NumBurstsRemaining--;
					tryRetrigger = true;
				}
			}
			case Minigun -> {
				if(IsCharging)
					Charge += TICK_RATE;
				else
					Charge -= TICK_RATE;
				if(Charge >= SpinUpDuration(context))
				{
					tryRetrigger = true;
					Charge = SpinUpDuration(context);
				}
			}
		}
		if(tryRetrigger)
		{
			boolean canRetrigger = true;
			for(Action action : Actions)
				if(!action.CanRetrigger(context))
					canRetrigger = false;

			if(canRetrigger)
			{
				// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
				float repeatDelay = RepeatDelaySeconds(context);
				repeatDelay = repeatDelay <= 0.0f ? TICK_RATE : repeatDelay;
				int count = context.ActionStack().TryShootMultiple(repeatDelay);
				for (int i = 0; i < count; i++)
					triggerFunc.accept(context);
			}
			else
			{
				SetFinished();
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  NET SYNC
	// -----------------------------------------------------------------------------------------------------------------
	public int GetRequiredNetSyncMin()
	{
		if(NetSyncedTriggers < TriggerCount)
			return NetSyncedTriggers + 1;
		return NO_NET_SYNC;
	}
	public int GetRequiredNetSyncMax()
	{
		if(NetSyncedTriggers < TriggerCount)
			return TriggerCount - 1;
		return NO_NET_SYNC;
	}
	public boolean NeedsNetSync()
	{
		return NetSyncedTriggers != TriggerCount;
	}
	public void OnPerformedNetSync(int triggerMin, int triggerMax)
	{
		if(!(triggerMin == GetRequiredNetSyncMin() && triggerMax == GetRequiredNetSyncMax()))
		{
			FlansMod.LOGGER.warn("Action was NetSynced, but not for the required range");
		}
		NetSyncedTriggers = triggerMax;
	}

	public boolean PropogateToServer(ActionGroupContext context)
	{
		for(Action action : Actions)
			if(action.PropogateToServer(context))
				return true;
		return false;
	}
	public boolean ShouldFallBackToReload(ActionGroupContext context)
	{
		for(Action action : Actions)
			if(action.ShouldFallBackToReload(context))
				return true;
		return false;
	}
	public double GetPropogationRadius(ActionGroupContext context)
	{
		double furthest = 0d;
		for(Action action : Actions)
		{
			double radius = action.GetPropogationRadius(context);
			if(radius > furthest)
				furthest = radius;
		}
		return furthest;
	}
	public void AddExtraPositionsForNetSync(ActionGroupContext context, int triggerIndex, List<Vec3> positions)
	{
		for(Action action : Actions)
			action.AddExtraPositionsForNetSync(context, triggerIndex, positions);
	}
	public boolean ShouldAddPlayerPosForNetSync(ActionGroupContext context)
	{
		if(!context.Shooter().IsValid())
			return false;
		for(Action action : Actions)
		{
			if(!action.ShouldNetSyncAroundPlayer(context))
				return false;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  STATS
	// -----------------------------------------------------------------------------------------------------------------
	public ERepeatMode RepeatMode(ActionGroupContext context) { return context.RepeatMode(); }
	public int RepeatCount(ActionGroupContext context) { return context.RepeatCount(); }
	public float RepeatDelaySeconds(ActionGroupContext context) { return context.RepeatDelaySeconds(); }
	public float SpinUpDuration(ActionGroupContext context) { return context.SpinUpDuration(); }

}
