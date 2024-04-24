package com.flansmod.common.entity.vehicle.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.item.GunItem;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.*;

public class VehicleGunModule implements IVehicleModule
{
	@Nonnull
	public final Map<String, VehicleGunSaveState> GunStates = new HashMap<>();
	@Nonnull
	public final List<String> GunOrdering = new ArrayList<>();

	public VehicleGunModule(@Nonnull VehicleDefinitionHeirarchy hierarchy,
							@Nonnull VehicleEntity vehicle)
	{
		hierarchy.ForEachMountedGun((gunDef) -> {
			GunStates.put(gunDef.attachedTo, new VehicleGunSaveState(gunDef));
		});

		GunOrdering.addAll(GunStates.keySet());
	}


	@Nonnull
	public String GetVehiclePartNameOfGunAtIndex(int index) { return GunOrdering.get(index); }
	@Nonnull
	public UUID GetGunIDAtIndex(int index) { return GunStates.get(GunOrdering.get(index)).GetGunID(); }
	public int GetIndexOfGunID(@Nonnull UUID gunID)
	{
		for(int i = 0; i < GunOrdering.size(); i++)
			if(GunStates.get(GunOrdering.get(i)).GetGunID().equals(gunID))
				return i;
		return -1;
	}
	@Nonnull
	public List<UUID> GetAllGunIDs()
	{
		List<UUID> uuids = new ArrayList<>();
		for(VehicleGunSaveState state : GunStates.values())
		{
			UUID id = state.GetGunID();
			if(!id.equals(GunItem.InvalidGunUUID))
				uuids.add(id);
		}
		return uuids;
	}


	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(GunStates.containsKey(key))
			{
				GunStates.get(key).Load(tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Gun key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : GunStates.entrySet())
		{
			tags.put(kvp.getKey(), kvp.getValue().Save());
		}
		return tags;
	}
}
