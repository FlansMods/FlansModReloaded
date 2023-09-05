package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GunContextPlayer extends GunContextLiving
{
	private final Player Player;
	private final int InventorySlot;


	public GunContextPlayer(ShooterContextPlayer shooter, int inventorySlot)
	{
		super(shooter, inventorySlot == ((Player)shooter.Shooter).getInventory().selected ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);

		Player = (Player)shooter.Shooter;
		InventorySlot = inventorySlot;
	}

	public GunContextPlayer(ShooterContextPlayer shooter, int inventorySlot, ItemStack withItemOverride)
	{
		super(shooter,
			inventorySlot == ((Player)shooter.Shooter).getInventory().selected ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
			withItemOverride);

		Player = (Player)shooter.Shooter;
		InventorySlot = inventorySlot;
	}

	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		ItemStack stackInSlot = Player.getInventory().getItem(InventorySlot);
		if(!StackUpdateWouldInvalidate(stackInSlot))
		{
			Player.getInventory().setItem(InventorySlot, stack);
		}
	}
	@Override
	public boolean IsItemStackStillInPlace() { return !StackUpdateWouldInvalidate(Player.getInventory().getItem(InventorySlot)); }
	@Override
	public Inventory GetAttachedInventory()
	{
		return Player.getInventory();
	}
	@Override
	public int GetInventorySlotIndex() { return InventorySlot; }

	@Override
	public int hashCode()
	{
		return Objects.hash(
			ShooterContext,
			Hand,
			InventorySlot);
	}

	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextPlayer otherContext)
		{
			return otherContext.Hand == Hand &&
				otherContext.InventorySlot == InventorySlot &&
				otherContext.ShooterContext.equals(ShooterContext);
		}
		return false;
	}
}
