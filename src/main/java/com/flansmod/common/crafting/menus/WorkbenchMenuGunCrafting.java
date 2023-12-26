package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.GunCraftingInputSlot;
import com.flansmod.common.crafting.GunCraftingOutputSlot;
import com.flansmod.common.crafting.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.crafting.elements.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
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

	public int SelectedGunRecipeIndex = NO_RECIPE_SELECTED;

	public final Container GunCraftingInputContainer;
	public final Container GunCraftingOutputContainer;


	protected GunCraftingInputSlot[] GunCraftingInputSlots;
	protected com.flansmod.common.crafting.GunCraftingOutputSlot GunCraftingOutputSlot;

	public WorkbenchMenuGunCrafting(int containerID,
									 @Nonnull Inventory inventory,
									 @Nonnull WorkbenchBlockEntity workbench)
	{
		super(FlansMod.WORKBENCH_MENU_GUN_CRAFTING.get(), containerID, inventory, workbench);
		GunCraftingInputContainer = BlockEntity.GunCraftingInputContainer;
		GunCraftingOutputContainer = BlockEntity.GunCraftingOutputContainer;
	}

	public WorkbenchMenuGunCrafting(int containerID,
									@Nonnull Inventory inventory,
									@Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_GUN_CRAFTING.get(), containerID, inventory, data);

		GunCraftingInputContainer = BlockEntity.GunCraftingInputContainer;
		GunCraftingOutputContainer = BlockEntity.GunCraftingOutputContainer;
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
			SelectedGunRecipeIndex = buttonID - BUTTON_SELECT_GUN_RECIPE_0;
			UpdateGunCraftingOutput();
			UpdateActiveSlots();
			return true;
		}
		if(BUTTON_AUTO_FILL_INGREDIENT_0 <= buttonID && buttonID <= BUTTON_AUTO_FULL_INGREDIENT_MAX)
		{
			int ingredientIndex = buttonID - BUTTON_AUTO_FILL_INGREDIENT_0;
			// Auto fill operates only on the containers, so we can do it in the Menu, not the BlockEntity
			AutoFillGunCraftingInputSlot(player, ingredientIndex);
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
			if(SelectedGunRecipeIndex >= 0)
			{
				GunCraftingEntryDefinition selectedEntry = GetGunCraftingEntry(SelectedGunRecipeIndex);
				if(selectedEntry != null)
				{
					int slotCount = 0;
					for(RecipePartDefinition part : selectedEntry.parts)
						slotCount += part.tieredIngredients.length + part.additionalIngredients.length;
					if(i < slotCount)
						active = true;
				}
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

	public void AutoFillGunCraftingInputSlot(@Nonnull Player player, int inputSlotIndex)
	{
		if(SelectedGunRecipeIndex >= 0
			&& 0 <= inputSlotIndex && inputSlotIndex < GunCraftingInputSlots.length
			&& GunCraftingInputSlots[inputSlotIndex].getItem().isEmpty())
		{
			GunCraftingEntryDefinition selectedEntry = GetGunCraftingEntry(SelectedGunRecipeIndex);
			Either<TieredIngredientDefinition, IngredientDefinition> ingredient = selectedEntry.GetIngredient(inputSlotIndex);
			if(ingredient != null)
			{
				ingredient.ifLeft((TieredIngredientDefinition tiered) -> {
					if(player.isCreative())
					{
						List<ItemStack> options = new ArrayList<>();
						tiered.GenerateMatches(options);
						if(options.size() > 0)
							GunCraftingInputSlots[inputSlotIndex].set(options.get(0));
					}
					else
					{
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack playerItem = player.getInventory().getItem(i);
							if(tiered.Matches(playerItem))
							{
								GunCraftingInputSlots[inputSlotIndex].set(playerItem.copyWithCount(1));
								playerItem.setCount(playerItem.getCount() - 1);
								break;
							}
						}
					}
				});

				ingredient.ifRight((IngredientDefinition basic) -> {
					if(player.isCreative())
					{
						List<ItemStack> options = new ArrayList<>();
						basic.GenerateMatches(options);
						if(options.size() > 0)
							GunCraftingInputSlots[inputSlotIndex].set(options.get(0));
					}
					else
					{
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack playerItem = player.getInventory().getItem(i);
							if(basic.Matches(playerItem))
							{
								GunCraftingInputSlots[inputSlotIndex].set(playerItem.copyWithCount(1));
								playerItem.setCount(playerItem.getCount() - 1);
								break;
							}
						}
					}
				});
			}

		}
	}

	public void ConsumeGunCraftingInputs()
	{
		if(GunCraftingOutputContainer.getContainerSize() > 0
			&& !GunCraftingOutputContainer.isEmpty()
			&& GunCraftingInputContainer.getContainerSize() > 0
			&& SelectedGunRecipeIndex >= 0)
		{
			GunCraftingEntryDefinition selectedEntry = GetGunCraftingEntry(SelectedGunRecipeIndex);
			if(selectedEntry != null)
			{
				int inputSlotIndex = 0;

				for(RecipePartDefinition part : selectedEntry.parts)
				{
					for(TieredIngredientDefinition tiered : part.tieredIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(tiered.Matches(input))
							input.setCount(input.getCount() - 1);

						inputSlotIndex++;
					}
					for(IngredientDefinition specific : part.additionalIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(specific.Matches(input))
							input.setCount(input.getCount() - specific.count);

						inputSlotIndex++;
					}
				}
			}
		}
	}

	public void UpdateGunCraftingOutput()
	{
		if(GunCraftingOutputContainer.getContainerSize() > 0 && GunCraftingInputContainer.getContainerSize() > 0 && SelectedGunRecipeIndex >= 0)
		{
			GunCraftingEntryDefinition selectedEntry = GetGunCraftingEntry(SelectedGunRecipeIndex);
			if(selectedEntry != null)
			{
				int inputSlotIndex = 0;
				List<ItemStack> craftedFromParts = new ArrayList<>();
				boolean match = true;

				for(RecipePartDefinition part : selectedEntry.parts)
				{
					for(TieredIngredientDefinition tiered : part.tieredIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(tiered.Matches(input))
							craftedFromParts.add(input);
						else
							match = false;

						inputSlotIndex++;
					}
					for(IngredientDefinition specific : part.additionalIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(specific.Matches(input))
							craftedFromParts.add(input);
						else
							match = false;

						inputSlotIndex++;
					}
				}

				if(match)
				{
					ItemStack stack = selectedEntry.outputs[0].CreateStack();
					FlanItem.SetCraftingInputs(stack, craftedFromParts);
					GunCraftingOutputContainer.setItem(0, stack);
				}
				else
				{
					GunCraftingOutputContainer.setItem(0, ItemStack.EMPTY);
				}
			}
		}
	}

	@Nullable
	public GunCraftingEntryDefinition GetSelectedGunCraftingEntry()
	{
		return SelectedGunRecipeIndex >= 0 ? GetGunCraftingEntry(SelectedGunRecipeIndex) : null;
	}

	@Nullable
	public GunCraftingEntryDefinition GetGunCraftingEntry(int recipeIndex)
	{
		int index = 0;
		for(GunCraftingPageDefinition page : Def.gunCrafting.pages)
		{
			if(index <= recipeIndex && recipeIndex < index + page.entries.length)
			{
				return page.entries[recipeIndex - index];
			}
			else index += page.entries.length;
		}
		return null;
	}
}
