package com.flansmod.common.network.elements;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ActionGroupNetID
{
	public UUID ShooterID;
	public int GroupPathHash;
	public int InventorySlotIndex;
	public UUID GunID;

	public ActionGroupNetID()
	{

	}

	public ActionGroupNetID(UUID entityUUID, int groupPathHash, int inventorySlotIndex, UUID gunID)
	{
		ShooterID = entityUUID;
		GroupPathHash = groupPathHash;
		InventorySlotIndex = inventorySlotIndex;
		GunID = gunID;
	}

	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeShort(InventorySlotIndex);
		buf.writeInt(GroupPathHash);
		buf.writeUUID(ShooterID);
		buf.writeUUID(GunID);
	}

	public void Decode(FriendlyByteBuf buf)
	{
		InventorySlotIndex = buf.readShort();
		GroupPathHash = buf.readInt();
		ShooterID = buf.readUUID();
		GunID = buf.readUUID();
	}
}
