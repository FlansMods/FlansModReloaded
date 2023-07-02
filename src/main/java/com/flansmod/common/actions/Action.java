package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.gunshots.GunshotCollection;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.util.Maths;
import net.minecraft.world.InteractionHand;

public abstract class Action
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	public final ActionDefinition actionDef;
	public final EActionInput inputType;
	protected int progress = 0;
	protected int duration = 0;
	public int GetProgressTicks() { return progress; }
	public int GetDurationTicks() { return duration; }
	public float GetProgressSeconds() { return progress * TICK_RATE; }
	public float GetDurationSeconds() { return duration * TICK_RATE; }

	public boolean Finished()
	{
		switch(Lifetime())
		{
			case Instant -> { return true; }
			case Duration -> { return progress > duration; }
			case UntilInputReleased -> { return progress == Integer.MAX_VALUE; }
			case ToggleWhenInputAgain -> { return progress == Integer.MAX_VALUE; }
		}
		return false;
	}
	public EActionLifetime Lifetime() { return EActionLifetime.Instant; }
	public void SetFinished() { progress = Integer.MAX_VALUE; }

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

	public void OnStartServer(ActionContext context) { progress = 0; }
	public void OnTickServer(ActionContext context) { progress++; }
	public void OnFinishServer(ActionContext context) {}

	public void OnStartClient(ActionContext context) { progress = 0; }
	public void OnTickClient(ActionContext context) { progress++; }
	public void OnFinishClient(ActionContext context) {}

	public boolean VerifyServer(ActionContext context, GunshotCollection shots) { return true; }

}
