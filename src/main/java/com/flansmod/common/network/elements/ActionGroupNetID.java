package com.flansmod.common.network.elements;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ActionGroupNetID
{
	public UUID EntityUUID;
	public int GroupPathHash;
	public int InventorySlotIndex;
	public UUID GunID;

	public ActionGroupNetID()
	{

	}

	public ActionGroupNetID(UUID entityUUID, int groupPathHash, int inventorySlotIndex, UUID gunID)
	{
		EntityUUID = entityUUID;
		GroupPathHash = groupPathHash;
		InventorySlotIndex = inventorySlotIndex;
		GunID = gunID;
	}

	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeShort(InventorySlotIndex);
		buf.writeInt(GroupPathHash);
		buf.writeUUID(EntityUUID);
		buf.writeUUID(GunID);
	}

	public void Decode(FriendlyByteBuf buf)
	{
		InventorySlotIndex = buf.readShort();
		GroupPathHash = buf.readInt();
		EntityUUID = buf.readUUID();
		GunID = buf.readUUID();
	}
}
