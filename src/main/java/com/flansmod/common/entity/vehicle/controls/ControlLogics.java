package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.types.vehicles.ControlSchemeDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ControlLogics
{
	@Nullable
	public static ControlLogic InstanceControlLogic(@Nonnull ControlSchemeDefinition def)
	{
		return switch(def.logicType)
		{
			case LegacyCar -> new LegacyVehicleControlLogic(def, false);
			case LegacyTank -> new LegacyVehicleControlLogic(def, true);
			case Car -> new CarControlLogic(def);
			default -> null;
		};
	}
}
