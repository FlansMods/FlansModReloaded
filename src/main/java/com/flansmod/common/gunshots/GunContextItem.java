package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.GunContext;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GunContextItem extends GunContext
{
	public GunContextItem(ItemStack stack)
	{
		super(stack, null);
	}
	// We don't even know where this ItemStack lives, no-op
	@Override
	public void OnItemStackChanged(ItemStack stack) { }
	@Override
	protected boolean CanInvalidateItemStack() { return false; }
	@Override
	public boolean UpdateFromItemStack() { return false; }
	@Override
	public DamageSource CreateDamageSource() { return DamageSource.GENERIC; }
	@Override
	@Nonnull
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
	// There are no modifiers to apply right now
	@Override
	public int HashModifierSources() { return 0x404; }
	@Override
	public void RecalculateModifierCache() {}
}
