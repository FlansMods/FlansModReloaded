package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VehicleDamageModule implements IVehicleModule
{
	private static final Map<String, EntityDataAccessor<Float>> PER_PART_HEALTH = new HashMap<>();
	private SynchedEntityData VehicleDataSynchronizer;
	public final Map<String, DamageablePartDefinition> DamageDefinitions = new HashMap<>();

	public VehicleDamageModule(@Nonnull VehicleDefinitionHierarchy heirarchy,
							   @Nonnull VehicleEntity vehicle)
	{
		heirarchy.ForEachDamageable(DamageDefinitions::put);

		if(!DamageDefinitions.containsKey("body"))
		{
			FlansMod.LOGGER.warn("VehicleDamageModule for " + vehicle.Def() + " had no 'body' collider");
			DamageDefinitions.put("body", DamageablePartDefinition.DEFAULT_BODY);
		}
	}

	@Override
	public void DefineSyncedData(@Nonnull SynchedEntityData entityData)
	{
		VehicleDataSynchronizer = entityData;
		for(var kvp : DamageDefinitions.entrySet())
		{
			EntityDataAccessor<Float> healthSync = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);
			PER_PART_HEALTH.put(kvp.getKey(), healthSync);
			entityData.define(healthSync, kvp.getValue().maxHealth);
		}
	}

	public float GetHealthOf(@Nonnull String partName)
	{
		EntityDataAccessor<Float> healthAccessor = PER_PART_HEALTH.get(partName);
		if (healthAccessor != null)
		{
			return VehicleDataSynchronizer.get(healthAccessor);
		}
		return 0f;
	}
	public void SetHealthOf(@Nonnull String partName, float health)
	{
		EntityDataAccessor<Float> healthAccessor = PER_PART_HEALTH.get(partName);
		if (healthAccessor != null)
		{
			VehicleDataSynchronizer.set(healthAccessor, health);
		}
	}
	@Nonnull
	public DamageablePartDefinition GetDef(@Nonnull String partName) { return DamageDefinitions.getOrDefault(partName, DamageablePartDefinition.DEFAULT_BODY); }
	public float GetMaxHealthOf(@Nonnull String partName) { return GetDef(partName).maxHealth; }
	public float GetMaxHealth()
	{
		float maxHealth = 0.0f;
		for(DamageablePartDefinition def : DamageDefinitions.values())
			maxHealth += def.maxHealth;
		return maxHealth;
	}
	public float GetTotalHealth()
	{
		float totalHealth = 0.0f;
		for(var accessor : PER_PART_HEALTH.values())
			totalHealth += VehicleDataSynchronizer.get(accessor);
		return totalHealth;
	}
	public boolean HasPart(@Nonnull String partName)
	{
		return DamageDefinitions.containsKey(partName);
	}
	@Nonnull
	public String OrElseCore(@Nullable String partName)
	{
		return (partName != null && DamageDefinitions.containsKey(partName)) ? partName : VehicleDefinition.CoreName;
	}
	public boolean IsPartDestroyed(@Nonnull String partName)
	{
		return HasPart(partName) && GetHealthOf(partName) <= 0.0f;
	}
	public float ApplyDamageTo(@Nonnull String partName, @Nonnull DamageSource source, float amount)
	{
		String partToDamage = OrElseCore(partName);
		float damageToDeal = Maths.Max(GetHealthOf(partToDamage) - amount, 0.0f);
		SetHealthOf(partToDamage, damageToDeal);
		return amount - damageToDeal;
	}
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}



	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(DamageDefinitions.containsKey(key))
			{
				CompoundTag partTags = tags.getCompound(key);
				SetHealthOf(key, partTags.getFloat("hp"));
			}
			else FlansMod.LOGGER.warn("Damage key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : DamageDefinitions.entrySet())
		{
			CompoundTag partTags = new CompoundTag();
			partTags.putFloat("hp", GetHealthOf(kvp.getKey()));
			tags.put(kvp.getKey(), partTags);
		}
		return tags;
	}
}
