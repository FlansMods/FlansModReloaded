package com.flansmod.common.entity.vehicle.modules;

import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.entity.vehicle.save.DamageSyncState;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public interface IVehicleDamageHelper
{
	@Nonnull VehicleDefinitionHierarchy GetHierarchy();
	@Nonnull PerPartMap<DamageSyncState> GetDamageMap();
	void SetDamageMap(@Nonnull PerPartMap<DamageSyncState> map);
	@Nonnull DamageablePartDefinition GetDef(@Nonnull VehicleComponentPath partPath);


	default float GetHealthOf(@Nonnull VehicleComponentPath partName)
	{
		return GetDamageMap().ApplyOrDefault(partName, DamageSyncState::Health, 0f);
	}
	default void SetHealthOf(@Nonnull VehicleComponentPath partName, float health)
	{
		PerPartMap<DamageSyncState> map = GetDamageMap();
		map.Put(partName, new DamageSyncState(health));
		SetDamageMap(map);
	}
	default float GetMaxHealthOf(@Nonnull VehicleComponentPath partName) { return GetDef(partName).maxHealth; }
	default float GetMaxHealth()
	{
		AtomicReference<Float> maxHealth = new AtomicReference<>(0.0f);
		GetHierarchy().ForEachDamageable((partPath, dmgDef) -> {
			maxHealth.updateAndGet(v -> v + dmgDef.maxHealth);
		});

		return maxHealth.get();
	}
	default float GetTotalHealth()
	{
		float totalHealth = 0.0f;
		PerPartMap<DamageSyncState> map = GetDamageMap();
		for(var damageState : map.Values())
			totalHealth += damageState.Health();
		return totalHealth;
	}
	// Might not be enabled!
	@Nonnull
	default DamageablePartDefinition GetCoreDamageDef()
	{
		return GetHierarchy().RootNode.Def.damage;
	}
	default boolean HasDamageablePart(@Nonnull VehicleComponentPath partPath)
	{
		return GetHierarchy().FindDamageable(partPath).isPresent();
	}
	@Nonnull
	default VehicleComponentPath DamageableOrElseCore(@Nonnull VehicleComponentPath partName)
	{
		return HasDamageablePart(partName) ? partName : VehicleComponentPath.coreDamage;
	}
	default boolean IsPartDestroyed(@Nonnull VehicleComponentPath partName)
	{
		return HasDamageablePart(partName) && GetHealthOf(partName) <= 0.0f;
	}
	default float ApplyDamageTo(@Nonnull VehicleComponentPath partName, @Nonnull DamageSource source, float amount)
	{
		VehicleComponentPath partToDamage = DamageableOrElseCore(partName);
		float damageToDeal = Maths.max(GetHealthOf(partToDamage) - amount, 0.0f);
		SetHealthOf(partToDamage, damageToDeal);
		return amount - damageToDeal;
	}
}
