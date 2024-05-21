package com.flansmod.common.entity.vehicle;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVehicleModule extends IVehicleSaveNode
{
	void Tick(@Nonnull VehicleEntity vehicle);
	void DefineSyncedData(@Nonnull SynchedEntityData entityData);
	@Nullable default Container AsInventory() { return null; }
}
