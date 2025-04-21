package com.flansmod.airtraffic.api;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IAirportConst
{
	UUID INVALID_AIRPORT_ID = new UUID(0L, 0L);

	@Nonnull String getName();
	@Nonnull BlockPos getControllerBlockPos();
	int getNumRunways();
	@Nonnull UUID getRunwayID(int index);
	@Nullable RunwayCheckResult getLargestRunway();


	@Nullable IRunwayConst tryLoadRunway(int index);

	// Permissions
	@Nonnull UUID getOwnerPlayerID();
}
