package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class GunModificationMenu extends AbstractContainerMenu
{
	public static final int BUTTON_CANCEL = 0;

	private final Container GunContainer;
	private final Container MaterialContainer;
	private final Container BatteryContainer;

	public GunModificationMenu(int containerID, Inventory inventory, FriendlyByteBuf data)
	{
 		super(FlansMod.GUN_MOD_MENU.get(), containerID);

		 BlockPos blockPos = data.readBlockPos();
		 BlockEntity blockEntity = inventory.player.level.getBlockEntity(blockPos);
		 if(blockEntity instanceof GunModBlockEntity gunModBlock)
		 {
			 GunContainer = gunModBlock.GunContainer;
			 MaterialContainer = gunModBlock.MaterialContainer;
			 BatteryContainer = gunModBlock.BatteryContainer;
		 }
		 else
		 {
			 FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			 GunContainer = null; // um?
			 MaterialContainer = null;
			 BatteryContainer = null;
		 }
		 CreateSlots(inventory);
	}

	public GunModificationMenu(int containerID, Inventory playerInventory, Container gunContainer, Container materialContainer, Container batteryContainer)
	{
		super(FlansMod.GUN_MOD_MENU.get(), containerID);
		GunContainer = gunContainer;
		MaterialContainer = materialContainer;
		BatteryContainer = batteryContainer;
		CreateSlots(playerInventory);
	}

	private void CreateSlots(Inventory playerInventory)
	{
		addSlot(new RestrictedSlot(GunContainer, 0, 120, 99));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < MaterialContainer.getContainerSize() / 3 + 1; j++)
			{

			}
		}

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
	public boolean clickMenuButton(Player player, int buttonID)
	{
		switch(buttonID)
		{
			case BUTTON_CANCEL -> { return true; }
		}

		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override
	public boolean stillValid(Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }
}
