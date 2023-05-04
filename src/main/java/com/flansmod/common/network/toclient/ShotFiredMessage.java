package com.flansmod.common.network.toclient;

import com.flansmod.common.gunshots.GunshotCollection;
import com.flansmod.common.network.FlansModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class ShotFiredMessage extends FlansModMessage
{
	public GunshotCollection Get() { return shotCollection; }
	private GunshotCollection shotCollection;
	private long timestamp;

	public ShotFiredMessage()
	{
		this.shotCollection = new GunshotCollection();
	}

	public ShotFiredMessage(GunshotCollection shots)
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