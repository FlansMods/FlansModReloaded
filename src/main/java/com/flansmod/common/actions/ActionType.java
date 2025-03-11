package com.flansmod.common.actions;

import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public record ActionType<T extends ActionInstance>(
	@Nonnull BiFunction<ActionGroupInstance, ActionDefinition, T> createFunc)
{
}
