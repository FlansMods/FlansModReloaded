package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.*;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public abstract class WorkbenchMenu extends AbstractContainerMenu
{
	@Nonnull
	public final WorkbenchDefinition Def;
	@Nonnull
	public final ContainerData WorkbenchData;
	@Nonnull
	public final WorkbenchBlockEntity BlockEntity;

	private Slot[] InventorySlots;

	public int ScrollIndex = 0;


	public WorkbenchMenu(@Nonnull MenuType<? extends WorkbenchMenu> menuType,
						 int containerID,
						 @Nonnull Inventory inventory,
						 @Nonnull WorkbenchBlockEntity workbench)
	{
		super(menuType, containerID);
		Def = workbench.Def;
		BlockEntity = workbench;
		WorkbenchData = workbench.DataAccess;
	}

	public WorkbenchMenu(@Nonnull MenuType<? extends WorkbenchMenu> menuType,
						 int containerID,
						 @Nonnull Inventory inventory,
						 @Nonnull FriendlyByteBuf data)
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

	@Nonnull
	protected ItemStack QuickStackIntoInventory(@Nonnull Player player, @Nonnull Slot slot)
	{
		if(player.getInventory().add(slot.getItem()))
		{
			slot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		return slot.getItem();
	}

	@Override
	public boolean stillValid(@Nonnull Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }

}
