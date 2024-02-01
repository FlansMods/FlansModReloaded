package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunContextItemEntity extends GunContext
{
	@Nonnull
	public final ItemEntity Holder;

	public GunContextItemEntity(@Nonnull ItemEntity itemEntity)
	{
		super(itemEntity.getItem());
		Holder = itemEntity;
	}

	@Override
	public void OnItemStackChanged(@Nonnull ItemStack stack) {}
	@Nonnull
	@Override
	public EItemStackLinkage CheckItemStackLink() { return Holder.isAlive() ? EItemStackLinkage.Connected : EItemStackLinkage.LostConnection; }
	@Nonnull
	@Override
	public ItemStack GetLinkedItemStack() { return Holder.getItem(); }
	@Nullable
	@Override
	public DamageSource CreateDamageSource() { return null; }
	@Nonnull
	@Override
	public ShooterContext GetShooter() { return ShooterContext.INVALID; }
	@Nullable
	@Override
	public Container GetAttachedInventory() { return null; }
	@Override
	public boolean CanPerformTwoHandedAction() { return false; }
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}
	@Nonnull
	@Override
	public ActionStack GetActionStack() { return ActionStack.Invalid; }
	@Override
	public boolean CanPerformActions() { return false; }
}
