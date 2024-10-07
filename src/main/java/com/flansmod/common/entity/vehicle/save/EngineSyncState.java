package com.flansmod.common.entity.vehicle.save;

import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EngineSyncState implements IVehicleSaveNode
{
	public static final int NO_SLOT = -1;

	public int BurnTimeDuration = 0;
	public int BurnTimeRemaining = 0;

	// From 0=EngineOff to 1=EngineIdle to 2=EngineMax
	public static final float ENGINE_OFF = 0.0f;
	public static final float ENGINE_IDLE = 1.0f;
	public static final float ENGINE_MAX = 2.0f;
	public float Throttle = 0.0f;


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

	public EngineSyncState()
	{
		EngineStack = ItemStack.EMPTY;
		SolidFuelSlots = new ItemStack[0];
		LiquidType = null;
		LiquidAmount = 0;
		CurrentFE = 0;
	}


	@Nullable
	public EngineDefinition GetEngine()
	{
		PartDefinition part = GetPart();
		return part.IsValid() ? part.engine : null;
	}

	@Nonnull
	public PartDefinition GetPart()
	{
		return (!EngineStack.isEmpty() && EngineStack.getItem() instanceof PartItem part) ? part.Def() : PartDefinition.INVALID;
	}

	public float GetEngineThrottle() { return Throttle; }
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
	public int GetBurnTimeRemaining() { return BurnTimeRemaining; }
	public int GetBurnTimeDuration() { return BurnTimeDuration; }
	public int GetCurrentFE() { return CurrentFE; }
	public int GetLiquidAmount() { return LiquidAmount; }

	public void SetOff() { Throttle = ENGINE_OFF; }
	public void SetIdle() { Throttle = ENGINE_IDLE; }
	public void SetFull() { Throttle = ENGINE_MAX; }
	public void SetBetweenOffAndFull(float parameter)
	{
		Throttle = Maths.LerpF(ENGINE_OFF, ENGINE_MAX, parameter);
	}
	public void SetBetweenIdleAndFull(float parameter)
	{
		Throttle = Maths.LerpF(ENGINE_IDLE, ENGINE_MAX, parameter);
	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("item"))
			EngineStack = ItemStack.of(tags.getCompound("item"));
		BurnTimeRemaining = tags.getInt("burnRemaining");
		BurnTimeDuration = tags.getInt("burnDuration");
		Throttle = tags.getFloat("power");
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		tags.put("item", EngineStack.save(new CompoundTag()));
		tags.putInt("burnRemaining", BurnTimeRemaining);
		tags.putInt("burnDuration", BurnTimeDuration);
		tags.putFloat("power", Throttle);
		return tags;
	}
}
