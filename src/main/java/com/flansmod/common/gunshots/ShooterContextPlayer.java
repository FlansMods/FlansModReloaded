package com.flansmod.common.gunshots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ShooterContextPlayer extends ShooterContextLiving
{
	private final Player Player;

	public ShooterContextPlayer(@NotNull Player player)
	{
		super(player);
		Player = player;
	}

	@Override
	public Container GetAttachedInventory() { return Player.getInventory(); }
	@Override
	public boolean IsCreative()
	{
		return Player.isCreative();
	}
}
