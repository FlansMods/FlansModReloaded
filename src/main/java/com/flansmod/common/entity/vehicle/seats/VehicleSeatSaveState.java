package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import javax.annotation.Nonnull;
import java.util.UUID;

public class VehicleSeatSaveState implements IVehicleSaveNode
{
	public void Load(@Nonnull CompoundTag tags)
	{
	}

	@Nonnull
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		return tags;
	}
}
