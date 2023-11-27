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
	public boolean UpdateFromItemStack()
	{
		ItemStack currentStack = Player.getInventory().getItem(InventorySlot);
		boolean invalidate = StackUpdateWouldInvalidate(currentStack);
		if(GetShooter().IsLocalPlayerOwner())
		{
			// If we accept server updates while reloading,
			// we are going to skip all our animations because the gun is "already reloaded"
			return false;
		}
		else
		{
			if (!invalidate)
				Stack = currentStack.copy();
			return invalidate;
		}
	}
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
		return HashGunOrigins(Stack) ^ (Hand.ordinal() << 7) ^ (InventorySlot << 13) ^ ShooterContext.hashCode();
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextLiving otherContext)
		{
			return HashGunOrigins(Stack) == HashGunOrigins(otherContext.Stack)
				&& Hand == otherContext.Hand
				&& InventorySlot == otherContext.GetInventorySlotIndex()
				&& ShooterContext.equals(otherContext.ShooterContext);
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "GunContextPlayer:" + GetItemStack().toString() + " held by " + Player + " in slot " + InventorySlot;
	}
}
