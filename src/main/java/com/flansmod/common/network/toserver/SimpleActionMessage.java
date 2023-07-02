package com.flansmod.common.network.toserver;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.network.FlansModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class SimpleActionMessage extends FlansModMessage
{
	public InteractionHand hand;
	public EActionInput inputType;

	public SimpleActionMessage()
	{
		this.hand = InteractionHand.MAIN_HAND;
		this.inputType = EActionInput.PRIMARY;
	}

	public SimpleActionMessage(InteractionHand hand, EActionInput inputType)
	{
		this.hand = hand;
		this.inputType = inputType;
	}

	@Override
	public void Encode(FriendlyByteBuf buf)
	{
		int flags = 0;
		flags |= (hand == InteractionHand.MAIN_HAND ? 1 : 0);
		flags |= (inputType.ordinal() << 1);
		buf.writeInt(flags);
	}

	@Override
	public void Decode(FriendlyByteBuf buf)
	{
		int flags = buf.readInt();
		hand = (flags & 0x1) != 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		inputType = EActionInput.values()[(flags & 0x6) >> 1];
	}
}
