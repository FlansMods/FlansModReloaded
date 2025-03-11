package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.types.vehicles.ControlSchemeDefinition;

import javax.annotation.Nonnull;
import java.util.function.Function;

public record ControlLogicType<T extends ControlLogic>(
	@Nonnull Function<ControlSchemeDefinition, T> createFunc
)
{

}
