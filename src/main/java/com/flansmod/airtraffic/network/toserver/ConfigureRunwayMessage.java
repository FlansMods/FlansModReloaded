package com.flansmod.airtraffic.network.toserver;

import com.flansmod.airtraffic.network.AirTrafficMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class ConfigureRunwayMessage extends AirTrafficMessage
{
	public BlockPos location;

	@Nonnull
	public static RenameRunwayMessage rename(@Nonnull BlockPos pos, @Nonnull String name)
	{
		var msg = new RenameRunwayMessage();
		msg.location = pos;
		msg.renameTo = name;
		return msg;
	}
	@Nonnull
	public static SetRunwayParametersMessage updateParameters(@Nonnull BlockPos pos, int width, int height, double incline)
	{
		var msg = new SetRunwayParametersMessage();
		msg.location = pos;
		msg.width = width;
		msg.height = height;
		return msg;
	}
	@Nonnull
	public static RequestRunwayValidationMessage requestValidate(@Nonnull BlockPos pos)
	{
		var msg = new RequestRunwayValidationMessage();
		msg.location = pos;
		return msg;
	}
	@Nonnull
	public static ConnectToAiportMessage connect(@Nonnull BlockPos pos, @Nonnull UUID airportID)
	{
		var msg = new ConnectToAiportMessage();
		msg.location = pos;
		msg.airportID = airportID;
		return msg;
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


	public static class RenameRunwayMessage extends ConfigureRunwayMessage
	{
		public String renameTo;
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
	public static class SetRunwayParametersMessage extends ConfigureRunwayMessage
	{
		public int width;
		public int height;
		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeInt(width);
			buf.writeInt(height);
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			width = buf.readInt();
			height = buf.readInt();
		}
	}
	public static class RequestRunwayValidationMessage extends ConfigureRunwayMessage
	{

	}
	public static class CancelRunwayValidationMessage extends ConfigureRunwayMessage
	{

	}
	public static class ConnectToAiportMessage extends ConfigureRunwayMessage
	{
		public UUID airportID;

		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeUUID(airportID);
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			airportID = buf.readUUID();
		}
	}
}
