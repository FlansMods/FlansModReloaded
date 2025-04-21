package com.flansmod.airtraffic.network.toclient;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListConfigOptionsMessage extends UpdateRunwayMessage
{
	@Nonnull
	public static ListNearestAirportNamesMessage listNearestAirports(@Nonnull BlockPos pos)
	{
		var msg = new ListNearestAirportNamesMessage();
		msg.location = pos;
		return msg;
	}

	public static class ListNearestAirportNamesMessage extends ListConfigOptionsMessage
	{
		public record AirportOption(@Nonnull UUID id, @Nonnull String name) {}
		public List<AirportOption> options = new ArrayList<>();

		@Nonnull
		public ListNearestAirportNamesMessage add(@Nonnull UUID id, @Nonnull String name)
		{
			options.add(new AirportOption(id, name));
			return this;
		}

		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeInt(options.size());
			for(int i = 0; i < options.size(); i++)
			{
				buf.writeUUID(options.get(i).id);
				buf.writeUtf(options.get(i).name);
			}
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			int count = buf.readInt();
			for(int i = 0; i < count; i++)
			{
				UUID id = buf.readUUID();
				String name = buf.readUtf();
				options.add(new AirportOption(id, name));
			}
		}
	}


}
