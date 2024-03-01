package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.nodes.ShootAction;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.elements.ERepeatMode;
import com.flansmod.util.Maths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ActionGroupInstance
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	public static final int NO_NET_SYNC = -1;

	private final List<ActionInstance> Actions;
	@Nonnull
	public final ActionGroupContext Context;
	@Nonnull
	public final ActionGroupDefinition Def;


	protected boolean Finished = false;
	protected int Progress = 0;
	protected int Duration = 0;
	protected long StartedTick = -1000L;
	protected int TriggerCount = 0;
	protected int NetSyncedTriggers = 0;

	// "Minigun" Charge-up / cool-down mode
	protected float Charge = 0.0f;
	protected boolean IsCharging = false;
	// Burst fire mode
	protected int NumBurstsRemaining = 0;

	public boolean HasStarted() { return StartedTick >= 0; }
	public long GetStartedTick() { return StartedTick; }
	public int GetProgressTicks() { return Progress; }
	public int GetDurationPerTriggerTicks() { return Duration; }
	public float GetProgressSeconds() { return Progress * TICK_RATE; }
	public float GetDurationPerTriggerSeconds() { return Duration * TICK_RATE; }
	public int GetTriggerCount() { return TriggerCount; }

	public ActionGroupInstance(@Nonnull ActionGroupContext context)
	{
		Context = context;
		Def = context.Def;
		Duration = Maths.Floor(Def.repeatDelay * 20.0f);
		for(ActionDefinition actionDefinition : Def.actions)
			if(actionDefinition.duration * 20.0f > Duration)
				Duration = Maths.Floor(actionDefinition.duration * 20.0f);
		Actions = new ArrayList<>();
	}

	public void AddAction(ActionInstance action)
	{
		Actions.add(action);
	}
	public List<ActionInstance> GetActions()
	{
		return Actions;
	}
	@Nullable
	public ShootAction GetShootAction()
	{
		for(ActionInstance action : Actions)
			if(action instanceof ShootAction shootAction)
				return shootAction;
		return null;
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  CLIENT
	// -----------------------------------------------------------------------------------------------------------------
	public void OnStartClient()
	{
		if(!HasStarted())
		{
			Finished = false;
			Progress = 0;
			StartedTick = 0L;
			Level level = Context.Gun.GetLevel();
			if (level != null)
				StartedTick = level.getLevelData().getGameTime();

			for(ActionInstance action : Actions)
				action.OnStartClient();
		}

		DoInitialTrigger(() ->
		{
			OnTriggerClient(TriggerCount);
			TriggerCount++;
			return null;
		});
	}
	public void OnStartClientFromNetwork(long startedTick)
	{
		StartedTick = startedTick;
		for(ActionInstance action : Actions)
			action.OnStartClient();
	}
	public void OnTickClient()
	{
		Progress++;

		for(ActionInstance action : Actions)
			action.OnTickClient();

		CheckRetrigger(() ->
		{
			OnTriggerClient(TriggerCount);
			TriggerCount++;
			return null;
		});
	}
	public void OnFinishClient()
	{
		for(ActionInstance action : Actions)
			action.OnFinishClient();
	}
	private void OnTriggerClient(int triggerIndex)
	{
		for(ActionInstance action : Actions)
			action.OnTriggerClient(triggerIndex);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  SERVER
	// -----------------------------------------------------------------------------------------------------------------
	public void OnStartServer(boolean doInitialTrigger)
	{
		if(!HasStarted())
		{
			Finished = false;
			Progress = 0;
			StartedTick = 0L;
			Level level = Context.Gun.GetLevel();
			if (level != null)
				StartedTick = level.getLevelData().getGameTime();

			for (ActionInstance action : Actions)
				action.OnStartServer();
		}

		if(doInitialTrigger)
		{
			DoInitialTrigger(() ->
			{
				OnTriggerServer(TriggerCount);
				TriggerCount++;
				return null;
			});
		}
	}
	public void OnStartServerFromNetwork(long startedTick)
	{
		StartedTick = startedTick;
		for(ActionInstance action : Actions)
			action.OnStartServer();
	}
	public void OnTickServer()
	{
		Progress++;

		for(ActionInstance action : Actions)
			action.OnTickServer();

		CheckRetrigger(() ->
		{
			OnTriggerServer(TriggerCount);
			TriggerCount++;
			return null;
		});
	}
	public void OnFinishServer()
	{
		for(ActionInstance action : Actions)
			action.OnFinishServer();
	}
	private void OnTriggerServer(int triggerIndex)
	{
		for(ActionInstance action : Actions)
			action.OnTriggerServer(triggerIndex);
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  SHARED
	// -----------------------------------------------------------------------------------------------------------------
	public EActionResult CanStart()
	{
		// Check two handed settings
		if(Def.twoHanded)
		{
			if (!Context.Gun.CanPerformTwoHandedAction())
				return EActionResult.TryNextAction;
		}
		if(!Def.canActUnderwater)
		{
			if(Context.Gun.GetShooter().Entity().level().isWaterAt(Context.Gun.GetShootOrigin().BlockPos()))
				return EActionResult.TryNextAction;
		}
		if(!Def.canActUnderOtherLiquid)
		{
			if(Context.Gun.GetShooter().Entity().level().isFluidAtPosition(Context.Gun.GetShootOrigin().BlockPos(), (fluidState) -> { return !fluidState.isEmpty() && !fluidState.isSourceOfType(Fluids.WATER); }))
				return EActionResult.TryNextAction;
		}

		// Check child actions
		EActionResult result = EActionResult.CanProcess;
		for(ActionInstance action : Actions)
		{
			EActionResult actionResult = action.CanStart();
			if(actionResult.ordinal() > result.ordinal())
				result = actionResult;
		}

		return result;
	}

	public void SkipTicks(int ticks)
	{
		for(ActionInstance action : Actions)
			action.SkipTicks(ticks);

		Progress += ticks;
	}

	public boolean Finished()
	{
		if(Finished)
			return true;

		switch(RepeatMode())
		{
			// These modes are all set to wait until a SetFinished call happens externally
			case Toggle, FullAuto, WaitUntilNextAction -> { return Finished; }
			// When the minigun has spun down completely, this action finishes
			case Minigun -> { return !IsCharging && Charge <= 0.0f; }
			case SemiAuto, BurstFire ->  { return Progress > Duration * TriggerCount; }
		}
		return false;
	}
	public void UpdateInputHeld(boolean held)
	{
		switch(RepeatMode())
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

	private void DoInitialTrigger(Supplier<Void> triggerFunc)
	{
		// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
		float repeatDelay = RepeatDelaySeconds();
		if(repeatDelay <= 0.0f)
		{
			triggerFunc.get();
		}
		else
		{
			int count = Context.Gun.GetActionStack().TryShootMultiple(repeatDelay);
			for (int i = 0; i < count; i++)
				triggerFunc.get();

			NumBurstsRemaining = RepeatMode() == ERepeatMode.BurstFire ? RepeatCount() - 1 : 0;
		}
	}

	private void CheckRetrigger(Supplier<Void> triggerFunc)
	{
		boolean tryRetrigger = false;
		switch(RepeatMode())
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
				if(Charge >= SpinUpDuration())
				{
					tryRetrigger = true;
					Charge = SpinUpDuration();
				}
			}
		}
		if(tryRetrigger)
		{
			boolean canRetrigger = true;
			for(ActionInstance action : Actions)
				if(!action.CanRetrigger())
					canRetrigger = false;

			if(canRetrigger)
			{
				// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
				float repeatDelay = RepeatDelaySeconds();
				repeatDelay = repeatDelay <= 0.0f ? TICK_RATE : repeatDelay;
				int count = Context.Gun.GetActionStack().TryShootMultiple(repeatDelay);
				for (int i = 0; i < count; i++)
					triggerFunc.get();
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  NET SYNC
	// -----------------------------------------------------------------------------------------------------------------
	public int GetRequiredNetSyncMin()
	{
		if(NetSyncedTriggers < TriggerCount)
			return NetSyncedTriggers;
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
		NetSyncedTriggers = triggerMax + 1;
	}

	public boolean PropogateToServer()
	{
		for(ActionInstance action : Actions)
			if(action.PropogateToServer() || Context.Gun.GetReloadDefinitionContaining(Context) != null)
				return true;
		return false;
	}
	public boolean ShouldFallBackToReload()
	{
		for(ActionInstance action : Actions)
			if(action.ShouldFallBackToReload())
				return true;
		return false;
	}
	public double GetPropogationRadius()
	{
		double furthest = 0d;
		for(ActionInstance action : Actions)
		{
			double radius = action.GetPropogationRadius();
			if(radius > furthest)
				furthest = radius;
		}
		return furthest;
	}
	public void AddExtraPositionsForNetSync(int triggerIndex, List<Vec3> positions)
	{
		for(ActionInstance action : Actions)
			action.AddExtraPositionsForNetSync(triggerIndex, positions);
	}
	public boolean ShouldAddPlayerPosForNetSync()
	{
		if(!Context.Gun.GetShooter().IsValid())
			return false;
		for(ActionInstance action : Actions)
		{
			if(!action.ShouldNetSyncAroundPlayer())
				return false;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------
	//  STATS
	// -----------------------------------------------------------------------------------------------------------------
	@Nonnull
	public ERepeatMode RepeatMode() { return Context.RepeatMode(); }
	public int RepeatCount() { return Context.RepeatCount(); }
	public float RepeatDelaySeconds() { return Context.RepeatDelaySeconds(); }
	public float SpinUpDuration() { return Context.SpinUpDuration(); }

}
