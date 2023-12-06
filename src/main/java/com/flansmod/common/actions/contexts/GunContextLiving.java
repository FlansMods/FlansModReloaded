package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionManager;
import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunContextLiving extends GunContextInventoryItem
{
	protected final ShooterContextLiving ShooterContext;
	protected final InteractionHand Hand;

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand)
	{
		super(shooterContext.GetAttachedInventory(), hand == InteractionHand.MAIN_HAND ? 0 : 1);
		ShooterContext = shooterContext;
		Hand = hand;
	}

	public GunContextLiving(ShooterContextLiving shooterContext, InteractionHand hand, int slotIndex)
	{
		super(shooterContext.GetAttachedInventory(), slotIndex);
		ShooterContext = shooterContext;
		Hand = hand;
	}

	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		ShooterContext.Shooter.setItemInHand(Hand, stack);
	}
	@Override
	public boolean UpdateFromItemStack()
	{
		ItemStack currentStack = Hand == InteractionHand.MAIN_HAND ? ShooterContext.Shooter.getMainHandItem() : ShooterContext.Shooter.getOffhandItem();
		Stack = currentStack.copy();
		return false;
	}
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
	@Nullable
	public Level GetLevel() { return ShooterContext.Level(); }
	@Override
	public String toString()
	{
		return "GunContextLiving:" + GetItemStack().toString() + " held by " + ShooterContext.Shooter;
	}
}
