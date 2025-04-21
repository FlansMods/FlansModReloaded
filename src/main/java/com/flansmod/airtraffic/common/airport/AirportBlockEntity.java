package com.flansmod.airtraffic.common.airport;

import com.flansmod.airtraffic.api.ATC;
import com.flansmod.airtraffic.api.AirmapPos;
import com.flansmod.airtraffic.api.IAirport;
import com.flansmod.airtraffic.api.IAirportConst;
import com.flansmod.airtraffic.api.server.IAirTrafficServer;
import com.flansmod.airtraffic.common.AirTrafficControlMod;
import com.flansmod.airtraffic.network.AirTrafficControlPacketHandler;
import com.flansmod.airtraffic.network.toserver.ConfigureAirportMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AirportBlockEntity extends BlockEntity
{
	public UUID airportID = IAirport.INVALID_AIRPORT_ID;
	public IAirportConst airportData = null;

	public AirportBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		super(AirTrafficControlMod.AIRPORT_TILE_ENTITY.get(), pos, state);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tags)
	{
		super.saveAdditional(tags);

		tags.putUUID("airportID", airportID);
	}
	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);

		if(tags.contains("airportID"))
			airportID = tags.getUUID("airportID");
	}



	@Nonnull
	public UUID registerAirportID()
	{
		if(level != null && !level.isClientSide)
		{
			IAirTrafficServer atcServer = ATC.getServer(level);
			if(atcServer != null)
			{
				return atcServer.generateAirportID(new AirmapPos(getBlockPos()));
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
				airportData = atcServer.loadAirportData(airportID);
				return airportData != null;
			}
		}
		return false;
	}
	public boolean isAirportInitialized()
	{
		return !airportID.equals(IAirport.INVALID_AIRPORT_ID);
	}
	public boolean isAirportDataKnown()
	{
		return airportData != null;
	}

	public void setEnabled()
	{

	}
	public void setAirportName(@Nonnull String newName)
	{
		if(level != null)
		{
			if (level.isClientSide)
			{
				AirTrafficControlPacketHandler.sendToServer(ConfigureAirportMessage.rename(getBlockPos(), newName));
			}
			else
			{
				IAirTrafficServer atcServer = ATC.getServer(level);
				if(atcServer != null)
				{
					atcServer.editAirport(airportID, (editableData) -> editableData.setName(newName));
				}
			}
		}
	}
}
