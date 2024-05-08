package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VehicleEngineSaveState implements IVehicleSaveNode
{
	public static final int NO_SLOT = -1;
	@Nonnull
	public final EngineDefinition DefaultEngineDef;

	public int BurnTimeDuration = 0;
	public int BurnTimeRemaining = 0;

	// From 0=EngineOff to 1=EngineIdle to 2=EngineMax
	public static final float ENGINE_OFF = 0.0f;
	public static final float ENGINE_IDLE = 1.0f;
	public static final float ENGINE_MAX = 2.0f;
	public float FuelConsumptionRate = 0.0f;


	@Nonnull
	public ItemStack EngineStack;

	// Solid fuel engines
	@Nonnull
	public ItemStack[] SolidFuelSlots;

	// FE (electric) engines
	public int CurrentFE;

	// Liquid fuel engines
	@Nullable
	public LiquidBlock LiquidType;
	public int LiquidAmount;

	public VehicleEngineSaveState(@Nonnull EngineDefinition defaultEngineDef)
	{
		DefaultEngineDef = defaultEngineDef;
		EngineStack = ItemStack.EMPTY;
		SolidFuelSlots = new ItemStack[0];
		LiquidType = null;
		LiquidAmount = 0;
		CurrentFE = 0;
	}


	@Nonnull
	public EngineDefinition GetEngineOrDefault()
	{
		PartDefinition part = GetPart();
		return part.IsValid() ? part.engine : DefaultEngineDef;
	}

	@Nonnull
	public PartDefinition GetPart()
	{
		return (!EngineStack.isEmpty() && EngineStack.getItem() instanceof PartItem part) ? part.Def() : PartDefinition.INVALID;
	}

	public float GetFuelConsumptionRate()
	{
		EngineDefinition engine = GetEngineOrDefault();
		if(FuelConsumptionRate <= ENGINE_IDLE)
			return Maths.LerpF(0f, engine.fuelConsumptionIdle, FuelConsumptionRate);
		else
			return Maths.LerpF(engine.fuelConsumptionIdle, engine.fuelConsumptionFull, FuelConsumptionRate - 1f);
	}

	public int GetNextBurnableSlot(@Nonnull RecipeType<?> recipeType)
	{
		for (int i = 0; i < SolidFuelSlots.length; i++)
		{
			int burnTime = ForgeHooks.getBurnTime(SolidFuelSlots[i], recipeType);
			if (burnTime > 0)
				return i;
		}
		return NO_SLOT;
	}

	public int CountBurnTime(@Nonnull RecipeType<?> recipeType)
	{
		int burnTime = 0;
		for (ItemStack solidFuelSlot : SolidFuelSlots)
			burnTime += ForgeHooks.getBurnTime(solidFuelSlot, recipeType);
		return burnTime;
	}

	public int CountBurnableItems(@Nonnull RecipeType<?> recipeType)
	{
		int burnables = 0;
		for (ItemStack solidFuelSlot : SolidFuelSlots)
			if (ForgeHooks.getBurnTime(solidFuelSlot, recipeType) > 0)
				burnables += solidFuelSlot.getCount();
		return burnables;
	}

	public int GetFuelFillLevel()
	{
		EngineDefinition engine = GetEngineOrDefault();
		return switch (engine.fuelType)
			{
				case Creative -> 999;
				case FE -> CurrentFE;
				case Liquid -> LiquidAmount;
				case Smeltable -> CountBurnableItems(RecipeType.SMELTING);
				case Smokable -> CountBurnableItems(RecipeType.SMOKING);
				case Blastable -> CountBurnableItems(RecipeType.BLASTING);
			};
	}

	public int GetFuelMaxLevel()
	{
		EngineDefinition engine = GetEngineOrDefault();
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

	public boolean CanBurnMoreFuel()
	{
		EngineDefinition engine = GetEngineOrDefault();
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> CurrentFE > 0;
				case Liquid -> LiquidAmount > 0;
				case Smeltable -> GetNextBurnableSlot(RecipeType.SMELTING) != NO_SLOT;
				case Smokable -> GetNextBurnableSlot(RecipeType.SMOKING) != NO_SLOT;
				case Blastable -> GetNextBurnableSlot(RecipeType.BLASTING) != NO_SLOT;
			};
	}

	public boolean CanThrust()
	{
		EngineDefinition engine = GetEngineOrDefault();
		return switch (engine.fuelType)
			{
				case Creative -> true;
				case FE -> CurrentFE > 0;
				default -> BurnTimeRemaining > 0;
			};
	}

	public void SetOff() { FuelConsumptionRate = ENGINE_OFF; }
	public void SetIdle() { FuelConsumptionRate = ENGINE_IDLE; }
	public void SetFull() { FuelConsumptionRate = ENGINE_MAX; }
	public void SetBetweenOffAndFull(float parameter)
	{
		FuelConsumptionRate = Maths.LerpF(ENGINE_OFF, ENGINE_MAX, parameter);
	}
	public void SetBetweenIdleAndFull(float parameter)
	{
		FuelConsumptionRate = Maths.LerpF(ENGINE_IDLE, ENGINE_MAX, parameter);
	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("item"))
			EngineStack = ItemStack.of(tags.getCompound("item"));
		BurnTimeRemaining = tags.getInt("burnRemaining");
		BurnTimeDuration = tags.getInt("burnDuration");
		FuelConsumptionRate = tags.getFloat("power");
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		tags.put("item", EngineStack.save(new CompoundTag()));
		tags.putInt("burnRemaining", BurnTimeRemaining);
		tags.putInt("burnDuration", BurnTimeDuration);
		tags.putFloat("power", FuelConsumptionRate);
		return tags;
	}
}
