package com.flansmod.airtraffic.api.server;

import com.flansmod.airtraffic.api.IFlight;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface IFlyerHandlerServer
{


	boolean onEnterLoadedChunks(@Nonnull IFlight flight, @Nonnull Level level, @Nonnull ChunkPos enteredChunk);
	boolean beforeExitLoadedChunks(@Nonnull IFlight flight, @Nonnull Level level, @Nonnull ChunkPos enteredChunk);

}
