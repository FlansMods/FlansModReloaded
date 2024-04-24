package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.types.vehicles.elements.ArticulatedPartDefinition;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class VehicleArticulationSaveState implements IVehicleSaveNode
{
	@Nonnull
	public final ArticulatedPartDefinition Def;
	public float Parameter;

	public VehicleArticulationSaveState(@Nonnull ArticulatedPartDefinition def)
	{
		Def = def;
		Parameter = def.startParameter;
	}

	public void Load(@Nonnull CompoundTag tags)
	{
		Parameter = tags.getFloat("param");
	}

	@Nonnull
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		tags.putFloat("param", Parameter);
		return tags;
	}
}
