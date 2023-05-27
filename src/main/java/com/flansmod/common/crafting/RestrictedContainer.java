package com.flansmod.common.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Function;

public class RestrictedContainer implements Container, ContainerData
{
	private final BlockEntity OwnedBy;
	private final int SlotCount;
	private final double InteractRange;
	private final int MaxStackSize;
	private final Function<ItemStack, Boolean> AllowFunction;
	private final ItemStack[] Slots;

	public RestrictedContainer(BlockEntity parent, double interactRange, int slots, int maxStack, Function<ItemStack, Boolean> allowFunc)
	{
		OwnedBy = parent;
		InteractRange = interactRange;
		AllowFunction = allowFunc;
		SlotCount = slots;
		MaxStackSize = maxStack;
		Slots = new ItemStack[slots];
		for(int i = 0; i < SlotCount; i++)
			Slots[i] = ItemStack.EMPTY;
	}

	// ContainerData
	public int get(int index) { return index >= 0 && index < SlotCount ? Slots[index].getCount() : 0; }
	public void set(int index, int value) { } // no-op
	public int getCount() { return SlotCount; }

	// Container
	@Override
	public int getContainerSize() { return SlotCount; }
	@Override
	public int getMaxStackSize() {
		return MaxStackSize;
	}
	@Override
	public boolean isEmpty()
	{
		for(int i = 0; i < SlotCount; i++)
			if(!Slots[i].isEmpty())
				return false;
		return true;
	}
	@Override
	public boolean canPlaceItem(int index, ItemStack stack) { return stack.isEmpty() || AllowFunction.apply(stack); }
	@Override
	public ItemStack getItem(int index) { return index >= 0 && index < SlotCount ? Slots[index] : ItemStack.EMPTY; }
	@Override
	public ItemStack removeItem(int index, int count)
	{
		if(index >= 0 && index < SlotCount)
			return Slots[index].split(count);
		return ItemStack.EMPTY;
	}
	@Override
	public ItemStack removeItemNoUpdate(int index)
	{
		if(index >= 0 && index < SlotCount)
		{
			ItemStack ret = Slots[index];
			Slots[index] = ItemStack.EMPTY;
			return ret;
		}
		return ItemStack.EMPTY;
	}
	@Override
	public void setItem(int index, ItemStack stack)
	{
		Slots[index] = stack;
	}
	@Override
	public void setChanged() {}
	@Override
	public boolean stillValid(Player player)
	{
		if(OwnedBy == null || OwnedBy.isRemoved() || OwnedBy.getLevel().getBlockEntity(OwnedBy.getBlockPos()) != OwnedBy)
			return false;
		if(OwnedBy.getBlockPos().distToCenterSqr(player.getPosition(0f)) > InteractRange * InteractRange)
			return false;
		return true;
	}
	@Override
	public void clearContent()
	{
		for(int i = 0; i < SlotCount; i++)
			Slots[i] = ItemStack.EMPTY;
	}

	public CompoundTag save(CompoundTag tags)
	{
		for(int i = 0; i < SlotCount; i++)
		{
			CompoundTag itemTag = new CompoundTag();
			Slots[i].save(itemTag);
			tags.put("slot" + i, itemTag);
		}
		return tags;
	}

	public void load(CompoundTag tags)
	{
		for(int i = 0; i < SlotCount; i++)
		{
			CompoundTag itemTag = tags.getCompound("slot" + i);
			Slots[i] = itemTag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(itemTag);
		}
	}
}
