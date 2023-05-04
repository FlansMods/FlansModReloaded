package com.flansmod.common.network.toserver;

import com.flansmod.common.gunshots.Gunshot;
import com.flansmod.common.gunshots.GunshotCollection;
import com.flansmod.common.network.FlansModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class ShotRequestMessage extends FlansModMessage
{
	public GunshotCollection Get() { return shotCollection; }
	private GunshotCollection shotCollection;
	private long timestamp;

	public ShotRequestMessage()
	{
		this.shotCollection = new GunshotCollection();
	}

	public ShotRequestMessage(GunshotCollection shots)
	{
		this.shotCollection = shots;
	}

	@Override
	public void Encode(FriendlyByteBuf buf)
	{
		GunshotCollection.Encode(shotCollection, buf);
	}

	@Override
	public void Decode(FriendlyByteBuf buf)
	{
		shotCollection = new GunshotCollection();
		GunshotCollection.Decode(shotCollection, buf);
	}
}
