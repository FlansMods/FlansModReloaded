package com.flansmod.common.network.toserver;

import com.flansmod.common.gunshots.GunshotCollection;
import com.flansmod.common.network.FlansModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class ReloadRequestMessage extends FlansModMessage
{
	public InteractionHand hand;

	public ReloadRequestMessage()
	{
		this.hand = InteractionHand.MAIN_HAND;
	}

	public ReloadRequestMessage(InteractionHand hand)
	{
		this.hand = hand;
	}

	@Override
	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
	}

	@Override
	public void Decode(FriendlyByteBuf buf)
	{
		hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}
}
