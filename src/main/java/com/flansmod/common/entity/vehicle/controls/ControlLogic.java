package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehiclePartPath;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.util.collision.DynamicCollisionEvent;
import com.flansmod.util.collision.StaticCollisionEvent;

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

	// Return: Should perform default collision resolution
	public boolean OnCollide(@Nonnull VehiclePartPath onPart,
							 @Nonnull StaticCollisionEvent collision)
	{
		return true;
	}
	public boolean OnCollide(@Nonnull VehiclePartPath onPart,
							 @Nonnull DynamicCollisionEvent collision)
	{
		return true;
	}
}
