package com.flansmod.common.network.toclient;

import com.flansmod.common.network.FlansModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class ShotVerifyMessage extends FlansModMessage
{
	public boolean verificationSuccess = true;


	@Override
	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(verificationSuccess);
	}

	@Override
	public void Decode(FriendlyByteBuf buf)
	{
		buf.readBoolean();
	}
}
