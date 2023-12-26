package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.*;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class WorkbenchMenu extends AbstractContainerMenu
{
	public final WorkbenchDefinition Def;
	public final ContainerData WorkbenchData;
	public final WorkbenchBlockEntity BlockEntity;

	public static final int NO_RECIPE_SELECTED = -1;

	private Slot[] InventorySlots;

	public int ScrollIndex = 0;


	public WorkbenchMenu(MenuType<? extends WorkbenchMenu> menuType,
						 int containerID,
						 @Nonnull Inventory inventory,
						 @Nonnull WorkbenchBlockEntity workbench)
	{
		super(menuType, containerID);
		Def = workbench.Def;
		BlockEntity = workbench;
		WorkbenchData = workbench.DataAccess;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenu(MenuType<? extends WorkbenchMenu> menuType, int containerID, Inventory inventory, FriendlyByteBuf data)
	{
		super(menuType, containerID);

		BlockPos blockPos = data.readBlockPos();
		BlockEntity blockEntity = inventory.player.level().getBlockEntity(blockPos);
		if(blockEntity instanceof WorkbenchBlockEntity workbenchBlockEntity)
		{
			Def = workbenchBlockEntity.Def;
			BlockEntity = workbenchBlockEntity;
			WorkbenchData = workbenchBlockEntity.DataAccess;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			Def = WorkbenchDefinition.INVALID;
			BlockEntity = null;
			WorkbenchData = null;
		}
		CreateSlots(inventory, 0);
	}

	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		addDataSlots(WorkbenchData);

		InventorySlots = new Slot[4 * 9];
		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				addSlot(InventorySlots[x + 9 * y + 9] = new RestrictedSlot(playerInventory, x + y * 9 + 9, inventorySlotOffsetX + 6 + x * 18, 137 + y * 18));
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			addSlot(InventorySlots[x] = new RestrictedSlot(playerInventory, x, inventorySlotOffsetX + 6 + x * 18, 195));
		}
	}

	protected ItemStack QuickStackIntoInventory(Player player, Slot slot)
	{
		if(player.getInventory().add(slot.getItem()))
		{
			slot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		return slot.getItem();
	}

	@Override
	public boolean stillValid(Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }

}
