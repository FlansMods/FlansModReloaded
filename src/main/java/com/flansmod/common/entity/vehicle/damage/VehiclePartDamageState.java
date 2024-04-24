package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class VehiclePartDamageState implements IVehicleSaveNode
{
	@Nonnull
	public final DamageablePartDefinition Def;

	public float CurrentHealth;

	public VehiclePartDamageState(@Nonnull DamageablePartDefinition def)
	{
		Def = def;
		CurrentHealth = Def.maxHealth;
	}

	@Override
	public void Load(@Nonnull CompoundTag tags)
	{
		if(tags.contains("hp"))
			CurrentHealth = tags.getFloat("hp");
	}

	@Nonnull
	@Override
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		tags.putFloat("hp", CurrentHealth);
		return tags;
	}
}
