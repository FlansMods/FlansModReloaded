package com.flansmod.common.entity.vehicle.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

import javax.annotation.Nonnull;
import java.util.*;

public class VehicleGunModule implements IVehicleModule
{
	public static final EntityDataAccessor<PerPartMap<GunSyncState>> GUNS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.GUN_MAP);

	@Nonnull
	public final Map<String, MountedGunDefinition> GunDefs = new HashMap<>();
	@Nonnull
	public final List<String> GunOrdering = new ArrayList<>();
	@Nonnull
	private final SynchedEntityData VehicleDataSynchronizer;

	public VehicleGunModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
							@Nonnull VehicleEntity vehicle)
	{
		hierarchy.ForEachMountedGun(GunDefs::put);
		GunOrdering.addAll(GunDefs.keySet());
		VehicleDataSynchronizer = vehicle.getEntityData();
	}

	public void SetGunSaveData(@Nonnull PerPartMap<GunSyncState> map) { VehicleDataSynchronizer.set(GUNS_ACCESSOR, map); }
	@Nonnull
	public PerPartMap<GunSyncState> GetGunSaveData() { return VehicleDataSynchronizer.get(GUNS_ACCESSOR); }
	@Nonnull
	public GunSyncState GetGunStateAtIndex(int index) {
		return GetGunSaveData().GetOrDefault(GunOrdering.get(index), GunSyncState.INVALID);
	}
	public void SetGunState(@Nonnull String partName, @Nonnull GunSyncState gunState)
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		map.Put(partName, gunState);
		SetGunSaveData(map);
	}
	public void SetGunState(int index, @Nonnull GunSyncState gunState)
	{
		SetGunState(GunOrdering.get(index), gunState);
	}

	@Nonnull
	public String GetVehiclePartNameOfGunAtIndex(int index) { return GunOrdering.get(index); }
	@Nonnull
	public UUID GetGunIDAtIndex(int index) { return GetGunStateAtIndex(index).GetGunID(); }
	public int GetIndexOfGunID(@Nonnull UUID gunID)
	{
		for(int i = 0; i < GunOrdering.size(); i++)
			if(GetGunStateAtIndex(i).GetGunID().equals(gunID))
				return i;
		return -1;
	}
	@Nonnull
	public List<UUID> GetAllGunIDs()
	{
		List<UUID> uuids = new ArrayList<>();
		for(GunSyncState state : GetGunSaveData().Values.values())
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
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		for(String key : tags.getAllKeys())
		{
			if(GunDefs.containsKey(key))
			{
				GunSyncState gunState = new GunSyncState();
				gunState.Load(vehicle, tags.getCompound(key));
				map.Put(key, gunState);
			}
			else FlansMod.LOGGER.warn("Gun key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
		SetGunSaveData(map);
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		CompoundTag tags = new CompoundTag();
		for (var kvp : GunDefs.entrySet())
		{
			String partName = kvp.getKey();
			if(map.Values.containsKey(partName.hashCode()))
				tags.put(partName, map.Values.get(partName.hashCode()).Save(vehicle));
		}
		return tags;
	}
}
