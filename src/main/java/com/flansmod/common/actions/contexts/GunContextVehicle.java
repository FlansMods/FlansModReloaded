package com.flansmod.common.actions.contexts;

import javax.annotation.Nonnull;

public class GunContextVehicle extends GunContextInventoryItem
{
	protected final ShooterContextVehicle VehicleContext;
	protected final int GunIndex;

	public GunContextVehicle(@Nonnull ShooterContextVehicle vehicle, int gunIndex)
	{
		super(vehicle.GetAttachedInventory(), vehicle.GunSlotIndex(gunIndex));
		VehicleContext = vehicle;
		GunIndex = gunIndex;
	}
}
