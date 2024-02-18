package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Subtly different to ApplyEffect ability, this does not show up in the HUD as a mob effect
// Use for things that are more constant and less trigger based
public class AbilityInstanceApplyModifier implements IAbilityEffect
{
	@Nonnull
	public final AbilityEffectDefinition Def;

	public AbilityInstanceApplyModifier(@Nonnull AbilityEffectDefinition def)
	{
		Def = def;
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	@Override
	@Nonnull
	public ModifierDefinition[] GetActiveModifiers()
	{
		return Def.modifiers;
	}
}
