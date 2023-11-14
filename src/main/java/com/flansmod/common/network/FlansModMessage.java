package com.flansmod.common.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract class FlansModMessage
{
	public abstract void Encode(FriendlyByteBuf buf);
	public abstract void Decode(FriendlyByteBuf buf);
}
