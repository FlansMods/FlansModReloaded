package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;

import javax.annotation.Nonnull;

public abstract class ControlLogic
{
	@Nonnull
	public final ControlSchemeDefinition Def;

	public ControlLogic(@Nonnull ControlSchemeDefinition def)
	{
		Def = def;
	}

	@Nonnull
	protected ControlSchemeAxisDefinition FindAxis(@Nonnull EVehicleAxis axisType)
	{
		for(ControlSchemeAxisDefinition axis : Def.axes)
		{
			if(axis.axisType == axisType)
				return axis;
		}
		return ControlSchemeAxisDefinition.INVALID;
	}

	public abstract boolean CanControl(@Nonnull VehicleDefinition vehicleDef);

	public abstract void TickAuthoritative(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs);
	public abstract void TickRemote(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs);
}
