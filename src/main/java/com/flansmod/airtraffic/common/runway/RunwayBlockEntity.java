package com.flansmod.airtraffic.common.runway;

import com.flansmod.airtraffic.api.*;
import com.flansmod.airtraffic.api.server.IAirTrafficServer;
import com.flansmod.airtraffic.common.AirTrafficControlMod;
import com.flansmod.airtraffic.network.AirTrafficControlPacketHandler;
import com.flansmod.airtraffic.network.toserver.ConfigureAirportMessage;
import com.flansmod.airtraffic.network.toserver.ConfigureRunwayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RunwayBlockEntity extends BlockEntity
{
	public static final UUID UnassignedRunwayID = new UUID(0L, 0L);
	public UUID runwayID = UnassignedRunwayID;
	public IRunwayConst runwayData = null;

	public RunwayBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		super(AirTrafficControlMod.RUNWAY_MARKER_TILE_ENTITY.get(), pos, state);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.putUUID("runwayID", runwayID);
	}
	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);
		runwayID = tags.getUUID("runwayID");
	}

	@Nonnull
	public UUID registerRunwayID()
	{
		if(level != null && !level.isClientSide)
		{
			IAirTrafficServer atcServer = ATC.getServer(level);
			if(atcServer != null)
			{
				return atcServer.generateRunwayID(new AirmapPos(getBlockPos()));
			}
		}
		return IAirport.INVALID_AIRPORT_ID;
	}
	public boolean tryLoadAirportData()
	{
		if(level != null && !level.isClientSide)
		{
			IAirTrafficServer atcServer = ATC.getServer(level);
			if (atcServer != null)
			{
				runwayData = atcServer.loadRunwayData(runwayID);
				return runwayData != null;
			}
		}
		return false;
	}



	public void setRunwayName(@Nonnull String newName)
	{
		if(level != null)
		{
			if (level.isClientSide)
			{
				AirTrafficControlPacketHandler.sendToServer(ConfigureRunwayMessage.rename(getBlockPos(), newName));
			}
			else
			{
				IAirTrafficServer atcServer = ATC.getServer(level);
				if(atcServer != null)
				{
					atcServer.editRunway(runwayID, (editableData) -> editableData.setName(newName));
				}
			}
		}
	}

}
