package com.flansmod.common.actions.contexts;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.VehicleInventory;
import com.flansmod.common.entity.vehicle.VehicleSubContainer;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ShooterContextVehicle extends ShooterContext
{
	@Nonnull
	protected final VehicleEntity Vehicle;

	@Nonnull
	public VehicleDefinition VehicleDef()
	{
		return Vehicle.Def();
	}

	public ShooterContextVehicle(@Nonnull VehicleEntity vehicle)
	{
		Vehicle = vehicle;
	}


	@Nonnull
	public ShooterContextVehicleSeat GetSeat(@Nonnull VehicleComponentPath seatPath)
	{
		return new ShooterContextVehicleSeat(Vehicle, seatPath);
	}

	@Override
	@Nonnull
	public Entity Entity()
	{
		return Vehicle;
	}
	@Override
	@Nonnull
	public UUID[] GetAllGunIDs() { return Vehicle.GetAllGunIDs().toArray(new UUID[0]); }
	@Nonnull
	@Override
	public UUID GetGunIDForSlot(int gunSlotIndex) { return Vehicle.GetGunIDAtIndex(gunSlotIndex); }
	@Nonnull
	@Override
	public GunContext CreateContext(@Nonnull UUID gunID)
	{
		int gunSlotIndex = Vehicle.GetIndexOfGunID(gunID);
		if(gunSlotIndex != -1)
		{
			return new GunContextVehicle(this, gunSlotIndex);
		}
		return GunContext.INVALID;
	}
	@Override
	@Nullable
	public VehicleInventory GetAttachedInventory()
	{
		return Vehicle.Inventory();
	}
	public int GunSlotIndex(int gunIndex) { return Vehicle.Inventory().SubIndexToContainer(VehicleSubContainer.EType.Gun, gunIndex); }
	public int AmmoSlotIndex(int ammoIndex) { return Vehicle.Inventory().SubIndexToContainer(VehicleSubContainer.EType.Ammo, ammoIndex); }
	public int FuelSlotIndex(int fuelIndex) { return Vehicle.Inventory().SubIndexToContainer(VehicleSubContainer.EType.Fuel, fuelIndex); }

	@Nonnull
	public ItemStack GetGunStack(int gunIndex)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean IsValid() { return !Vehicle.isRemoved(); }
	@Override
	public boolean IsCreative() { return false; }



}
