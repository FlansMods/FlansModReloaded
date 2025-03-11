package com.flansmod.common.effects;

import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;

// Subtly different to ApplyEffect ability, this does not show up in the HUD as a mob effect
// Use for things that are more constant and less trigger based
public class EffectApplyModifier implements IEffect
{
	@Nonnull
	public final AbilityEffectDefinition Def;

	public EffectApplyModifier(@Nonnull AbilityEffectDefinition def)
	{
		Def = def;
	}
	@Override
	public boolean CanBeContinuous() { return true; }
	@Override
	@Nonnull
	public ModifierDefinition[] GetActiveModifiers()
	{
		return Def.modifiers;
	}
}
