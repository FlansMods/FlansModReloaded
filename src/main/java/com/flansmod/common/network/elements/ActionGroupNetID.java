package com.flansmod.common.network.elements;

import com.flansmod.common.actions.EActionInput;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ActionGroupNetID
{
	public UUID EntityUUID;
	public int GroupPathHash;
	public int InventorySlotIndex;
	public int ContextHash;

	public ActionGroupNetID()
	{

	}

	public ActionGroupNetID(UUID entityUUID, int groupPathHash, int inventorySlotIndex, int contextHash)
	{
		EntityUUID = entityUUID;
		GroupPathHash = groupPathHash;
		InventorySlotIndex = inventorySlotIndex;
		ContextHash = contextHash;
	}

	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeShort(InventorySlotIndex);
		buf.writeInt(GroupPathHash);
		buf.writeUUID(EntityUUID);
		buf.writeInt(ContextHash);
	}

	public void Decode(FriendlyByteBuf buf)
	{
		InventorySlotIndex = buf.readShort();
		GroupPathHash = buf.readInt();
		EntityUUID = buf.readUUID();
		ContextHash = buf.readInt();
	}
}
