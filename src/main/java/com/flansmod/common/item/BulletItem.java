package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.bullets.BulletDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;

public class BulletItem extends FlanItem implements IForgeItem
{
	private ResourceLocation definitionLocation;
	public BulletDefinition Def() { return FlansMod.BULLETS.Get(definitionLocation); }

	public BulletItem(ResourceLocation defLoc, Properties properties)
	{
		super(properties);
		definitionLocation = defLoc;
	}

	@Override
	public boolean isDamageable(ItemStack stack)
	{
		return Def().GetItemDurability() > 0;
	}

	@Override
	public boolean canBeDepleted()
	{
		return Def().GetItemDurability() > 0;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return Def().GetItemDurability();
	}

	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		if(stack.getDamageValue() == 0)
			return Def().GetMaxStackSize();
		else
			return 1;
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }
}
