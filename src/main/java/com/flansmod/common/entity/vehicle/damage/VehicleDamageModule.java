package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VehicleDamageModule implements IVehicleModule
{
	public final Map<String, VehiclePartDamageState> DamageStates = new HashMap<>();

	public VehicleDamageModule(@Nonnull VehicleDefinitionHierarchy heirarchy,
							   @Nonnull VehicleEntity vehicle)

	{
		heirarchy.ForEachDamageable((damageableDef) -> {
			DamageStates.put(damageableDef.partName, new VehiclePartDamageState(damageableDef));
		});

		if(!DamageStates.containsKey("body"))
		{
			FlansMod.LOGGER.warn("VehicleDamageModule for " + vehicle.Def() + " had no 'body' collider");
			DamageStates.put("body", new VehiclePartDamageState(DamageablePartDefinition.DEFAULT_BODY));
		}
	}

	@Nonnull
	public VehiclePartDamageState GetCore() { return DamageStates.get("body"); }
	@Nonnull
	public VehiclePartDamageState GetPartOrCore(@Nonnull String partName)
	{
		return DamageStates.getOrDefault(partName, GetCore());
	}

	public boolean IsPartDestroyed(@Nonnull String partName)
	{
		if(DamageStates.containsKey(partName))
			return DamageStates.get(partName).IsDestroyed();
		return false;
	}

	public float ApplyDamageTo(@Nonnull String partName, @Nonnull DamageSource source, float amount)
	{
		return GetPartOrCore(partName).ApplyDamage(source, amount);
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
