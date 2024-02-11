package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAbilityEffect
{
	void Trigger(@Nonnull GunContext gun,
				 @Nonnull TargetsContext targets,
				 @Nullable AbilityStack stacks,
				 int tier);

	void End(@Nonnull GunContext gun,
			 @Nullable AbilityStack stacks);

	ModifierDefinition[] NO_MODS = new ModifierDefinition[0];
	@Nonnull
	default ModifierDefinition[] GetActiveModifiers() { return NO_MODS; }
}
