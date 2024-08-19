package com.flansmod.common.entity.vehicle;

import com.flansmod.util.Maths;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class VehicleSubContainer implements Container
{
	public enum EType
	{
		Gun,
		Ammo,
		Fuel,
	}

	public final EType Type;
	public final ItemStack[] Contents;

	public VehicleSubContainer(@Nonnull EType type, int size)
	{
		Type = type;
		Contents = new ItemStack[size];
	}

	@Override
	public int getContainerSize() { return Contents.length; }
	@Override
	public boolean isEmpty()
	{
		for(ItemStack stack : Contents)
			if(!stack.isEmpty())
				return false;
		return true;
	}
	@Override
	@Nonnull
	public ItemStack getItem(int index) { return Contents[index]; }
	@Override
	@Nonnull
	public ItemStack removeItem(int index, int count)
	{
		ItemStack stackInSlot = Contents[index];
		int countToRemove = Maths.Min(count, stackInSlot.getCount());
		ItemStack returnStack = Contents[index].copyWithCount(countToRemove);
		Contents[index].setCount(Contents[index].getCount() - countToRemove);
		return returnStack;
	}
	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int index)
	{
		ItemStack toReturn = Contents[index];
		Contents[index] = ItemStack.EMPTY;
		return toReturn;
	}
	@Override
	public void setItem(int index, @Nonnull ItemStack stack)
	{
		Contents[index] = stack;
	}
	@Override
	public void setChanged() {}
	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return true;
	}
	@Override
	public void clearContent()
	{
		Arrays.fill(Contents, ItemStack.EMPTY);
	}
}
