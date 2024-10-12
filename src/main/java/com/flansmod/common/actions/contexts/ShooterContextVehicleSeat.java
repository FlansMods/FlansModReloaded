package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShooterContextVehicleSeat extends ShooterContextVehicle
{
	@Nonnull
	public final VehicleComponentPath SeatPath;
	@Nullable
	public final SeatDefinition SeatDef;

	public ShooterContextVehicleSeat(@Nonnull VehicleEntity vehicle, @Nonnull VehicleComponentPath seatPath)
	{
		super(vehicle);
		SeatPath = seatPath;
		SeatDef = VehicleDef().AsHierarchy().FindSeat(SeatPath).orElse(SeatDefinition.INVALID);
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
			return Transform.fromPos(passenger.position());
		}
		return Transform.fromPos(Vehicle.position());
	}

	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker)
	{

	}


	@Nullable
	public Entity EntityInThisSeat()
	{
		return Vehicle.GetPassengerInSeat(SeatPath, Vehicle.getPassengers());
	}
	public boolean IsOccupied() { return EntityInThisSeat() != null; }
}
