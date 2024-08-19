package com.flansmod.common.entity.vehicle.modules;

import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.entity.vehicle.save.SeatSyncState;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public interface IVehicleSeatHelper
{

	@Nonnull
	PerPartMap<SeatSyncState> GetSeatSaveData();
	void SetSeatSaveData(@Nonnull PerPartMap<SeatSyncState> map);
	List<VehicleComponentPath> GetSeatOrdering();

	@Nonnull
	default SeatSyncState GetSeat(@Nonnull VehicleComponentPath seatPath)
	{
		return GetSeatSaveData().GetOrDefault(seatPath, SeatSyncState.INVALID);
	}
	@Nonnull
	default UUID GetPassengerIDInSeat(@Nonnull VehicleComponentPath seatPath)
	{
		return GetSeat(seatPath).PassengerID;
	}
	@Nullable
	default Entity GetPassengerInSeat(@Nonnull VehicleComponentPath seatPath, @Nonnull List<Entity> passengers)
	{
		UUID id = GetPassengerIDInSeat(seatPath);
		for(Entity entity : passengers)
			if(entity.getUUID().equals(id))
				return entity;
		return null;
	}
	@Nonnull
	default VehicleComponentPath GetControlSeatPath()
	{
		for(int i = 0; i < GetSeatOrdering().size(); i++)
		{
			//if(SeatStates.get(SeatOrdering.get(i)).IsController())
			{
				return GetSeatOrdering().get(i);
			}
		}
		return VehicleComponentPath.Invalid;
	}
	@Nonnull
	default SeatSyncState GetSeat(int seatIndex)
	{
		return GetSeat(GetSeatOrdering().get(seatIndex));
	}
	default int GetSeatIndexOf(@Nonnull Entity entity)
	{
		for(int i = 0; i < GetSeatOrdering().size(); i++)
		{
			if(GetSeat(i).PassengerID == entity.getUUID())
				return i;
		}
		return -1;
	}
	default int GetSeatIndexForNewPassenger(@Nonnull Entity passenger)
	{
		for(int i = 0; i < GetSeatOrdering().size(); i++)
		{
			if(GetSeat(i).IsEmpty())
				return i;
		}
		return -1;
	}
	// Minecraft needs this, but our vehicles are more complex, other seats might control sub-pieces
	default int GetControlSeatIndex()
	{
		return -1;
	}
	@Nullable
	default Entity GetControllingPassenger(@Nonnull VehicleEntity vehicle)
	{
		return GetPassengerInSeat(GetControlSeatPath(), vehicle.getPassengers());
	}



}
