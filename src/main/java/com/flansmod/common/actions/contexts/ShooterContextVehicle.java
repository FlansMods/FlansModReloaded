package com.flansmod.common.actions.contexts;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.VehicleSaveState;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class ShooterContextVehicle extends ShooterContext
{
	@Nonnull
	protected final VehicleEntity Vehicle;

	@Nonnull
	public VehicleDefinition VehicleDef()
	{
		return Vehicle.Def();
	}

	public ShooterContextVehicle(@Nonnull VehicleEntity vehicle)
	{
		Vehicle = vehicle;
	}


	@Nonnull
	public ShooterContextVehicleSeat GetSeat(int seatIndex)
	{

	}

	@Override
	@Nonnull
	public Entity Entity()
	{
		return Vehicle;
	}
	@Override
	@Nonnull
	public UUID[] GetAllGunIDs()
	{
		UUID[] gunIDs = new UUID[Vehicle.SaveState.Guns.length];
		for(int i = 0; i < Vehicle.SaveState.Guns.length; i++)
			gunIDs[i] = Vehicle.SaveState.Guns[i].GunID;

		return gunIDs;
	}
	@Override
	public boolean IsValid() { return !Vehicle.isRemoved(); }
	@Override
	public boolean IsCreative() { return false; }



}
