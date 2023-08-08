package com.flansmod.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BulletItemMultiStack
{
	public List<Item> BulletItems;
	public String Ordering;

	public BulletItemMultiStack(int maxSize)
	{
		BulletItems = new ArrayList<>();
		Ordering = "";
	}

	public void AddBullet(ItemStack bulletStack)
	{
		if(!BulletItems.contains(bulletStack.getItem()))
			BulletItems.add(bulletStack.getItem());

		int index = BulletItems.indexOf(bulletStack.getItem());
		Ordering += (char)('0' + index);
	}

	public ItemStack GetBulletAtIndex(int index)
	{
		if(0 <= index && index < Ordering.length())
		{
			int bulletType = Ordering.charAt(index) - '0';
			return new ItemStack(BulletItems.get(bulletType), 1);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder(Ordering.length() + "x {");
		for(int i = 0; i < BulletItems.size(); i++)
		{
			output.append(BulletItems.get(i).toString());
			if(i < BulletItems.size() - 1)
				output.append(", ");
		}
		output.append("}, [");
		output.append(Ordering);
		output.append("]");
		return output.toString();
	}
}
