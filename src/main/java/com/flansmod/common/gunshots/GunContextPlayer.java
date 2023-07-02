package com.flansmod.common.gunshots;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class GunContextPlayer extends GunContextLiving
{
	private Player Player;

	public GunContextPlayer(ShooterContextLiving shooter, InteractionHand hand)
	{
		super(shooter, hand);
		Player = (Player)shooter.Shooter;
	}

	@Override
	public Inventory GetAttachedInventory()
	{
		return Player.getInventory();
	}
}
