package com.flansmod.airtraffic.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IAirmapSegment
{
	@Nonnull AirmapPos getPos();
	//boolean connectsTo(@Nonnull RotationSegment rotation);


	// -- Airports --
	int getNumAirports();
	@Nonnull UUID getAirportID(int index);
	@Nullable IAirportConst tryLoadAirport(@Nonnull UUID airportID);
	@Nullable IAirport tryLoadAirportForEdit(@Nonnull UUID airportID);
	default boolean containsAirportID(@Nonnull UUID airportID)
	{
		for(int i = 0; i < getNumAirports(); i++)
			if(airportID.equals(getAirportID(i)))
				return true;
		return false;
	}
	boolean trySaveAirport(@Nonnull UUID airportID, @Nonnull IAirport airportData);

	// -- Runways --
	int getNumRunways();
	@Nonnull UUID getRunwayID(int index);
	@Nullable IRunwayConst tryLoadRunway(@Nonnull UUID runwayID);
	@Nullable IRunway tryLoadRunwayForEdit(@Nonnull UUID runwayID);
	default boolean containsRunwayID(@Nonnull UUID runwayID)
	{
		for(int i = 0; i < getNumRunways(); i++)
			if(runwayID.equals(getRunwayID(i)))
				return true;
		return false;
	}
	boolean trySaveRunway(@Nonnull UUID runwayID, @Nonnull IRunway runwayData);

}
