package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GunModificationMenu extends AbstractContainerMenu
{
	public static final int BUTTON_CANCEL = 0;

	private final Container GunContainer;

	public GunModificationMenu(int containerID, Inventory inventory, FriendlyByteBuf data)
	{
		super(FlansMod.GUN_MOD_MENU.get(), containerID);
		GunContainer = null; // um?
	}

	public GunModificationMenu(int containerID, Container gunContainer)
	{
		super(FlansMod.GUN_MOD_MENU.get(), containerID);
		GunContainer = gunContainer;
		addSlot(new Slot(GunContainer, 0, 0, 0));
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
	public boolean stillValid(Player player) { return GunContainer.stillValid(player); }
}
