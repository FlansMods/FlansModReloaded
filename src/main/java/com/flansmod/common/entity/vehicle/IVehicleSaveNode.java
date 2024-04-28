package com.flansmod.common.entity.vehicle;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public interface IVehicleSaveNode
{
	void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags);
	@Nonnull CompoundTag Save(@Nonnull VehicleEntity vehicle);
}
