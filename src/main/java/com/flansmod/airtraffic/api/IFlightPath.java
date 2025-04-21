package com.flansmod.airtraffic.api;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IFlightPath
{
	@Nonnull UUID getOriginAirport();
	@Nonnull UUID getDestinationAirport();
	double getFlightLength();
	double getFlightYLevel();
}
