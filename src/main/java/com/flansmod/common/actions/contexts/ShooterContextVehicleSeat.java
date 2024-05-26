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
	@Nonnull
	public final String SeatPath;
	@Nullable
	public final SeatDefinition SeatDef;

	public ShooterContextVehicleSeat(@Nonnull VehicleEntity vehicle, @Nonnull String seatPath)
	{
		super(vehicle);
		SeatPath = seatPath;
		SeatDef = VehicleDef().AsHierarchy.get().FindSeat(SeatPath);
	}

	@Override
	public int GetNumValidContexts()
	{
		// TODO: Calculate how many of this vehicle's guns are referenced by this seat
		return SeatDef.inputs.length;
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
		return Vehicle.Seats().GetPassengerInSeat(SeatPath, Vehicle.getPassengers());
	}
	public boolean IsOccupied() { return EntityInThisSeat() != null; }
}
