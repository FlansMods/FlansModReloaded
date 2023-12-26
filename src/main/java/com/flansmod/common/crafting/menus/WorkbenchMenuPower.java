package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WorkbenchMenuPower extends WorkbenchMenu
{
	public final Container BatteryContainer;
	public final Container FuelContainer;

	private RestrictedSlot FuelSlot;
	private RestrictedSlot BatterySlot;

	public WorkbenchMenuPower(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull WorkbenchBlockEntity workbench)
	{
		super(FlansMod.WORKBENCH_MENU_POWER.get(), containerID, inventory, workbench);
		BatteryContainer = BlockEntity.BatteryContainer;
		FuelContainer = BlockEntity.FuelContainer;
	}

	public WorkbenchMenuPower(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_POWER.get(), containerID, inventory, data);

		BatteryContainer = BlockEntity.BatteryContainer;
		FuelContainer = BlockEntity.FuelContainer;
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		return false;
	}

	@Override
	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		super.CreateSlots(playerInventory, inventorySlotOffsetX);
		if(BatteryContainer.getContainerSize() > 0)
		{
			addSlot(BatterySlot = new RestrictedSlot(BatteryContainer, 0, 78, 66));
		}

		if(FuelContainer.getContainerSize() > 0)
		{
			addSlot(FuelSlot = new RestrictedSlot(FuelContainer, 0, 129, 66));
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(FuelSlot != null && slot == FuelSlot.index)
			return QuickStackIntoInventory(player, FuelSlot);
		else if(BatterySlot != null && slot == BatterySlot.index)
			return QuickStackIntoInventory(player, BatterySlot);
		else
		{
			ItemStack stack = slots.get(slot).getItem();
			if(FuelSlot != null && FuelSlot.mayPlace(stack))
			{
				FuelSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			else if(BatterySlot != null && BatterySlot.mayPlace(stack))
			{
				BatterySlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
		}
		return ItemStack.EMPTY;
	}
}
