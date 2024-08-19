package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class TemporaryWorkbenchItem extends Item
{
	public final ResourceLocation DefLoc;

	public TemporaryWorkbenchItem(@Nonnull ResourceLocation workbenchDefLoc)
	{
		super(new Properties());
		DefLoc = workbenchDefLoc;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		ItemStack itemstack = player.getItemInHand(hand);

		if (!world.isClientSide)
		{
			if(FlansMod.INVENTORY_MANAGER.OpenTemporaryInventory(player, DefLoc))
			{
				return InteractionResultHolder.consume(itemstack);
			}
		}

		return InteractionResultHolder.pass(itemstack);
	}
}
