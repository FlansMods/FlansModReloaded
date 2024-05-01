package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VehicleSeatSaveState implements IVehicleSaveNode
{
	public static final VehicleSeatSaveState INVALID = new VehicleSeatSaveState(SeatDefinition.INVALID);

	@Nonnull
	public final SeatDefinition Def;
	@Nullable
	public Entity Passenger;

	public VehicleSeatSaveState(@Nonnull SeatDefinition def)
	{
		Def = def;
		Passenger = null;
	}

	@Nonnull
	public InputDefinition[] GetInputs() { return Def.inputs; }

	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("passIdx"))
			Passenger = vehicle.getPassengers().get(tags.getInt("passIdx"));
	}

	@Nonnull
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		// This only works in combination with the getPassengers array being loaded in the same order
		// TODO: CHECK this
		tags.putInt("passIdx", vehicle.getPassengers().indexOf(Passenger));
		return tags;
	}
}
