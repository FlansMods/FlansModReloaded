package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.guns.VehicleGunSaveState;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class VehicleSeatSaveState implements IVehicleSaveNode
{
	public static final VehicleSeatSaveState INVALID = new VehicleSeatSaveState();
	public static final UUID EMPTY_SEAT = new UUID(0L, 0L);

	public static final EntityDataSerializer<VehicleSeatSaveState> SERIALIZER = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull VehicleSeatSaveState data)
		{
			buf.writeUUID(data.PassengerID);
		}
		@Override
		@Nonnull
		public VehicleSeatSaveState read(@Nonnull FriendlyByteBuf buf)
		{
			VehicleSeatSaveState state = new VehicleSeatSaveState();
			state.PassengerID = buf.readUUID();
			return state;
		}
	};

	@Nonnull
	public UUID PassengerID;

	public boolean IsEmpty()
	{
		return PassengerID.equals(EMPTY_SEAT);
	}

	public VehicleSeatSaveState()
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
