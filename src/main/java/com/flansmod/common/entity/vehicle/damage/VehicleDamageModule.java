package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VehicleDamageModule implements IVehicleModule
{
	public final Map<String, VehiclePartDamageState> DamageStates = new HashMap<>();

	public VehicleDamageModule(@Nonnull VehicleDefinitionHeirarchy heirarchy,
							   @Nonnull VehicleEntity vehicle)

	{
		heirarchy.ForEachDamageable((damageableDef) -> {
			DamageStates.put(damageableDef.partName, new VehiclePartDamageState(damageableDef));
		});
	}

	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(DamageStates.containsKey(key))
			{
				DamageStates.get(key).Load(vehicle, tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Damage key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : DamageStates.entrySet())
		{
			tags.put(kvp.getKey(), kvp.getValue().Save(vehicle));
		}
		return tags;
	}
}
