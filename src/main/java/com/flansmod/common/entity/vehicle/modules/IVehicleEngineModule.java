package com.flansmod.common.entity.vehicle.modules;

import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.entity.vehicle.save.EngineSyncState;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IVehicleEngineModule
{
	@Nonnull
	PerPartMap<EngineSyncState> GetEngineSaveData();
	void SetEngineSaveData(@Nonnull PerPartMap<EngineSyncState> map);
	void ModifyEngineSaveData(@Nonnull VehicleComponentPath enginePath, @Nonnull Consumer<EngineSyncState> func);

	@Nonnull
	EngineDefinition GetDefaultEngine();



	//public final List<String> EnginePaths = new ArrayList<>();


	@Nonnull
	default EngineDefinition GetEngineDef(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetEngine, GetDefaultEngine());
	}
	default float GetThrottle(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetEngineThrottle, 0.0f);
	}
	default int GetBurnTimeRemaining(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetBurnTimeRemaining, 0);
	}
	default int GetBurnTimeDuration(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetBurnTimeDuration, 0);
	}
	default int GetCurrentFE(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetCurrentFE, 0);
	}
	default int GetLiquidAmount(@Nonnull VehicleComponentPath enginePath)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, EngineSyncState::GetLiquidAmount, 0);
	}
	default int GetNextBurnableSlot(@Nonnull VehicleComponentPath enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.GetNextBurnableSlot(recipeType), EngineSyncState.NO_SLOT);
	}
	default int CountBurnTime(@Nonnull VehicleComponentPath enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.CountBurnTime(recipeType), EngineSyncState.NO_SLOT);
	}
	default int CountBurnableItems(@Nonnull VehicleComponentPath enginePath, @Nonnull RecipeType<?> recipeType)
	{
		return GetEngineSaveData().ApplyOrDefault(enginePath, (engineSave) -> engineSave.CountBurnableItems(recipeType), EngineSyncState.NO_SLOT);
	}
	default float GetFuelConsumptionRate(@Nonnull VehicleComponentPath enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		float throttle = GetThrottle(enginePath);
		if(throttle <= EngineSyncState.ENGINE_IDLE)
			return Maths.lerpF(0f, engine.fuelConsumptionIdle, throttle);
		else
			return Maths.lerpF(engine.fuelConsumptionIdle, engine.fuelConsumptionFull, throttle - 1f);
	}
	default boolean CanThrust(@Nullable Player player, @Nonnull VehicleComponentPath enginePath)
	{
		if(player != null && player.isCreative())
			return true;

		return CanThrust(enginePath);
	}
	default boolean CanThrust(@Nonnull VehicleComponentPath enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> GetCurrentFE(enginePath) > 0;
				default -> GetBurnTimeRemaining(enginePath) > 0;
			};
	}
	default boolean CanBurnMoreFuel(@Nonnull VehicleComponentPath enginePath)
	{
		EngineDefinition engine = GetEngineDef(enginePath);
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> GetCurrentFE(enginePath) > 0;
				case Liquid -> GetLiquidAmount(enginePath) > 0;
				case Smeltable -> GetNextBurnableSlot(enginePath, RecipeType.SMELTING) != EngineSyncState.NO_SLOT;
				case Smokable -> GetNextBurnableSlot(enginePath, RecipeType.SMOKING) != EngineSyncState.NO_SLOT;
				case Blastable -> GetNextBurnableSlot(enginePath, RecipeType.BLASTING) != EngineSyncState.NO_SLOT;
			};
	}
	default int GetFuelFillLevel(@Nonnull VehicleComponentPath enginePath)
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
	default int GetFuelMaxLevel(@Nonnull VehicleComponentPath enginePath)
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


	default void SetToOff(@Nonnull VehicleComponentPath enginePath)
	{
		SetThrottle(enginePath, EngineSyncState.ENGINE_OFF);
	}
	default void SetToIdle(@Nonnull VehicleComponentPath enginePath)
	{
		SetThrottle(enginePath, EngineSyncState.ENGINE_IDLE);
	}
	default void SetToFull(@Nonnull VehicleComponentPath enginePath)
	{
		SetThrottle(enginePath, EngineSyncState.ENGINE_MAX);
	}
	default void SetThrottle(@Nonnull VehicleComponentPath enginePath, float parameter)
	{
		ModifyEngineSaveData(enginePath, (engineSaveData) -> engineSaveData.Throttle = parameter);
	}

}
