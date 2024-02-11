package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectNone implements IAbilityEffect
{
	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targetsContext, @Nullable AbilityStack stacks, int tier) {}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks) {}
}
