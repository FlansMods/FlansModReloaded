package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WorkbenchMenuMaterials extends WorkbenchMenu
{
	public final Container MaterialContainer;

	private RestrictedSlot[] MaterialSlots;

	public WorkbenchMenuMaterials(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull WorkbenchBlockEntity workbench)
	{
		super(FlansMod.WORKBENCH_MENU_MATERIALS.get(), containerID, inventory, workbench);
		MaterialContainer = BlockEntity.MaterialContainer;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenuMaterials(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_MATERIALS.get(), containerID, inventory, data);

		MaterialContainer = BlockEntity.MaterialContainer;
		CreateSlots(inventory, 0);
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
		MaterialSlots = new RestrictedSlot[MaterialContainer.getContainerSize()];
		for(int j = 0; j < MaterialContainer.getContainerSize() / 9 + 1; j++)
		{
			for(int i = 0; i < 9; i++)
			{
				if(j * 9 + i < MaterialContainer.getContainerSize())
				{
					addSlot(MaterialSlots[j * 9 + i] = new RestrictedSlot(MaterialContainer, j * 9 + i, 6 + i * 18, 23 + j * 18));
				}
			}
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(MaterialSlots.length > 0 && slot >= MaterialSlots[0].index && slot < MaterialSlots[0].index + MaterialSlots.length)
		{
			int materialSlotIndex = slot - MaterialSlots[0].index;
			return QuickStackIntoInventory(player, MaterialSlots[materialSlotIndex]);
		}
		else
		{
			ItemStack stack = slots.get(slot).getItem();
			for(RestrictedSlot matSlot : MaterialSlots)
			{
				if(matSlot.getItem().isEmpty() && matSlot.mayPlace(stack))
				{
					matSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}
