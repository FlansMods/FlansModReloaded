package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BulletItem extends FlanItem
{
	private ResourceLocation definitionLocation;
	public BulletDefinition Def() { return FlansMod.BULLETS.get(definitionLocation); }

	public BulletItem(ResourceLocation defLoc, Properties properties)
	{
		super(properties);

		definitionLocation = defLoc;
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }
}
