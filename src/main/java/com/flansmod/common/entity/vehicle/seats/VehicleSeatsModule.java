package com.flansmod.common.entity.vehicle.seats;

import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import com.flansmod.common.types.vehicles.elements.VehicleControlOptionDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class VehicleSeatsModule implements IVehicleModule
{
	public static final int INVALID_SEAT_INDEX = -1;
	public static final String INVALID_SEAT_PATH = "body/seat_-1";
	public static final EntityDataSerializer<PerPartMap<VehicleSeatSaveState>> SEATS_SERIALIZER =
		PerPartMap.SERIALIZER(VehicleSeatSaveState.SERIALIZER);
	public static final EntityDataAccessor<PerPartMap<VehicleSeatSaveState>> SEATS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, SEATS_SERIALIZER);



	@Nonnull
	public final List<String> SeatOrdering = new ArrayList<>();
	@Nonnull
	public final Map<String, SeatDefinition> SeatDefs = new HashMap<>();
	@Nonnull
	private final SynchedEntityData VehicleDataSynchronizer;



	public VehicleSeatsModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
							  @Nonnull VehicleEntity vehicle)
	{
		hierarchy.ForEachSeat(SeatDefs::put);
		SeatOrdering.addAll(SeatDefs.keySet());
		VehicleDataSynchronizer = vehicle.getEntityData();
	}

	@Nonnull
	public PerPartMap<VehicleSeatSaveState> GetSeatSaveData() { return VehicleDataSynchronizer.get(SEATS_ACCESSOR); }
	public void SetSeatSaveData(@Nonnull PerPartMap<VehicleSeatSaveState> map) { VehicleDataSynchronizer.set(SEATS_ACCESSOR, map); }


	// Seat functions by SeatPath
	@Nonnull
	protected VehicleSeatSaveState GetSeat(@Nonnull String seatPath)
	{
		return GetSeatSaveData().GetOrDefault(seatPath, VehicleSeatSaveState.INVALID);
	}
	@Nonnull
	public UUID GetPassengerIDInSeat(@Nonnull String seatPath)
	{
		return GetSeat(seatPath).PassengerID;
	}
	@Nullable
	public Entity GetPassengerInSeat(@Nonnull String seatPath, @Nonnull List<Entity> passengers)
	{
		UUID id = GetPassengerIDInSeat(seatPath);
		for(Entity entity : passengers)
			if(entity.getUUID().equals(id))
				return entity;
		return null;
	}
	@Nonnull
	public String GetControlSeatPath()
	{
		for(int i = 0; i < SeatOrdering.size(); i++)
		{
			//if(SeatStates.get(SeatOrdering.get(i)).IsController())
			{
				return SeatOrdering.get(i);
			}
		}
		return INVALID_SEAT_PATH;
	}


	// Seat functions by Index
	@Nonnull
	protected VehicleSeatSaveState GetSeat(int seatIndex)
	{
		return GetSeat(SeatOrdering.get(seatIndex));
	}
	public int GetSeatIndexOf(@Nonnull Entity entity)
	{
		for(int i = 0; i < SeatOrdering.size(); i++)
		{
			if(GetSeat(i).PassengerID == entity.getUUID())
				return i;
		}
		return INVALID_SEAT_INDEX;
	}
	public int GetSeatIndexForNewPassenger(@Nonnull Entity passenger)
	{
		for(int i = 0; i < SeatDefs.size(); i++)
		{
			if(GetSeat(i).IsEmpty())
				return i;
		}
		return INVALID_SEAT_INDEX;
	}
	// Minecraft needs this, but our vehicles are more complex, other seats might control sub-pieces
	public int GetControlSeatIndex()
	{
		return INVALID_SEAT_INDEX;
	}


	@Nullable
	public Entity GetControllingPassenger(@Nonnull VehicleEntity vehicle)
	{
		return GetPassengerInSeat(GetControlSeatPath(), vehicle.getPassengers());
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetAllValidControllers()
	{
		List<ControlSchemeDefinition> list = new ArrayList<>();
		for(var seatDef : SeatDefs.values())
			for(ControlSchemeDefinition controlScheme : seatDef.Controllers.get().values())
			{
				if(!list.contains(controlScheme))
					list.add(controlScheme);
			}
		return list;
	}
	@Nonnull
	public Collection<ControlSchemeDefinition> GetValidControllersForSeat(@Nonnull String seatName)
	{
		SeatDefinition seatDef = SeatDefs.get(seatName);
		if(seatDef != null)
			return seatDef.Controllers.get().values();
		return List.of();
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetActiveControllersForSeat(@Nonnull String seatName,
																	 @Nonnull Map<String, String> modes)
	{
		SeatDefinition seatDef = SeatDefs.get(seatName);
		if(seatDef != null)
		{
			List<ControlSchemeDefinition> matches = new ArrayList<>();
			for(VehicleControlOptionDefinition option : seatDef.controllerOptions)
			{
				if(option.Passes(modes))
					matches.add(seatDef.Controllers.get().get(option.key));
			}
			return matches;
		}
		return List.of();
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetAllActiveControllers(@Nonnull Map<String, String> modes)
	{
		List<ControlSchemeDefinition> matches = new ArrayList<>();
		for(SeatDefinition seatDef : SeatDefs.values())
		{
			for(VehicleControlOptionDefinition option : seatDef.controllerOptions)
			{
				if(option.Passes(modes))
				{
					ControlSchemeDefinition scheme = seatDef.Controllers.get().get(option.key);
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

	@OnlyIn(Dist.CLIENT)
	public void Client_GetLocalPlayerInputs(@Nonnull VehicleEntity vehicle,
											@Nonnull List<ControlSchemeDefinition> controlSchemes,
											@Nonnull InputDefinition[] additionalInputs)
	{
		// Process any control schemes that are active (e.g. CarController, 1AxisTurretController)
		for(ControlSchemeDefinition scheme : controlSchemes)
		{
			VehicleInputState inputs = vehicle.GetInputStateFor(scheme);
			for(ControlSchemeAxisDefinition axis : scheme.axes)
			{
				float value = ClientInputHooks.GetInput(axis.axisType);
				inputs.SetInput(axis.axisType, value);
			}
		}
		for(InputDefinition input : additionalInputs)
		{
			VehicleInputState miscInputs = vehicle.GetMiscInputState();
			miscInputs.SetInput(input.key, ClientInputHooks.GetInput(input.key));
		}
	}

	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{
		Map<ControlSchemeDefinition, VehicleInputState> statesToTick = new HashMap<>();
		for(int i = 0; i < SeatOrdering.size(); i++)
		{
			String seatPath = SeatOrdering.get(i);
			SeatDefinition seatDef = SeatDefs.get(seatPath);

			if(seatDef != null)
			{
				// If there is someone in this seat, process inputs
				Entity passenger = GetPassengerInSeat(seatPath, vehicle.getPassengers());
				if (passenger != null) // TODO: && passenger.canDriveVehicle
				{
					if (passenger instanceof Player player && player.isLocalPlayer())
					{
						List<ControlSchemeDefinition> controlSchemes = GetActiveControllersForSeat(seatPath, vehicle.ModalStates);

						Client_GetLocalPlayerInputs(vehicle, controlSchemes, seatDef.inputs);

						for (ControlSchemeDefinition scheme : controlSchemes)
						{
							statesToTick.put(scheme, vehicle.GetInputStateFor(scheme));
						}
					}
					//else if(isControlledByAI())
					//{
//
					//}

				}
			}
		}

		for(var kvp : statesToTick.entrySet())
		{
			kvp.getValue().Tick(kvp.getKey());
		}
	}
	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		PerPartMap<VehicleSeatSaveState> seatMap = GetSeatSaveData();
		for(String key : tags.getAllKeys())
		{
			if(SeatDefs.containsKey(key))
			{
				VehicleSeatSaveState seatState = new VehicleSeatSaveState();
				seatState.Load(vehicle, tags.getCompound(key));
				seatMap.Put(key, seatState);
			}
			else FlansMod.LOGGER.warn("Seat key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
		SetSeatSaveData(seatMap);
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		PerPartMap<VehicleSeatSaveState> map = GetSeatSaveData();
		CompoundTag tags = new CompoundTag();
		for (var kvp : SeatDefs.entrySet())
		{
			String partName = kvp.getKey();
			if(map.Values.containsKey(partName.hashCode()))
				tags.put(partName, map.Values.get(partName.hashCode()).Save(vehicle));
		}
		return tags;
	}
}
