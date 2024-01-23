package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.crafting.slots.GunCraftingInputSlot;
import com.flansmod.common.crafting.slots.GunCraftingOutputSlot;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.crafting.elements.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WorkbenchMenuGunCrafting extends WorkbenchMenu
{
	public static final int GUN_CRAFTING_NUM_SLOTS_X = 4;
	public static final int GUN_CRAFTING_NUM_SLOTS_Y = 2;

	public static final int BUTTON_SELECT_GUN_RECIPE_0 				= 0x0;	// 128 gun recipes
	public static final int BUTTON_SELECT_GUN_RECIPE_MAX 			= 0x7f;
	public static final int BUTTON_AUTO_FILL_INGREDIENT_0 			= 0x80; // 128 auto-fill buttons
	public static final int BUTTON_AUTO_FULL_INGREDIENT_MAX 		= 0xff;

	public static final int BUTTON_SET_RECIPE_SCROLL_0				= 0x1000; // Needs to be networked??
	public static final int BUTTON_SET_RECIPE_SCROLL_MAX 			= 0x10ff;

	public final Container GunCraftingInputContainer;
	public final Container GunCraftingOutputContainer;


	protected GunCraftingInputSlot[] GunCraftingInputSlots;
	protected com.flansmod.common.crafting.slots.GunCraftingOutputSlot GunCraftingOutputSlot;

	public WorkbenchMenuGunCrafting(int containerID,
									 @Nonnull Inventory inventory,
									 @Nonnull WorkbenchBlockEntity workbench)
	{
		super(FlansMod.WORKBENCH_MENU_GUN_CRAFTING.get(), containerID, inventory, workbench);
		GunCraftingInputContainer = BlockEntity.GunCraftingInputContainer;
		GunCraftingOutputContainer = BlockEntity.GunCraftingOutputContainer;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenuGunCrafting(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_GUN_CRAFTING.get(), containerID, inventory, data);

		GunCraftingInputContainer = BlockEntity.GunCraftingInputContainer;
		GunCraftingOutputContainer = BlockEntity.GunCraftingOutputContainer;
		CreateSlots(inventory, 0);
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		if(BUTTON_SET_RECIPE_SCROLL_0 <= buttonID && buttonID <= BUTTON_SET_RECIPE_SCROLL_MAX)
		{
			int scrollIndex = buttonID - BUTTON_SET_RECIPE_SCROLL_0;
			if(scrollIndex != ScrollIndex)
			{
				ScrollIndex = scrollIndex;
				UpdateActiveSlots();
			}
			return true;
		}
		if(BUTTON_SELECT_GUN_RECIPE_0 <= buttonID && buttonID <= BUTTON_SELECT_GUN_RECIPE_MAX)
		{
			BlockEntity.SelectGunCraftingRecipe(buttonID - BUTTON_SELECT_GUN_RECIPE_0);
			UpdateActiveSlots();
			return true;
		}
		if(BUTTON_AUTO_FILL_INGREDIENT_0 <= buttonID && buttonID <= BUTTON_AUTO_FULL_INGREDIENT_MAX)
		{
			int ingredientIndex = buttonID - BUTTON_AUTO_FILL_INGREDIENT_0;
			BlockEntity.AutoFillGunCraftingInputSlot(player, ingredientIndex);
			return true;
		}
		return false;
	}

	@Override
	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		super.CreateSlots(playerInventory, inventorySlotOffsetX);
		if(GunCraftingOutputContainer.getContainerSize() > 0)
		{
			addSlot(GunCraftingOutputSlot = new GunCraftingOutputSlot(this, GunCraftingOutputContainer, 0, 150, 77));
		}
		GunCraftingInputSlots = new GunCraftingInputSlot[GunCraftingInputContainer.getContainerSize()];
		if(GunCraftingInputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < GunCraftingInputContainer.getContainerSize(); i++)
			{
				addSlot(GunCraftingInputSlots[i] = new GunCraftingInputSlot(this, GunCraftingInputContainer, i, 78, 66));
			}
		}
		UpdateActiveSlots();
	}

	private void UpdateActiveSlots()
	{
		for(int i = 0; i < GunCraftingInputSlots.length; i++)
		{
			boolean active = false;
			if(!GunCraftingInputSlots[i].getItem().isEmpty())
				active = true;

			GunFabricationRecipe gunRecipe = BlockEntity.GetSelectedGunRecipe();
			if(gunRecipe != null)
			{
				if(i < gunRecipe.InputIngredients.size())
					active = true;
			}

			// x,y are final. Let's hack
			GunCraftingInputSlot replacementSlot = new GunCraftingInputSlot(
				this,
				GunCraftingInputContainer,
				i,
				active ? 50 + 20 * (i % GUN_CRAFTING_NUM_SLOTS_X) : -1000,
				active ? 56 + 30 * (i / GUN_CRAFTING_NUM_SLOTS_X) : -1000);

			replacementSlot.index = GunCraftingInputSlots[i].index;
			GunCraftingInputSlots[i].SetActive(false);
			slots.set(replacementSlot.index, replacementSlot);
			GunCraftingInputSlots[i] = replacementSlot;
			GunCraftingInputSlots[i].SetActive(active);
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(GunCraftingOutputSlot != null && slot == GunCraftingOutputSlot.index)
		{
			return QuickStackIntoInventory(player, GunCraftingOutputSlot);
		}
		else if(GunCraftingInputSlots != null && GunCraftingInputSlots.length > 0 && slot >= GunCraftingInputSlots[0].index && slot < GunCraftingInputSlots[0].index + GunCraftingInputSlots.length)
		{
			int craftingInputSlotIndex = slot - GunCraftingInputSlots[0].index;
			return QuickStackIntoInventory(player, GunCraftingInputSlots[craftingInputSlotIndex]);
		}
		else
		{
			ItemStack stack = slots.get(slot).getItem();
			for(RestrictedSlot inputSlot : GunCraftingInputSlots)
			{
				if(inputSlot.getItem().isEmpty() && inputSlot.mayPlace(stack))
				{
					inputSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}
