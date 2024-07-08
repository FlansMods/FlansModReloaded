package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionManager;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class GunContextLiving extends GunContextInventoryItem
{
	protected final ShooterContextLiving ShooterContext;
	protected final InteractionHand Hand;

	public GunContextLiving(@Nonnull ShooterContextLiving shooterContext, @Nonnull InteractionHand hand)
	{
		super(shooterContext.GetAttachedInventory(), hand == InteractionHand.MAIN_HAND ? 0 : 1);
		ShooterContext = shooterContext;
		Hand = hand;
	}

	public GunContextLiving(@Nonnull ShooterContextLiving shooterContext, @Nonnull InteractionHand hand, int slotIndex)
	{
		super(shooterContext.GetAttachedInventory(), slotIndex);
		ShooterContext = shooterContext;
		Hand = hand;
	}

	@Override
	public void OnItemStackChanged(@Nonnull ItemStack stack)
	{
		ShooterContext.Shooter.setItemInHand(Hand, stack);
	}
	@Override
	@Nonnull
	public EItemStackLinkage CheckItemStackLink() { return ShooterContext.IsValid() ? EItemStackLinkage.Connected : EItemStackLinkage.LostConnection; }
	@Override
	@Nonnull
	public ItemStack GetLinkedItemStack() { return Hand == InteractionHand.MAIN_HAND ? ShooterContext.Shooter.getMainHandItem() : ShooterContext.Shooter.getOffhandItem(); }
	@Override
	@Nullable
	public Container GetAttachedInventory() { return ShooterContext.GetAttachedInventory(); }
	@Override
	@Nullable
	public DamageSource CreateDamageSource()
	{
		return ShooterContext.Level().damageSources().mobProjectile(ShooterContext.Shooter, (ShooterContext.Owner() instanceof LivingEntity lv) ? lv : null);
		//return DamageSources.source(FlansMod.DAMAGE_TYPE_GUN ,ShooterContext.Shooter, ShooterContext.Shooter);
	}
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
	public void BakeModifiers(@Nonnull IModifierBaker baker) {}
	@Override
	@Nullable
	public Level GetLevel() { return ShooterContext.Level(); }
	@Override
	@Nonnull
	public String toString()
	{
		return GetShooter() + " holding [" + GetItemStack() + "] " + (Hand == InteractionHand.MAIN_HAND ? "(Main)" : "(Off)");
	}
}
