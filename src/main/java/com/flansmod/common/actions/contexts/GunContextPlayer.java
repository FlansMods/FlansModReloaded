package com.flansmod.common.actions.contexts;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

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
	@Nonnull
	public EItemStackLinkage CheckItemStackLink() { return Player.isAlive() ? EItemStackLinkage.Connected : EItemStackLinkage.LostConnection; }
	@Override
	@Nonnull
	public ItemStack GetLinkedItemStack() { return Player.getInventory().getItem(InventorySlot); }
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
		return GetShooter() +" holding [" + GetItemStack().toString() + "] (Slot " + InventorySlot + ")";
	}
}
