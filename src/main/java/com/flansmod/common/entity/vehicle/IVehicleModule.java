package com.flansmod.common.entity.vehicle;

import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVehicleModule extends IVehicleSaveNode
{
	void Tick(@Nonnull VehicleEntity vehicle);
	@Nullable default Container AsInventory() { return null; }
}
