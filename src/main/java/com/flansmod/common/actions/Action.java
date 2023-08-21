package com.flansmod.common.actions;

import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class Action
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	public final ActionGroupDefinition ActionGroupDef;
	public final ActionDefinition ActionDef;
	public final EActionInput InputType;

	protected boolean Finished = false;
	protected int Progress = 0;
	protected int Duration = 0;

	// "Minigun" Charge-up / cool-down mode
	protected float Charge = 0.0f;
	protected boolean IsCharging = false;
	// Burst fire mode
	protected int NumBurstsRemaining = 0;

	public int GetProgressTicks() { return Progress; }
	public int GetDurationTicks() { return Duration; }
	public float GetProgressSeconds() { return Progress * TICK_RATE; }
	public float GetDurationSeconds() { return Duration * TICK_RATE; }

	public boolean Finished(ActionGroupContext context)
	{
		switch(RepeatMode(context))
		{
			// These modes are all set to wait until a SetFinished call happens externally
			case Toggle, FullAuto -> { return Finished; }
			// When the minigun has spun down completely, this action finishes
			case Minigun -> { return !IsCharging && Charge <= 0.0f; }
			case SemiAuto, BurstFire ->  { return Progress > Duration; }
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

	public Action(@Nonnull ActionGroupDefinition groupDef, @Nonnull ActionDefinition def, EActionInput inputType)
	{
		this.ActionGroupDef = groupDef;
		this.ActionDef = def;
		this.InputType = inputType;
		this.Duration = Maths.Ceil(def.duration * 20f);
		this.Progress = 0;
	}

	public boolean ShouldRender(GunContext context) { return true; }
	public boolean PropogateToServer(ActionGroupContext context) { return false; }
	public boolean ShouldFallBackToReload(ActionGroupContext context) { return false; }
	public boolean CanStart(ActionGroupContext context)
	{
		if(ActionGroupDef.twoHanded)
		{
			if (!context.Gun().CanPerformTwoHandedAction())
				return false;
		}

		return true;
	}

	protected abstract void OnTriggerClient(ActionGroupContext context);
	protected abstract void OnTriggerServer(ActionGroupContext context);


	public void OnStartServer(ActionGroupContext context)
	{
		Progress = 0;
		DoInitialTrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerServer(ac);
		});
	}
	public void OnTickServer(ActionGroupContext context)
	{
		Progress++;
		CheckRetrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerServer(ac);
		});
	}
	public void OnFinishServer(ActionGroupContext context) {}

	public void OnStartClient(ActionGroupContext context)
	{
		Progress = 0;
		DoInitialTrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerClient(ac);
		});
	}
	public void OnTickClient(ActionGroupContext context)
	{
		Progress++;
		CheckRetrigger(context, (ActionGroupContext ac) ->
		{
			OnTriggerClient(ac);
		});
	}
	public void OnFinishClient(ActionGroupContext context) {}

	public boolean VerifyServer(ActionGroupContext context, GunshotCollection shots) { return true; }

	private void DoInitialTrigger(ActionGroupContext context, Consumer<ActionGroupContext> triggerFunc)
	{
		// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
		float repeatDelay = RepeatDelay(context);
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
			// We allow *any* non-zero value for repeats, but we should not allow exactly zero.
			float repeatDelay = RepeatDelay(context);
			repeatDelay = repeatDelay <= 0.0f ? TICK_RATE : repeatDelay;
			int count = context.ActionStack().TryShootMultiple(repeatDelay);
			for(int i = 0; i < count; i++)
				triggerFunc.accept(context);
		}
	}

	// These ones are general for the whole group
	public ERepeatMode RepeatMode(ActionGroupContext context) { return context.RepeatMode(); }
	public int RepeatCount(ActionGroupContext context) { return context.RepeatCount(); }
	public float RepeatDelay(ActionGroupContext context) { return context.RepeatDelay(); }
	public float SpinUpDuration(ActionGroupContext context) { return context.SpinUpDuration(); }

	// These ones are specific to this action
	public float Duration(ActionGroupContext context) { return context.ModifyFloat("duration", ActionDef.duration); }
	public float FOVFactor(ActionGroupContext context) { return context.ModifyFloat("FOV_factor", ActionDef.fovFactor); }
	public float ToolLevel(ActionGroupContext context) { return context.ModifyFloat("tool_level", ActionDef.toolLevel); }
	public float HarvestSpeed(ActionGroupContext context) { return context.ModifyFloat("harvest_speed", ActionDef.harvestSpeed); }
	public float Reach(ActionGroupContext context) { return context.ModifyFloat("reach", ActionDef.reach); }
}
