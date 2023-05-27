package com.flansmod.common.energy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.RestrictedSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GeneratorMenu extends AbstractContainerMenu
{
	private final Container FuelContainer;

	public GeneratorMenu(int containerID, Inventory inventory, FriendlyByteBuf data)
	{
		super(FlansMod.GENERATOR_MENU.get(), containerID);
		BlockPos blockPos = data.readBlockPos();
		BlockEntity blockEntity = inventory.player.level.getBlockEntity(blockPos);
		if(blockEntity instanceof GeneratorBlockEntity generatorBlock)
		{
			FuelContainer = generatorBlock.FuelContainer;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GeneratorMenu data");
			FuelContainer = null; // um?
		}
		CreateSlots(inventory);
	}

	public GeneratorMenu(int containerID, Inventory inventory, Container fuelContainer)
	{
		super(FlansMod.GENERATOR_MENU.get(), containerID);
		FuelContainer =  fuelContainer;
		CreateSlots(inventory);
	}

	private void CreateSlots(Inventory playerInventory)
	{
		addSlot(new RestrictedSlot(FuelContainer, 0, 120, 99));

		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				addSlot(new RestrictedSlot(playerInventory, x + y * 9 + 9, 48 + x * 18, 128 + y * 18));
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			addSlot(new RestrictedSlot(playerInventory, x, 48 + x * 18, 186));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override
	public boolean stillValid(Player player) { return true; } //return FuelContainer != null && FuelContainer.stillValid(player); }

}
