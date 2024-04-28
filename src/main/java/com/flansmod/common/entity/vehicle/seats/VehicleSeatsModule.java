package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleSeatsModule implements IVehicleModule
{
	public static final int INVALID_SEAT_INDEX = -1;

	@Nonnull
	public final List<String> SeatOrdering = new ArrayList<>();
	@Nonnull
	public final Map<String, VehicleSeatSaveState> SeatStates = new HashMap<>();

	public VehicleSeatsModule(@Nonnull VehicleDefinitionHeirarchy hierarchy,
							  @Nonnull VehicleEntity vehicle)
	{
		hierarchy.ForEachSeat((seatDef) -> {
				SeatStates.put(seatDef.attachedTo, new VehicleSeatSaveState(seatDef));
		});

		SeatOrdering.addAll(SeatStates.keySet());
	}

	@Nonnull
	protected VehicleSeatSaveState GetSeat(@Nonnull String vehiclePart) {
		return SeatStates.getOrDefault(vehiclePart, VehicleSeatSaveState.INVALID);
	}
	@Nonnull
	protected VehicleSeatSaveState GetSeat(int seatIndex) {
		return SeatStates.getOrDefault(SeatOrdering.get(seatIndex), VehicleSeatSaveState.INVALID);
	}
	@Nullable
	public Entity GetPassengerInSeat(int seatIndex)
	{
		return GetSeat(seatIndex).Passenger;
	}



	public int GetSeatIndexForNewPassenger(@Nonnull Entity passenger)
	{
		// TODO:
		return INVALID_SEAT_INDEX;
	}
	// Minecraft needs this, but our vehicles are more complex, other seats might control sub-pieces
	public int GetControlSeatIndex()
	{
		return 0;
	}




	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(SeatStates.containsKey(key))
			{
				SeatStates.get(key).Load(vehicle, tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Seat key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : SeatStates.entrySet())
		{
			CompoundTag seatTags = kvp.getValue().Save(vehicle);
			tags.put(kvp.getKey(), seatTags);
		}
		return tags;
	}
}
