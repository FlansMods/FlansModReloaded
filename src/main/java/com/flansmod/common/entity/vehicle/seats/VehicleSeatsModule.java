package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class VehicleSeatsModule implements IVehicleModule
{
	public VehicleSeatsModule(@Nonnull VehicleDefinitionHeirarchy hierarchy,
							  @Nonnull VehicleEntity vehicle)
	{

	}


	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull CompoundTag tags)
	{

	}

	@Nonnull
	@Override
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		return tags;
	}
}
