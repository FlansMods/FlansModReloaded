package com.flansmod.airtraffic.api.client;

import com.flansmod.airtraffic.api.AirmapPos;
import com.flansmod.airtraffic.api.IFlight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IAirTrafficClient
{
	int getNumKnownFlights();
	@Nullable UUID getKnownFlightID(int index);
	@Nullable IFlight getKnownFlight(int index);


	boolean requestAirmapInfo(@Nonnull AirmapPos pos);
	default boolean requestAirmapInfo(@Nonnull ChunkPos chunkPos) {
		return requestAirmapInfo(new AirmapPos(chunkPos));
	}
	default boolean requestAirmapInfo(@Nonnull BlockPos blockPos) {
		return requestAirmapInfo(new AirmapPos(blockPos));
	}
}
