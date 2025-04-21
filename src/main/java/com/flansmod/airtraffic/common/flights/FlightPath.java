package com.flansmod.airtraffic.common.flights;

import com.flansmod.airtraffic.api.IFlightPath;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FlightPath implements IFlightPath
{
	public final UUID fromAirport;
	public final UUID toAirport;
	public double yLevel;
	public double flightLength;

	public FlightPath(@Nonnull UUID from, @Nonnull UUID to)
	{
		fromAirport = from;
		toAirport = to;
	}

	@Override @Nonnull
	public UUID getOriginAirport() { return fromAirport; }
	@Override @Nonnull
	public UUID getDestinationAirport() { return toAirport; }
	@Override
	public double getFlightLength() { return flightLength; }
	@Override
	public double getFlightYLevel() { return yLevel; }

}
