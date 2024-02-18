package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

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
	public void BakeModifiers(@Nonnull IModifierBaker baker) {}
	@Nonnull
	@Override
	public ActionStack GetActionStack() { return ActionStack.Invalid; }
	@Override
	public boolean CanPerformActions() { return false; }
}
