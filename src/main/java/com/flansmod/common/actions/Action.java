package com.flansmod.common.actions;

import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.util.Maths;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class Action
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	public final ActionDefinition actionDef;
	public final EActionInput inputType;

	protected boolean finished = false;
	protected int progress = 0;
	protected int duration = 0;

	// "Minigun" Charge-up / cool-down mode
	protected float charge = 0.0f;
	protected boolean isCharging = false;
	// Burst fire mode
	protected int numBurstsRemaining = 0;

	// Caches
	@Nullable
	private CachedActionStats ActionStatCache = null;
	@Nullable
	private CachedGunStats GunStatCache = null;

	public int GetProgressTicks() { return progress; }
	public int GetDurationTicks() { return duration; }
	public float GetProgressSeconds() { return progress * TICK_RATE; }
	public float GetDurationSeconds() { return duration * TICK_RATE; }

	public boolean Finished(ActionContext context)
	{
		switch(RepeatMode(context))
		{
			// These modes are all set to wait until a SetFinished call happens externally
			case Toggle, FullAuto -> { return finished; }
			// When the minigun has spun down completely, this action finishes
			case Minigun -> { return !isCharging && charge <= 0.0f; }
			case SemiAuto, BurstFire ->  { return progress > duration; }
		}
		return false;
	}
	public void UpdateInputHeld(ActionContext context, boolean held)
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
				isCharging = held;
			}
		}
	}
	public void SetFinished() { finished = true; }

	public Action(ActionDefinition def, EActionInput inputType)
	{
		this.actionDef = def;
		this.inputType = inputType;
		this.duration = Maths.Ceil(def.duration * 20f);
		this.progress = 0;
	}

	public boolean ShouldRender(GunContext context) { return true; }
	public boolean PropogateToServer(ActionContext context) { return false; }
	public boolean ShouldFallBackToReload(ActionContext context) { return false; }
	public boolean CanStart(ActionContext context)
	{
		if(actionDef.twoHanded)
		{
			if (!context.Gun().CanPerformTwoHandedAction())
				return false;
		}

		return true;
	}

	protected abstract void OnTriggerClient(ActionContext context);
	protected abstract void OnTriggerServer(ActionContext context);


	public void OnStartServer(ActionContext context)
	{
		progress = 0;
		DoInitialTrigger(context, this::OnTriggerServer);
	}
	public void OnTickServer(ActionContext context)
	{
		progress++;
		CheckRetrigger(context, this::OnTriggerServer);
	}
	public void OnFinishServer(ActionContext context) {}

	public void OnStartClient(ActionContext context)
	{
		progress = 0;
		DoInitialTrigger(context, this::OnTriggerClient);
	}
	public void OnTickClient(ActionContext context)
	{
		progress++;
		CheckRetrigger(context, this::OnTriggerClient);
	}
	public void OnFinishClient(ActionContext context) {}

	public boolean VerifyServer(ActionContext context, GunshotCollection shots) { return true; }

	private void DoInitialTrigger(ActionContext context, Consumer<ActionContext> triggerFunc)
	{
		// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
		float repeatDelay = RepeatDelay(context);
		repeatDelay = repeatDelay <= 0.0f ? TICK_RATE : repeatDelay;
		int count = context.ActionStack().TryShootMultiple(repeatDelay);
		for(int i = 0; i < count; i++)
			triggerFunc.accept(context);

		numBurstsRemaining = RepeatMode(context) == ERepeatMode.BurstFire ? RepeatCount(context) - 1 : 0;
	}

	private void CheckRetrigger(ActionContext context, Consumer<ActionContext> triggerFunc)
	{
		boolean tryRetrigger = false;
		switch(RepeatMode(context))
		{
			case FullAuto -> {
				tryRetrigger = true;
			}
			case BurstFire -> {
				if (numBurstsRemaining > 0)
				{
					numBurstsRemaining--;
					tryRetrigger = true;
				}
			}
			case Minigun -> {
				if(isCharging)
					charge += TICK_RATE;
				else
					charge -= TICK_RATE;
				if(charge >= SpinUpDuration(context))
				{
					tryRetrigger = true;
					charge = SpinUpDuration(context);
				}
			}
		}
		if(tryRetrigger)
		{
			// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
			float repeatDelay = RepeatDelay(context);
			repeatDelay = repeatDelay <= 0.0f ? TICK_RATE : repeatDelay;
			int count = context.ActionStack().TryShootMultiple(repeatDelay);
			for(int i = 0; i < count; i++)
				triggerFunc.accept(context);
		}
	}

	@Nonnull
	private CachedActionStats GetActionStats(ActionContext context)
	{
		if(ActionStatCache == null)
			ActionStatCache = context.BuildActionStatCache(actionDef);
		return ActionStatCache;
	}
	public ERepeatMode RepeatMode(ActionContext context) { return GetActionStats(context).RepeatMode; }
	public int RepeatCount(ActionContext context) { return GetActionStats(context).RepeatCount; }
	public float RepeatDelay(ActionContext context) { return GetActionStats(context).RepeatDelay; }
	public float SpinUpDuration(ActionContext context) { return GetActionStats(context).SpinUpDuration; }
	public float Duration(ActionContext context) { return GetActionStats(context).Duration; }
	public float FOVFactor(ActionContext context) { return GetActionStats(context).FOVFactor; }
	public float ToolLevel(ActionContext context) { return GetActionStats(context).ToolLevel; }
	public float HarvestSpeed(ActionContext context) { return GetActionStats(context).HarvestSpeed; }
	public float Reach(ActionContext context) { return GetActionStats(context).Reach; }


}
