package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WorkbenchMenuPartCrafting extends WorkbenchMenu
{
	public static final int PART_CRAFTING_NUM_INPUT_SLOTS_X = 4;
	public static final int PART_CRAFTING_NUM_INPUT_SLOTS_Y = 11;
	public static final int PART_CRAFTING_INPUT_SLOTS_X = 5;
	public static final int PART_CRAFTING_INPUT_SLOTS_Y = 18;
	public static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_X = 4;
	public static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_Y = 6;
	public static final int PART_CRAFTING_OUTPUT_SLOTS_X = 279;
	public static final int PART_CRAFTING_OUTPUT_SLOTS_Y = 115;

	public static final int BUTTON_CRAFT_1 							= 0x00; // Technically there are 16 buttons here
	public static final int BUTTON_CRAFT_5							= 0x05;
	public static final int BUTTON_CRAFT_ALL						= 0x0f;
	public static final int BUTTON_QUEUE_CANCEL_0 					= 0x10; // 16 queue cancels
	public static final int BUTTON_QUEUE_CANCEL_MAX					= 0x1f;
	public static final int BUTTON_SELECT_PART_RECIPE_0				= 0x20; // many part recipes
	public static final int BUTTON_SELECT_PART_RECIPE_MAX			= 0xff;

	// Containers
	public final Container PartCraftingInputContainer;
	public final Container PartCraftingOutputContainer;
	// Slots
	protected RestrictedSlot[] PartCraftingInputSlots;
	protected RestrictedSlot[] PartCraftingOutputSlots;

	public WorkbenchMenuPartCrafting(int containerID,
									 @Nonnull Inventory inventory,
									 @Nonnull AbstractWorkbench workbench)
	{
		super(FlansMod.WORKBENCH_MENU_PART_CRAFTING.get(), containerID, inventory, workbench);
		PartCraftingInputContainer = Workbench.PartCraftingInputContainer;
		PartCraftingOutputContainer = Workbench.PartCraftingOutputContainer;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenuPartCrafting(int containerID,
									 @Nonnull Inventory inventory,
									 @Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_PART_CRAFTING.get(), containerID, inventory, data);

		PartCraftingInputContainer = Workbench.PartCraftingInputContainer;
		PartCraftingOutputContainer = Workbench.PartCraftingOutputContainer;
		CreateSlots(inventory, 0);
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		if(BUTTON_CRAFT_1 <= buttonID && buttonID < BUTTON_CRAFT_ALL)
		{
			int craftCount = buttonID - BUTTON_CRAFT_1 + 1;
			CraftParts(craftCount);
			return true;
		}
		if(buttonID == BUTTON_CRAFT_ALL)
		{
			CraftParts(-1);
			return true;
		}
		if(BUTTON_SELECT_PART_RECIPE_0 <= buttonID && buttonID <= BUTTON_SELECT_PART_RECIPE_MAX)
		{
			Workbench.SelectPartCraftingRecipe(player.level(), buttonID - BUTTON_SELECT_PART_RECIPE_0);
			return true;
		}
		if(BUTTON_QUEUE_CANCEL_0 <= buttonID && buttonID <= BUTTON_QUEUE_CANCEL_MAX)
		{
			int cancelIndex = buttonID - BUTTON_QUEUE_CANCEL_0;
			Workbench.CancelQueue(cancelIndex);
			return true;
		}
		return false;
	}


	@Override
	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		super.CreateSlots(playerInventory, inventorySlotOffsetX + 92);

		PartCraftingInputSlots = new RestrictedSlot[PartCraftingInputContainer.getContainerSize()];
		if(PartCraftingInputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
			{
				int x = i % PART_CRAFTING_NUM_INPUT_SLOTS_X;
				int y = i / PART_CRAFTING_NUM_INPUT_SLOTS_X;
				addSlot(PartCraftingInputSlots[i] = new RestrictedSlot(PartCraftingInputContainer, i, PART_CRAFTING_INPUT_SLOTS_X + 1 + x * 18, PART_CRAFTING_INPUT_SLOTS_Y + 1 + y * 18));
			}
		}
		PartCraftingOutputSlots = new RestrictedSlot[PartCraftingOutputContainer.getContainerSize()];
		if(PartCraftingOutputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < PartCraftingOutputContainer.getContainerSize(); i++)
			{
				int x = i % PART_CRAFTING_NUM_OUTPUT_SLOTS_X;
				int y = i / PART_CRAFTING_NUM_OUTPUT_SLOTS_X;
				addSlot(PartCraftingOutputSlots[i] = new RestrictedSlot(PartCraftingOutputContainer, i, PART_CRAFTING_OUTPUT_SLOTS_X + 1 + x * 18, PART_CRAFTING_OUTPUT_SLOTS_Y + 1 + y * 18));
			}
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(PartCraftingInputSlots != null && PartCraftingInputSlots.length > 0 && slot >= PartCraftingInputSlots[0].index && slot < PartCraftingInputSlots[0].index + PartCraftingInputSlots.length)
		{
			int craftingInputSlotIndex = slot - PartCraftingInputSlots[0].index;
			return QuickStackIntoInventory(player, PartCraftingInputSlots[craftingInputSlotIndex]);
		}
		else if(PartCraftingOutputSlots != null && PartCraftingOutputSlots.length > 0 && slot >= PartCraftingOutputSlots[0].index && slot < PartCraftingOutputSlots[0].index + PartCraftingOutputSlots.length)
		{
			int craftingOutputSlotIndex = slot - PartCraftingOutputSlots[0].index;
			return QuickStackIntoInventory(player, PartCraftingOutputSlots[craftingOutputSlotIndex]);
		}
		else
		{
			ItemStack stack = slots.get(slot).getItem();
			for(RestrictedSlot partInputSlot : PartCraftingInputSlots)
			{
				if(partInputSlot.getItem().isEmpty())
				{
					partInputSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public void CraftParts(int count)
	{
		Workbench.QueueCrafting(World, count);
	}
}


