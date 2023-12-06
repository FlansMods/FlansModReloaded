package com.flansmod.common.actions.contexts;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunContextInventoryItem extends GunContextItem
{
	@Nonnull
	private final Container Inv;
	private final int Slot;
	public GunContextInventoryItem(@Nonnull Container inv, int slot)
	{
		super(inv.getItem(slot));
		Inv = inv;
		Slot = slot;
	}
	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		Inv.setItem(Slot, stack);
	}
	@Override
	public boolean UpdateFromItemStack()
	{
		Stack = Inv.getItem(Slot).copy();
		return false;
	}
	@Override
	public Container GetAttachedInventory() { return Inv; }
	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString() + " in slot " + Slot + " in inventory " + Inv;
	}
	// There are no modifiers to apply right now
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}
}
