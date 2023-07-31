package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GunContextItem extends GunContext
{
	private final ItemStack Stack;
	public GunContextItem(ItemStack stack)
	{
		super();
		Stack = stack;
	}

	@Override
	@Nonnull
	public ItemStack GetItemStack() { return Stack; }
	@Override
	public void SetItemStack(ItemStack stack) { } // Can't set an itemstack if we don't know where it is
	@Override
	public DamageSource CreateDamageSource() { return DamageSource.GENERIC; }
	@Override
	public ShooterContext GetShooter() { return ShooterContext.INVALID; }
	@Override
	public Container GetAttachedInventory() { return null; }
	@Override
	public boolean CanPerformActions() { return false; }
	@Override
	@Nonnull
	public ActionStack GetActionStack() { return ActionStack.Invalid; }
	@Override
	public boolean CanPerformTwoHandedAction() { return false; }

	@Override
	public int hashCode()
	{
		return Objects.hash(
			Stack.getCount(),
			Item.getId(Stack.getItem()),
			Stack.getDamageValue(),
			Stack.getTag());
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextItem otherContext)
		{
			return ItemStack.isSame(Stack, otherContext.Stack);
		}
		return false;
	}
}
