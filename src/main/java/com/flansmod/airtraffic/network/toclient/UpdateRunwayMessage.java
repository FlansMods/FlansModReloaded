package com.flansmod.airtraffic.network.toclient;

import com.flansmod.airtraffic.network.AirTrafficMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class UpdateRunwayMessage extends AirTrafficMessage
{
	public BlockPos location;

	@Nonnull
	public static UpdateRunwayNameAndID updateNameAndID(@Nonnull BlockPos pos, @Nonnull String name, @Nonnull UUID id)
	{
		var msg = new UpdateRunwayNameAndID();
		msg.location = pos;
		msg.runwayName = name;
		msg.runwayID = id;
		return msg;
	}
	@Nonnull
	public static UpdateRunwayValidationProgress updateRunwayValidationProgress(@Nonnull BlockPos pos, double progress, int numIssues)
	{
		var msg = new UpdateRunwayValidationProgress();
		msg.location = pos;
		msg.progress = progress;
		msg.numIssues = numIssues;
		return msg;
	}
	@Nonnull
	public static CompletedRunwayValidation completedRunwayValidation(@Nonnull BlockPos pos, int numIssues)
	{
		var msg = new CompletedRunwayValidation();
		msg.location = pos;
		msg.numIssues = numIssues;
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


	public static class UpdateRunwayNameAndID extends UpdateRunwayMessage
	{
		public String runwayName;
		public UUID runwayID;
		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeUtf(runwayName);
			buf.writeUUID(runwayID);
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			runwayName = buf.readUtf();
			runwayID = buf.readUUID();
		}
	}
	public static class UpdateRunwayValidationProgress extends UpdateRunwayMessage
	{
		public double progress;
		public int numIssues;

		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeDouble(progress);
			buf.writeInt(numIssues);
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			progress = buf.readDouble();
			numIssues = buf.readInt();
		}
	}
	public static class CompletedRunwayValidation extends UpdateRunwayMessage
	{
		public int numIssues;

		@Override
		public void encode(@Nonnull FriendlyByteBuf buf)
		{
			super.encode(buf);
			buf.writeInt(numIssues);
		}
		@Override
		public void decode(@Nonnull FriendlyByteBuf buf)
		{
			super.decode(buf);
			numIssues = buf.readInt();
		}
	}
}
