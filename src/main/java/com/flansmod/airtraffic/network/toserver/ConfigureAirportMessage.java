package com.flansmod.airtraffic.network.toserver;

import com.flansmod.airtraffic.network.AirTrafficMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public abstract class ConfigureAirportMessage extends AirTrafficMessage
{
	public BlockPos location;

	@Nonnull
	public static RenameAirportMessage rename(@Nonnull BlockPos pos, @Nonnull String name)
	{
		var msg = new RenameAirportMessage();
		msg.location = pos;
		msg.renameTo = name;
		return msg;
	}

	public ConfigureAirportMessage()
	{
		location = BlockPos.ZERO;
	}
	public ConfigureAirportMessage(@Nonnull BlockPos pos)
	{
		location = pos;
	}
	@Override
	public void encode(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeBlockPos(location);
	}
	@Override
	public void decode(@Nonnull FriendlyByteBuf buf)
	{
		location = buf.readBlockPos();
	}



	public static class RenameAirportMessage extends ConfigureAirportMessage
	{
		public String renameTo;
		public RenameAirportMessage()
		{
			super();
			renameTo = "";
		}
		public RenameAirportMessage(@Nonnull BlockPos pos, @Nonnull String newName)
		{
			super(pos);
			renameTo = newName;
		}

		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeUtf(renameTo);
		}

		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			renameTo = buf.readUtf();
		}
	}

}
