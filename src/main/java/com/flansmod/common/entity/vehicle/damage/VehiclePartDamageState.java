package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import org.checkerframework.checker.units.qual.Current;

import javax.annotation.Nonnull;

public class VehiclePartDamageState implements IVehicleSaveNode
{
	@Nonnull
	public final DamageablePartDefinition Def;

	public float CurrentHealth;

	public boolean IsDestroyed() { return CurrentHealth <= 0.0f; }

	public VehiclePartDamageState(@Nonnull DamageablePartDefinition def)
	{
		Def = def;
		CurrentHealth = Def.maxHealth;
	}

	public float ApplyDamage(@Nonnull DamageSource source, float amount)
	{
		float amountToDeal = Maths.Min(CurrentHealth, amount);
		CurrentHealth -= amountToDeal;
		return amountToDeal;
	}

	public float Heal(float amount)
	{
		float amountToHeal = Maths.Min(Def.maxHealth - CurrentHealth, amount);
		CurrentHealth += amountToHeal;
		return amountToHeal;
	}


	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("hp"))
			CurrentHealth = tags.getFloat("hp");
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		tags.putFloat("hp", CurrentHealth);
		return tags;
	}
}
