package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.bullets.BulletItemRenderer;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.bullets.BulletDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class BulletItem extends FlanItem implements IForgeItem
{
	@Override
	public BulletDefinition Def() { return FlansMod.BULLETS.Get(DefinitionLocation); }

	public BulletItem(ResourceLocation defLoc, Properties properties)
	{
		super(defLoc, properties);
	}
	@Override
	public boolean CanBeCraftedFromParts() { return false; }
	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }

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

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(FlanClientItemExtensions.create(this, new BulletItemRenderer(this)));
	}
}
