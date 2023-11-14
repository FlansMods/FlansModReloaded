package com.flansmod.common.gunshots;

import com.flansmod.common.actions.GunContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ShooterContextPlayer extends ShooterContextLiving
{
	public final Player Player;

	public ShooterContextPlayer(@Nonnull Player player)
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
	@Override
	public GunContext CreateForGunIndex(int gunSlotIndex) { return new GunContextPlayer(this, gunSlotIndex); }
	@Override
	public GunContext CreateForSpecificStack(int gunSlotIndex, ItemStack stack) { return new GunContextPlayer(this, gunSlotIndex, stack); }

}
