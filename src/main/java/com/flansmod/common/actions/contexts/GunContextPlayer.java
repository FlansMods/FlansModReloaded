package com.flansmod.common.actions.contexts;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GunContextPlayer extends GunContextLiving
{
	private final Player Player;
	private final int InventorySlot;

	public GunContextPlayer(ShooterContextPlayer shooter, int inventorySlot)
	{
		super(shooter, inventorySlot == Inventory.SLOT_OFFHAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, inventorySlot);

		Player = (Player)shooter.Shooter;
		InventorySlot = inventorySlot;
	}

	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		ItemStack stackInSlot = Player.getInventory().getItem(InventorySlot);
		Player.getInventory().setItem(InventorySlot, stack);
		Player.getInventory().setChanged();
	}
	@Override
	public boolean UpdateStackFromInventory()
	{
		ItemStack currentStack = Player.getInventory().getItem(InventorySlot);
		if(!ItemStack.isSameItemSameTags(currentStack, Stack))
		{
			Stack = currentStack.copy();
			return true;
		}

		return false;
	}
	@Override
	public Inventory GetAttachedInventory()
	{
		return Player.getInventory();
	}
	@Override
	public int GetInventorySlotIndex() { return InventorySlot; }
	@Override
	public String toString()
	{
		return "GunContextPlayer:" + GetItemStack().toString() + " held by " + Player + " in slot " + InventorySlot;
	}
}
