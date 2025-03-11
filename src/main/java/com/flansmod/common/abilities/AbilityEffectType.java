package com.flansmod.common.abilities;

import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;

import javax.annotation.Nonnull;
import java.util.function.Function;

public record AbilityEffectType<T extends IAbilityEffect>(
	@Nonnull Function<AbilityEffectDefinition, T> createFunc)
{
}
