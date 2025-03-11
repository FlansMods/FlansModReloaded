package com.flansmod.common.effects;

import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;

import javax.annotation.Nonnull;
import java.util.function.Function;

public record EffectType<T extends IEffect>(
	@Nonnull Function<AbilityEffectDefinition, T> createFunc)
{
}
