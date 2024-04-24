package com.flansmod.common.entity.vehicle;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public interface IVehicleSaveNode
{
	void Load(@Nonnull CompoundTag tags);
	@Nonnull CompoundTag Save();
}
