package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VehicleEngineModule implements IVehicleModule
{
	@Nonnull
	public final EngineDefinition DefaultEngineDef;
	@Nonnull
	public Map<String, VehicleEngineSaveState> Engines = new HashMap<>();


	public VehicleEngineModule(@Nonnull VehicleEntity vehicle)
	{
		DefaultEngineDef = vehicle.Def().defaultEngine;
	}

	@Nonnull
	public EngineDefinition GetEngine(@Nonnull String key)
	{
		if(Engines.containsKey(key))
			return Engines.get(key).GetEngineOrDefault();
		return DefaultEngineDef;
	}

	public boolean CanThrust(@Nullable Player player, @Nonnull String engineKey)
	{
		if(player != null && player.isCreative())
			return true;

		if(Engines.containsKey(engineKey))
		{
			return Engines.get(engineKey).CanThrust();
		}
		return false; // TODO:
	}

	public void SetEngineOff(@Nonnull String engineKey)
	{
		if(Engines.containsKey(engineKey))
			Engines.get(engineKey).SetOff();
	}
	public void SetEngineIdle(@Nonnull String engineKey)
	{
		if(Engines.containsKey(engineKey))
			Engines.get(engineKey).SetIdle();
	}
	public void SetEngineFull(@Nonnull String engineKey)
	{
		if(Engines.containsKey(engineKey))
			Engines.get(engineKey).SetFull();
	}
	public void SetEnginePower(@Nonnull String engineKey, float parameter)
	{
		if(Engines.containsKey(engineKey))
			Engines.get(engineKey).SetBetweenIdleAndFull(parameter);
	}

	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(Engines.containsKey(key))
			{
				Engines.get(key).Load(vehicle, tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Engine key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : Engines.entrySet())
		{
			CompoundTag seatTags = kvp.getValue().Save(vehicle);
			tags.put(kvp.getKey(), seatTags);
		}
		return tags;
	}
}
