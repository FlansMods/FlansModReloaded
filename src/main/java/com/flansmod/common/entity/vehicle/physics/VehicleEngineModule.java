package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VehicleEngineModule implements IVehicleModule
{
	public static final EntityDataSerializer<PerPartMap<VehicleEngineSaveState>> ENGINES_SERIALIZER =
		PerPartMap.SERIALIZER(VehicleEngineSaveState.SERIALIZER);
	public static final EntityDataAccessor<PerPartMap<VehicleEngineSaveState>> ENGINES_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, ENGINES_SERIALIZER);

	@Nonnull
	public final EngineDefinition DefaultEngineDef;
	@Nonnull
	private final SynchedEntityData VehicleDataSynchronizer;
	public final List<String> EnginePaths = new ArrayList<>();



	public VehicleEngineModule(@Nonnull VehicleEntity vehicle)
	{
		DefaultEngineDef = vehicle.Def().defaultEngine;
		// TODO: vehicle.Def().AsHierarchy.get().ForEachEngine();
		VehicleDataSynchronizer = vehicle.getEntityData();
	}




	@Nonnull
	public PerPartMap<VehicleEngineSaveState> GetEngineSaveData() { return VehicleDataSynchronizer.get(ENGINES_ACCESSOR); }
	public void SetEngineSaveData(@Nonnull PerPartMap<VehicleEngineSaveState> map) { VehicleDataSynchronizer.set(ENGINES_ACCESSOR, map); }
	private void ApplyToSaveState(@Nonnull String enginePath, @Nonnull Consumer<VehicleEngineSaveState> func)
	{
		PerPartMap<VehicleEngineSaveState> map = GetEngineSaveData();
		map.CreateAndApply(enginePath,
			VehicleEngineSaveState::new,
			func);
		SetEngineSaveData(map);
	}


	@Nonnull
	public EngineDefinition GetEngineDef(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetEngine, DefaultEngineDef);
	}
	public float GetThrottle(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetEngineThrottle, 0.0f);
	}
	public int GetBurnTimeRemaining(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetBurnTimeRemaining, 0);
	}
	public int GetBurnTimeDuration(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetBurnTimeDuration, 0);
	}
	public int GetCurrentFE(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetCurrentFE, 0);
	}
	public int GetLiquidAmount(@Nonnull String enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, VehicleEngineSaveState::GetLiquidAmount, 0);
	}
	public int GetNextBurnableSlot(@Nonnull String enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.GetNextBurnableSlot(recipeType), VehicleEngineSaveState.NO_SLOT);
	}
	public int CountBurnTime(@Nonnull String enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.CountBurnTime(recipeType), VehicleEngineSaveState.NO_SLOT);
	}
	public int CountBurnableItems(@Nonnull String enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.CountBurnableItems(recipeType), VehicleEngineSaveState.NO_SLOT);
	}
	public float GetFuelConsumptionRate(@Nonnull String enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		float throttle = GetThrottle(enginePath);
		if(throttle <= VehicleEngineSaveState.ENGINE_IDLE)
			return Maths.LerpF(0f, engine.fuelConsumptionIdle, throttle);
		else
			return Maths.LerpF(engine.fuelConsumptionIdle, engine.fuelConsumptionFull, throttle - 1f);
	}
	public boolean CanThrust(@Nullable Player player, @Nonnull String enginePath)
	{
		if(player != null && player.isCreative())
			return true;

		return CanThrust(enginePath);
	}
	public boolean CanThrust(@Nonnull String enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> GetCurrentFE(enginePath) > 0;
				default -> GetBurnTimeRemaining(enginePath) > 0;
			};
	}
	public boolean CanBurnMoreFuel(@Nonnull String enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> GetCurrentFE(enginePath) > 0;
				case Liquid -> GetLiquidAmount(enginePath) > 0;
				case Smeltable -> GetNextBurnableSlot(enginePath, RecipeType.SMELTING) != VehicleEngineSaveState.NO_SLOT;
				case Smokable -> GetNextBurnableSlot(enginePath, RecipeType.SMOKING) != VehicleEngineSaveState.NO_SLOT;
				case Blastable -> GetNextBurnableSlot(enginePath, RecipeType.BLASTING) != VehicleEngineSaveState.NO_SLOT;
			};
	}
	public int GetFuelFillLevel(@Nonnull String enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> 999;
				case FE -> GetCurrentFE(enginePath);
				case Liquid -> GetLiquidAmount(enginePath);
				case Smeltable -> CountBurnableItems(enginePath, RecipeType.SMELTING);
				case Smokable -> CountBurnableItems(enginePath, RecipeType.SMOKING);
				case Blastable -> CountBurnableItems(enginePath, RecipeType.BLASTING);
			};
	}
	public int GetFuelMaxLevel(@Nonnull String enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> 999;
				case FE -> engine.FECapacity;
				case Liquid -> engine.liquidFuelCapacity;
				case Smeltable -> engine.solidFuelSlots * 64;
				case Smokable -> engine.solidFuelSlots * 64;
				case Blastable -> engine.solidFuelSlots * 64;
			};
	}


	public void SetToOff(@Nonnull String enginePath)
	{
		SetThrottle(enginePath, VehicleEngineSaveState.ENGINE_OFF);
	}
	public void SetToIdle(@Nonnull String enginePath)
	{
		SetThrottle(enginePath, VehicleEngineSaveState.ENGINE_IDLE);
	}
	public void SetToFull(@Nonnull String enginePath)
	{
		SetThrottle(enginePath, VehicleEngineSaveState.ENGINE_MAX);
	}
	public void SetThrottle(@Nonnull String enginePath, float parameter)
	{
		ApplyToSaveState(enginePath, (engineSaveData) -> engineSaveData.Throttle = parameter);
	}







	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}
	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		PerPartMap<VehicleEngineSaveState> engineMap = GetEngineSaveData();
		for(String key : tags.getAllKeys())
		{
			if(EnginePaths.contains(key))
			{
				VehicleEngineSaveState engineState = new VehicleEngineSaveState();
				engineState.Load(vehicle, tags.getCompound(key));
				engineMap.Put(key, engineState);
			}
			else FlansMod.LOGGER.warn("Engine key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
		SetEngineSaveData(engineMap);
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		PerPartMap<VehicleEngineSaveState> map = GetEngineSaveData();
		CompoundTag tags = new CompoundTag();
		for (var enginePath : EnginePaths)
		{
			if(map.Values.containsKey(enginePath.hashCode()))
				tags.put(enginePath, map.Values.get(enginePath.hashCode()).Save(vehicle));
		}
		return tags;
	}
}
