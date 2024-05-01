package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import com.flansmod.common.types.vehicles.elements.VehicleControlOptionDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.ldap.Control;
import java.util.*;

public class VehicleSeatsModule implements IVehicleModule
{
	public static final int INVALID_SEAT_INDEX = -1;

	@Nonnull
	public final List<String> SeatOrdering = new ArrayList<>();
	@Nonnull
	public final Map<String, VehicleSeatSaveState> SeatStates = new HashMap<>();

	public VehicleSeatsModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
							  @Nonnull VehicleEntity vehicle)
	{
		hierarchy.ForEachSeat((seatDef) -> {
				SeatStates.put(seatDef.attachedTo, new VehicleSeatSaveState(seatDef));
		});

		SeatOrdering.addAll(SeatStates.keySet());
	}

	@Nonnull
	protected VehicleSeatSaveState GetSeat(@Nonnull String vehiclePart) {
		return SeatStates.getOrDefault(vehiclePart, VehicleSeatSaveState.INVALID);
	}
	@Nonnull
	protected VehicleSeatSaveState GetSeat(int seatIndex) {
		return SeatStates.getOrDefault(SeatOrdering.get(seatIndex), VehicleSeatSaveState.INVALID);
	}
	@Nullable
	public Entity GetPassengerInSeat(int seatIndex)
	{
		return GetSeat(seatIndex).Passenger;
	}



	public int GetSeatIndexForNewPassenger(@Nonnull Entity passenger)
	{
		// TODO:
		return INVALID_SEAT_INDEX;
	}
	// Minecraft needs this, but our vehicles are more complex, other seats might control sub-pieces
	public int GetControlSeatIndex()
	{
		return INVALID_SEAT_INDEX;
	}

	@Nonnull
	public List<ControlSchemeDefinition> GetAllValidControllers()
	{
		List<ControlSchemeDefinition> list = new ArrayList<>();
		for(var seatState : SeatStates.values())
			for(ControlSchemeDefinition controlScheme : seatState.Def.Controllers.get().values())
			{
				if(!list.contains(controlScheme))
					list.add(controlScheme);
			}
		return list;
	}
	@Nonnull
	public Collection<ControlSchemeDefinition> GetValidControllersForSeat(@Nonnull String seatName)
	{
		VehicleSeatSaveState seatState = SeatStates.get(seatName);
		if(seatState != null)
			return seatState.Def.Controllers.get().values();
		return List.of();
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetActiveControllersForSeat(@Nonnull String seatName,
																	 @Nonnull Map<String, String> modes)
	{
		VehicleSeatSaveState seatState = SeatStates.get(seatName);
		if(seatState != null)
		{
			List<ControlSchemeDefinition> matches = new ArrayList<>();
			for(VehicleControlOptionDefinition option : seatState.Def.controllerOptions)
			{
				if(option.Passes(modes))
					matches.add(seatState.Def.Controllers.get().get(option.key));
			}
			return matches;
		}
		return List.of();
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetAllActiveControllers(@Nonnull Map<String, String> modes)
	{
		List<ControlSchemeDefinition> matches = new ArrayList<>();
		for(VehicleSeatSaveState seatState : SeatStates.values())
		{
			for(VehicleControlOptionDefinition option : seatState.Def.controllerOptions)
			{
				if(option.Passes(modes))
				{
					ControlSchemeDefinition scheme = seatState.Def.Controllers.get().get(option.key);
					if(scheme != null && !matches.contains(scheme))
						matches.add(scheme);
				}
			}
		}
		return matches;
	}
	@Nullable
	public ControlSchemeDefinition GetMainActiveController(@Nonnull Map<String, String> modes)
	{
		for (String seatName : SeatOrdering)
		{
			List<ControlSchemeDefinition> activeForSeat = GetActiveControllersForSeat(seatName, modes);
			if(activeForSeat.size() > 1)
				FlansMod.LOGGER.warn("Seat " + seatName + " has more than 1 control scheme active?");
			if(activeForSeat.size() > 0)
				return activeForSeat.get(0);
		}
		return null;
	}


	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{
		for(int i = 0; i < SeatOrdering.size(); i++)
		{
			String seatName = SeatOrdering.get(i);
			VehicleSeatSaveState seatState = SeatStates.get(seatName);

			// If there is someone in this seat, process inputs
			Entity passenger = GetPassengerInSeat(i);
			if(passenger != null) // TODO: && passenger.canDriveVehicle
			{
				// Process any control schemes that are active (e.g. CarController, 1AxisTurretController)
				List<ControlSchemeDefinition> controlSchemes = GetActiveControllersForSeat(seatName, vehicle.ModalStates);
				for(ControlSchemeDefinition scheme : controlSchemes)
				{
					VehicleInputState inputs = vehicle.GetInputStateFor(scheme);

					//inputs.updateFrom(...)

					// TODO:
				}

				// And additional inputs (these are like opening one door or simple things that don't need a whole control scheme)
				for(InputDefinition additionalInput : seatState.Def.inputs)
				{
					VehicleInputState inputs = vehicle.GetMiscInputState();

					// TODO:


				}
			}



		}
	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(SeatStates.containsKey(key))
			{
				SeatStates.get(key).Load(vehicle, tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Seat key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : SeatStates.entrySet())
		{
			CompoundTag seatTags = kvp.getValue().Save(vehicle);
			tags.put(kvp.getKey(), seatTags);
		}
		return tags;
	}
}
