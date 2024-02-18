package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

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
	@Nonnull
	public EItemStackLinkage CheckItemStackLink()
	{
		return EItemStackLinkage.Connected;
	}
	@Override
	@Nonnull
	public ItemStack GetLinkedItemStack()
	{
		return Inv.getItem(Slot);
	}
	@Override
	public Container GetAttachedInventory() { return Inv; }
	@Override
	public int GetInventorySlotIndex() { return Slot; }
	// There are no modifiers to apply right now
	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker) {}
	@Override
	public String toString()
	{
		return "Container: '"+Inv+"'/"+Slot+" -> Item ["+Stack+"]";
	}
}
