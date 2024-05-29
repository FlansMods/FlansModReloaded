package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SeatSyncState implements IVehicleSaveNode
{
	public static final SeatSyncState INVALID = new SeatSyncState();
	public static final UUID EMPTY_SEAT = new UUID(0L, 0L);

	@Nonnull
	public UUID PassengerID;

	public boolean IsEmpty()
	{
		return PassengerID.equals(EMPTY_SEAT);
	}

	public SeatSyncState()
	{
		PassengerID = EMPTY_SEAT;
	}

	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("passengerID"))
			PassengerID = tags.getUUID("passengerID");
	}

	@Nonnull
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		tags.putUUID("passIdx", PassengerID);
		return tags;
	}
}
