package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GunContextInventoryItem extends GunContext
{
	private final Container Inv;
	private final int Slot;
	public GunContextInventoryItem(Container inv, int slot)
	{
		super();
		Inv = inv;
		Slot = slot;
	}

	@Override
	@Nonnull
	public ItemStack GetItemStack() { return Inv.getItem(Slot); }
	@Override
	public void SetItemStack(ItemStack stack) { Inv.setItem(Slot, stack); }
	@Override
	public DamageSource CreateDamageSource() { return DamageSource.GENERIC; }
	@Override
	public ShooterContext GetShooter() { return ShooterContext.INVALID; }
	@Override
	public Container GetAttachedInventory() { return Inv; }
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
		return Objects.hash(Inv, Slot);
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextInventoryItem otherContext)
		{
			return Slot == otherContext.Slot && Inv == otherContext.Inv;
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString() + " in slot " + Slot + " in inventory " + Inv.toString();
	}
}
