package com.flansmod.airtraffic.common.airmap;

import com.flansmod.airtraffic.api.*;
import com.flansmod.airtraffic.common.airport.AirportInstance;
import com.flansmod.airtraffic.common.runway.RunwayInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AirmapSegment implements IAirmapSegment
{
	public final AirmapPos airmapPos;
	public List<UUID> airportIDs = new ArrayList<>();
	public Map<UUID, AirportInstance> loadedAirportData = new HashMap<>();
	public List<UUID> runwayIDs = new ArrayList<>();
	public Map<UUID, RunwayInstance> loadedRunwayData = new HashMap<>();


	public AirmapSegment(@Nonnull AirmapPos pos)
	{
		airmapPos = pos;
	}

	@Override @Nonnull
	public AirmapPos getPos() { return airmapPos; }

	// -- Airports --
	@Override
	public int getNumAirports() { return airportIDs.size(); }
	@Override @Nonnull
	public UUID getAirportID(int index) { return airportIDs.get(index);	}
	@Override @Nullable
	public IAirport tryLoadAirportForEdit(@Nonnull UUID airportID)
	{
		if(!loadedAirportData.containsKey(airportID))
		{

		}

		return loadedAirportData.get(airportID);
	}
	@Override @Nullable
	public IAirportConst tryLoadAirport(@Nonnull UUID airportID)
	{
		return tryLoadAirportForEdit(airportID);
	}
	@Override
	public boolean trySaveAirport(@Nonnull UUID airportID, @Nonnull IAirport airportData)
	{
		loadedAirportData.put(airportID, airportData);
		// TODO: To disk
		return true;
	}

	// -- Runways --
	@Override
	public int getNumRunways() { return runwayIDs.size(); }
	@Override @Nonnull
	public UUID getRunwayID(int index) { return runwayIDs.get(index);	}
	@Override @Nullable
	public IRunway tryLoadRunway(@Nonnull UUID runwayID)
	{
		if(!loadedRunwayData.containsKey(runwayID))
		{

		}

		return loadedRunwayData.get(runwayID);
	}


	// Save/Load
	public void saveTo(@Nonnull CompoundTag tags)
	{
		ListTag airportListTag = new ListTag();
		for(UUID airportID : airportIDs)
		{
			CompoundTag airportTag = new CompoundTag();
			airportTag.putUUID("id", airportID);
			if(loadedAirportData.containsKey(airportID))
			{
				loadedAirportData.get(airportID).saveTo(airportTag);
			}
			airportListTag.add(airportTag);
		}
		tags.put("airports", airportListTag);

		ListTag runwayListTag = new ListTag();
		for(UUID runwayID : runwayIDs)
		{
			CompoundTag runwayTag = new CompoundTag();
			runwayTag.putUUID("id", runwayID);
			if(loadedRunwayData.containsKey(runwayID))
			{
				loadedRunwayData.get(runwayID).saveTo(runwayTag);
			}
			runwayListTag.add(runwayTag);
		}
		tags.put("runways", runwayListTag);
	}
	public void loadFrom(@Nonnull CompoundTag tags)
	{
		runwayIDs.clear();
		loadedRunwayData.clear();
		ListTag runwayListTag = tags.getList("runways", 10);
		for(Tag tag : runwayListTag)
		{
			if(tag instanceof CompoundTag runwayTag)
			{
				UUID id = runwayTag.getUUID("id");
				runwayIDs.add(id);
				RunwayInstance instance = new RunwayInstance(id);
				instance.loadFrom(runwayTag);
				loadedRunwayData.put(id, instance);
			}
		}
		airportIDs.clear();
		loadedAirportData.clear();
		ListTag airportListTag = tags.getList("airports", 10);
		for(Tag tag : airportListTag)
		{
			if(tag instanceof CompoundTag airportTag)
			{
				UUID id = airportTag.getUUID("id");
				airportIDs.add(id);
				AirportInstance instance = new AirportInstance();
				instance.loadFrom(airportTag);
				loadedAirportData.put(id, instance);
			}
		}
	}

}
