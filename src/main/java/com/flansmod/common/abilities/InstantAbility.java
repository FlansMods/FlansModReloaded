package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InstantAbility extends AbilityInstance
{
	private float TimeSinceLastTrigger = 0.0f;

	public InstantAbility(AbilityDefinition def, int level)
	{
		super(def, level);
	}

	public float GetAmount() { return Def.GetAmount(Level); }
	public float GetDurationSeconds() { return 0.0f; }
	public float GetTimeSinceLastTriggerSeconds() { return TimeSinceLastTrigger; }

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		TimeSinceLastTrigger = 0.0f;
	}

	@Override
	public void End(@Nonnull GunContext gun)
	{
		// We don't really need an end condition, we're just applying an instant effect
	}

	@Override
	public void Tick()
	{
		TimeSinceLastTrigger += 1f/20f;
	}
}
