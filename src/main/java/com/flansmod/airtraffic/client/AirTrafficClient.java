package com.flansmod.airtraffic.client;

import com.flansmod.airtraffic.api.AirmapPos;
import com.flansmod.airtraffic.api.IFlight;
import com.flansmod.airtraffic.api.client.IAirTrafficClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AirTrafficClient implements IAirTrafficClient
{
	private record AirmapSegmentClientInfo(
		boolean requested)
	{

	}
	private record FlightClientInfo(
		boolean requested) implements IFlight
	{

	}

	public Map<AirmapPos, AirmapSegmentClientInfo> clientAirmap = new HashMap<>();
	public List<UUID> knownFlightIDs = new ArrayList<>();
	public Map<UUID, FlightClientInfo> extraFlightInfo = new HashMap<>();

	@Override
	public int getNumKnownFlights() { return knownFlightIDs.size(); }
	@Override @Nullable
	public UUID getKnownFlightID(int index) { return knownFlightIDs.get(index); }
	@Override @Nullable
	public IFlight getKnownFlight(int index)
	{
		UUID flightID = getKnownFlightID(index);
		if(flightID != null)
			return extraFlightInfo.get(flightID);
		return null;
	}


	@Override
	public boolean requestAirmapInfo(@Nonnull AirmapPos pos)
	{
		// Send msg
		return true;
	}
}
