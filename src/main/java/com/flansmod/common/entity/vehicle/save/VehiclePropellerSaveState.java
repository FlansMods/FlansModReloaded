package com.flansmod.common.entity.vehicle.save;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.PropellerDefinition;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class VehiclePropellerSaveState implements IVehicleSaveNode
{
	@Nonnull
	public final PropellerDefinition Def;

	public VehiclePropellerSaveState(@Nonnull PropellerDefinition def)
	{
		Def = def;
	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{

	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		return new CompoundTag();
	}
}
