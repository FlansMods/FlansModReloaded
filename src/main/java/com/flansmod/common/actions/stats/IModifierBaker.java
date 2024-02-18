package com.flansmod.common.actions.stats;

import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;

public interface IModifierBaker
{
	default void Bake(@Nonnull ModifierDefinition mod) { Bake(mod, 1, 1); }
	void Bake(@Nonnull ModifierDefinition mod, int level, int stackCount);
}
