package com.flansmod.common.types.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ItemStackDefinition
{
	@JsonField
	public String item = "minecraft:air";
	@JsonField
	public int count = 1;
	@JsonField
	public int damage = 0;
	@JsonField
	public String tags = "{}";

	@Nonnull
	public ItemStack CreateStack()
	{
		try
		{
			ResourceLocation itemLoc = new ResourceLocation(item);
			Item resolvedItem = ForgeRegistries.ITEMS.getValue(itemLoc);
			if (resolvedItem == null)
			{
				FlansMod.LOGGER.warn("Could not find item " + item + " in ItemStackDefinition");
				return ItemStack.EMPTY;
			}
			ItemStack stack = new ItemStack(resolvedItem, count);
			if (stack.isDamageableItem())
				stack.setDamageValue(damage);
			//stack.setTag()
			// TODO: Tag
			return stack;
		}
		catch(Exception e)
		{
			FlansMod.LOGGER.error("Failed to resolve item stack " + e.getMessage());
			return ItemStack.EMPTY;
		}
	}
}
