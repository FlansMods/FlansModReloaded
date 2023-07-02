package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GunContextLiving extends GunContext
{
	protected final ShooterContextLiving ShooterContext;
	protected final InteractionHand Hand;

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand)
	{
		super();
		ShooterContext = shooterContext;
		Hand = hand;
	}

	@Override
	@Nonnull
	public ItemStack GetItemStack() { return ShooterContext.Shooter.getItemInHand(Hand); }
	@Override
	public void SetItemStack(ItemStack stack) { ShooterContext.Shooter.setItemInHand(Hand, stack); }
	@Override
	public Inventory GetAttachedInventory() { return null; }
	@Override
	public DamageSource CreateDamageSource() { return new IndirectEntityDamageSource("gun", ShooterContext.Shooter, ShooterContext.Shooter); }
	@Override
	public ShooterContext GetShooter() { return ShooterContext; }
	@Override
	public boolean CanPerformActions() { return true; }
	@Override
	public ActionStack GetActionStack() { return ActionManager.SafeGetActionStack(this); }
	@Override
	public boolean CanPerformTwoHandedAction() { return ShooterContext.CanPerformTwoHandedAction(); }
	@Override
	public InteractionHand GetHand() { return Hand; }
	@Override
	public int hashCode()
	{
		return Objects.hash(
			ShooterContext,
			Hand);
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextLiving otherContext)
		{
			return otherContext.Hand == Hand &&
				otherContext.ShooterContext.equals(ShooterContext);
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString() + " held by " + ShooterContext.Shooter.toString();
	}
}
