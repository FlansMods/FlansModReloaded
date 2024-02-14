package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.abilities.Abilities;
import com.flansmod.common.abilities.IAbilityEffect;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AbilityEffectDefinition
{
	// -- Effect --
	@JsonField
	public EAbilityEffect effectType = EAbilityEffect.Nothing;
	@JsonField(Docs = "The modifiers to add when the effect is active")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	@Nonnull
	public ModifierDefinition[] MatchModifiers(@Nonnull String stat)
	{
		List<ModifierDefinition> matches = new ArrayList<>();
		for(ModifierDefinition modifier : modifiers)
		{
			if(modifier.stat.equals(stat))
				matches.add(modifier);
		}
		return matches.toArray(new ModifierDefinition[0]);
	}
	@Nonnull
	public String ModifyString(@Nonnull String stat, @Nonnull String defaultValue)
	{
		for(ModifierDefinition modifier : modifiers)
		{
			if(modifier.stat.equals(stat) && !modifier.setValue.isEmpty())
				defaultValue = modifier.setValue;
		}
		return defaultValue;
	}

	@Nullable
	private IAbilityEffect EffectProcessor = null;
	@Nonnull
	public IAbilityEffect GetEffectProcessor()
	{
		if(EffectProcessor == null)
			EffectProcessor = Abilities.CreateEffectProcessor(this);
		return EffectProcessor;
	}
}
