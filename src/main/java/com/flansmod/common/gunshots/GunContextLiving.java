package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.Container;
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
		super(hand == InteractionHand.MAIN_HAND ? shooterContext.Shooter.getMainHandItem() : shooterContext.Shooter.getOffhandItem());
		ShooterContext = shooterContext;
		Hand = hand;
	}

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand, ItemStack withItemOverride)
	{
		super(withItemOverride);
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
	@Override
	public String toString()
	{
		return "GunContext:" + GetItemStack().toString() + " held by " + ShooterContext.Shooter.toString();
	}
	// There are no modifiers to apply right now
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}
}
