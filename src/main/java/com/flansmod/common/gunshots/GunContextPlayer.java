package com.flansmod.common.gunshots;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class GunContextPlayer extends GunContextLiving
{
	private final Player Player;
	private final int InventorySlot;

	public GunContextPlayer(ShooterContextLiving shooter, InteractionHand hand)
	{
		super(shooter, hand);

		Player = (Player)shooter.Shooter;
		InventorySlot = hand == InteractionHand.MAIN_HAND ? Player.getInventory().selected :Inventory.SLOT_OFFHAND;
	}

	@Override
	public Inventory GetAttachedInventory()
	{
		return Player.getInventory();
	}

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
