package com.flansmod.airtraffic.server;

import com.flansmod.airtraffic.api.*;
import com.flansmod.airtraffic.api.server.IAirTrafficServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AirTrafficServer implements IAirTrafficServer
{
	public final LevelAccessor level;

	public AirTrafficServer(@Nonnull LevelAccessor forLevel)
	{
		level = forLevel;
	}

	// -------------------------------------------------------------------------------------------
	// Airmap
	// -------------------------------------------------------------------------------------------
	private record AirmapSegmentReference(
		boolean checkedOnDisk,
		boolean existsOnDisk,
		@Nullable IAirmapSegment loadedSegment)
	{
	}

	public Map<AirmapPos, AirmapSegmentReference> loadedSegments = new HashMap<>();


	public void checkDiskFor(@Nonnull AirmapPos airmapPos)
	{
		loadedSegments.put(airmapPos, new AirmapSegmentReference(true, false, null));
	}
	public void load(@Nonnull AirmapPos airmapPos)
	{

	}
	@Override
	public boolean isSegmentLoaded(@Nonnull AirmapPos airmapPos)
	{
		AirmapSegmentReference ref = loadedSegments.get(airmapPos);
		return ref != null && ref.loadedSegment != null;
	}
	@Override
	public boolean doesSegmentExistOnDisk(@Nonnull AirmapPos airmapPos)
	{
		AirmapSegmentReference ref = loadedSegments.get(airmapPos);
		return ref != null && ref.existsOnDisk;
	}
	@Override @Nullable
	public IAirmapSegment tryLoadExistingSegment(@Nonnull AirmapPos airmapPos)
	{
		if(loadedSegments.containsKey(airmapPos))
		{
			AirmapSegmentReference ref = loadedSegments.get(airmapPos);
			if(ref.loadedSegment != null)
				return ref.loadedSegment;
			if(!ref.checkedOnDisk)
				load(airmapPos);
		}
		return null;
	}
	// -------------------------------------------------------------------------------------------




	// -------------------------------------------------------------------------------------------
	// Airports
	// -------------------------------------------------------------------------------------------
	public static long generateAirportUUID_A(@Nonnull AirmapPos pos)
	{
		return ((long)pos.x() << 32) ^ (long)(pos.z());
	}
	@Override @Nonnull
	public UUID generateSeededAirportID(@Nonnull AirmapPos pos, long seed)
	{
		return new UUID(generateAirportUUID_A(pos), RandomSource.create(seed).nextLong());
	}
	@Override @Nonnull
	public AirmapPos locateAirportID(@Nonnull UUID airportID)
	{
		long pos = airportID.getMostSignificantBits();
		int posX = (int)((pos >> 32) & 0xffffffffL);
		int posZ = (int)(pos & 0xffffffffL);
		return new AirmapPos(posX, posZ);
	}
	@Override @Nullable
	public IAirportConst loadAirportData(@Nonnull UUID airportID)
	{
		return loadAirportDataForEdit(airportID);
	}
	@Override @Nullable
	public IAirport loadAirportDataForEdit(@Nonnull UUID airportID)
	{
		AirmapPos mapPos = locateAirportID(airportID);
		IAirmapSegment mapSegment = tryLoadExistingSegment(mapPos);
		if(mapSegment != null)
		{
			mapSegment.tryLoadAirport(airportID);
		}
		return null;
	}
	@Override
	public void editAirport(@Nonnull UUID airportID, @Nonnull Consumer<IAirport> editFunc)
	{
		AirmapPos mapPos = locateAirportID(airportID);
		IAirmapSegment mapSegment = tryLoadExistingSegment(mapPos);
		if(mapSegment != null)
		{
			IAirport editableAirport = mapSegment.tryLoadAirportForEdit(airportID);
			if(editableAirport != null)
			{
				editFunc.accept(editableAirport);
				mapSegment.trySaveAirport(airportID, editableAirport);
			}
		}
	}


	// -------------------------------------------------------------------------------------------



	// -------------------------------------------------------------------------------------------
	// Runways
	// -------------------------------------------------------------------------------------------
	public static long generateRunwayUUID_A(@Nonnull AirmapPos pos)
	{
		return ((long)pos.x() << 32) ^ (long)(pos.z());
	}
	@Override @Nonnull
	public UUID generateSeededRunwayID(@Nonnull AirmapPos pos, long seed)
	{
		return new UUID(generateRunwayUUID_A(pos), RandomSource.create(seed).fork().nextLong());
	}
	@Override @Nonnull
	public AirmapPos locateRunwayID(@Nonnull UUID runwayID)
	{
		long pos = runwayID.getMostSignificantBits();
		int posX = (int)((pos >> 32) & 0xffffffffL);
		int posZ = (int)(pos & 0xffffffffL);
		return new AirmapPos(posX, posZ);
	}
	@Override @Nullable
	public IRunwayConst loadRunwayData(@Nonnull UUID runwayID)
	{
		return loadRunwayDataForEdit(runwayID);
	}
	@Override @Nullable
	public IRunway loadRunwayDataForEdit(@Nonnull UUID runwayID)
	{
		AirmapPos mapPos = locateAirportID(runwayID);
		IAirmapSegment mapSegment = tryLoadExistingSegment(mapPos);
		if(mapSegment != null)
		{
			mapSegment.tryLoadAirport(runwayID);
		}
		return null;
	}
	@Override
	public void editRunway(@Nonnull UUID runwayID, @Nonnull Consumer<IRunway> editFunc)
	{
		AirmapPos mapPos = locateRunwayID(runwayID);
		IAirmapSegment mapSegment = tryLoadExistingSegment(mapPos);
		if(mapSegment != null)
		{
			IRunway editableRunway = mapSegment.tryLoadRunwayForEdit(runwayID);
			if(editableRunway != null)
			{
				editFunc.accept(editableRunway);
				mapSegment.trySaveRunway(runwayID, editableRunway);
			}
		}
	}


	// -------------------------------------------------------------------------------------------

}
