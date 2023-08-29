package com.flansmod.common.network.elements;

import com.flansmod.common.actions.EActionInput;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ActionGroupNetID
{
	public UUID EntityUUID;
	public EActionInput InputType;
	public int InventorySlotIndex;

	public ActionGroupNetID()
	{

	}
	public ActionGroupNetID(UUID entityUUID, EActionInput inputType, int inventorySlotIndex)
	{
		EntityUUID = entityUUID;
		InputType = inputType;
		InventorySlotIndex = inventorySlotIndex;
	}

	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeShort(InventorySlotIndex);
		buf.writeShort(InputType.ordinal());
		buf.writeUUID(EntityUUID);
	}

	public void Decode(FriendlyByteBuf buf)
	{
		InventorySlotIndex = buf.readShort();
		InputType = EActionInput.values()[buf.readShort()];
		EntityUUID = buf.readUUID();
	}
}
