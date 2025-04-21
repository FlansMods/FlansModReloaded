package com.flansmod.airtraffic.api.server;

import com.flansmod.airtraffic.api.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public interface IAirTrafficServer
{
	boolean isSegmentLoaded(@Nonnull AirmapPos airmapPos);
	boolean doesSegmentExistOnDisk(@Nonnull AirmapPos airmapPos);
	@Nullable IAirmapSegment tryLoadExistingSegment(@Nonnull AirmapPos airmapPos);

	// Airports
	@Nonnull AirmapPos locateAirportID(@Nonnull UUID airportID);
	@Nullable IAirportConst loadAirportData(@Nonnull UUID airportID);
	@Nullable IAirport loadAirportDataForEdit(@Nonnull UUID airportID);
	void editAirport(@Nonnull UUID airportID, @Nonnull Consumer<IAirport> editFunc);
	@Nonnull UUID generateSeededAirportID(@Nonnull AirmapPos pos, long seed);
	@Nonnull default UUID generateAirportID(@Nonnull AirmapPos pos) { return generateSeededAirportID(pos, 0L); }

	// Runways
	@Nonnull AirmapPos locateRunwayID(@Nonnull UUID runwayID);
	@Nullable IRunwayConst loadRunwayData(@Nonnull UUID runwayID);
	@Nullable IRunway loadRunwayDataForEdit(@Nonnull UUID runwayID);
	void editRunway(@Nonnull UUID runwayID, @Nonnull Consumer<IRunway> editFunc);
	@Nonnull UUID generateSeededRunwayID(@Nonnull AirmapPos pos, long seed);
	@Nonnull default UUID generateRunwayID(@Nonnull AirmapPos pos) { return generateSeededAirportID(pos, 0L); }



	//@Nullable AirmapPos locateFlightPathOrigin(@Nonnull UUID )
}
