package com.flansmod.common.entity.vehicle.guns;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

public class VehicleGunSaveState implements IVehicleSaveNode
{
	@Nonnull
	public final MountedGunDefinition Def;
	@Nonnull
	public ItemStack GunStack;

	@Nonnull
	public UUID GetGunID() { return GunItem.GetGunID(GunStack); }

	public VehicleGunSaveState(@Nonnull MountedGunDefinition def)
	{
		Def = def;
		GunStack = ItemStack.EMPTY;
	}

	public void Load(@Nonnull CompoundTag tags)
	{
		if(tags.contains("item"))
			GunStack = ItemStack.of(tags.getCompound("item"));
	}

	@Nonnull
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		tags.put("item", GunStack.save(new CompoundTag()));
		return tags;
	}
}
