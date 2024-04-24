package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ShooterContextVehicleSeat extends ShooterContextVehicle
{
	protected final int SeatIndex;

	@Nonnull
	public SeatDefinition SeatDef()
	{
		VehicleDefinition vehicleDef = VehicleDef();
		if(0 <= SeatIndex && SeatIndex < vehicleDef.seats.length)
		{
			return vehicleDef.seats[SeatIndex];
		}
		return SeatDefinition.INVALID;
	}


	public ShooterContextVehicleSeat(@Nonnull VehicleEntity vehicle, int seatIndex)
	{
		super(vehicle);
		SeatIndex = seatIndex;
	}

	@Override
	public int GetNumValidContexts()
	{
		// TODO: Calculate how many of this vehicle's guns are referenced by this seat
		return SeatDef().inputs.length;
	}
	@Override
	@Nullable
	public Entity Owner()
	{
		return EntityInThisSeat();
	}
	@Nonnull
	@Override
	public Transform GetShootOrigin(float deltaTick)
	{
		// TODO: 3D Transform stack
		Entity passenger = EntityInThisSeat();
		if(passenger != null)
		{
			return Transform.FromPos(passenger.position());
		}
		return Transform.FromPos(Vehicle.position());
	}

	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker)
	{

	}


	@Nullable
	public Entity EntityInThisSeat()
	{
		List<Entity> passengers = Vehicle.getPassengers();
		if(0 <= SeatIndex && SeatIndex < passengers.size())
		{
			return passengers.get(SeatIndex);
		}
		return null;
	}
	public boolean IsOccupied() { return EntityInThisSeat() != null; }
}
