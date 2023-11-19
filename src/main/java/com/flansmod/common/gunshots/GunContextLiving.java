package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionManager;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.GunContext;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunContextLiving extends GunContext
{
	protected final ShooterContextLiving ShooterContext;
	protected final InteractionHand Hand;

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand)
	{
		super(hand == InteractionHand.MAIN_HAND ? shooterContext.Shooter.getMainHandItem() : shooterContext.Shooter.getOffhandItem(), shooterContext.Level());
		ShooterContext = shooterContext;
		Hand = hand;
	}

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand, ItemStack withItemOverride)
	{
		super(withItemOverride, shooterContext.Level());
		ShooterContext = shooterContext;
		Hand = hand;
	}

	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		ItemStack stackInHand = Hand == InteractionHand.MAIN_HAND ? ShooterContext.Shooter.getMainHandItem() : ShooterContext.Shooter.getOffhandItem();
		if(!StackUpdateWouldInvalidate(stackInHand))
		{
			ShooterContext.Shooter.setItemInHand(Hand, stack);
		}
	}
	@Override
	public boolean IsItemStackStillInPlace() { return !StackUpdateWouldInvalidate(Hand == InteractionHand.MAIN_HAND ? ShooterContext.Shooter.getMainHandItem() : ShooterContext.Shooter.getOffhandItem()); }
	@Override
	public Container GetAttachedInventory() { return ShooterContext.GetAttachedInventory(); }
	@Override
	public DamageSource CreateDamageSource() { return new IndirectEntityDamageSource("gun", ShooterContext.Shooter, ShooterContext.Shooter); }
	@Override
	@Nonnull
	public ShooterContext GetShooter() { return ShooterContext; }
	@Override
	public boolean CanPerformActions() { return true; }
	@Override
	@Nonnull
	public ActionStack GetActionStack() { return ActionManager.SafeGetActionStack(this); }
	@Override
	public boolean CanPerformTwoHandedAction() { return ShooterContext.CanPerformTwoHandedAction(); }
	@Override
	public int GetInventorySlotIndex() { return Hand == InteractionHand.MAIN_HAND ? 0 : 1; }
	public InteractionHand GetHand() { return Hand; }

	// There are no modifiers to apply right now
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}

	@Override
	public int hashCode()
	{
		return HashGunOrigins(Stack) ^ Hand.hashCode() ^ ShooterContext.hashCode();
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextLiving otherContext)
		{
			return HashGunOrigins(Stack) == HashGunOrigins(otherContext.Stack)
				&& Hand == otherContext.Hand
				&& ShooterContext.equals(otherContext.ShooterContext);
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "GunContextLiving:" + GetItemStack().toString() + " held by " + ShooterContext.Shooter.toString();
	}
}
