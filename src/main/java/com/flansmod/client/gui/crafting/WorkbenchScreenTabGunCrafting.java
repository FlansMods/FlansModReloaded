package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ContextCache;
import com.flansmod.common.crafting.ingredients.TieredPartIngredient;
import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WorkbenchScreenTabGunCrafting extends WorkbenchScreenTab<WorkbenchMenuGunCrafting>
{
	private enum PartBackgroundType
	{
		Barrel(0, 0),
		UpperReceiver(1, 0),
		LowerReciever(2, 0),
		Stock(3, 0),
		Grip(4, 0),
		Handguard(11, 0),
		BoltAction(12, 0),
		RevolverAction(13, 0),
		Scope(5, 0),
		Armour(6, 0),
		Seat(7, 0),
		Wheel(8, 0),
		Canvas(9, 0),
		Fabric(10, 0),

		Unknown(2, 1),
		Powder(3, 1),
		Ingot(4, 1),
		Plate(5, 1),

		Circuitry(7, 1),
		Engine(8, 1),
		StructuralParts(9, 1),
		Propeller(10, 1),
		MechanicalParts(11, 1);

		public static PartBackgroundType GetFromTag(String tag)
		{
			switch(tag)
			{
				case "flansmod:barrel": return Barrel;
				case "flansmod:upper_receiver": return UpperReceiver;
				case "flansmod:lower_receiver": return LowerReciever;
				case "flansmod:stock": return Stock;
				case "flansmod:grip": return Grip;
				case "flansmod:handguard": return Handguard;
				case "flansmod:bolt_action": return BoltAction;
				case "flansmod:revolver_action": return RevolverAction;
				case "flansmod:scope": return Scope;
				case "flansmod:armour": return Armour;
				case "flansmod:seat": return Seat;
				case "flansmod:wheel": return Wheel;
				case "flansmod:canvas": return Canvas;
				case "flansmod:fabric": return Fabric;
				case "flansmod:circuitry": return Circuitry;
				case "flansmod:engine": return Engine;
				case "flansmod:structural_parts": return StructuralParts;
				case "flansmod:propeller": return Propeller;
				case "flansmod:mechanical_parts": return MechanicalParts;
			}

			return Unknown;
		}

		public final int texX;
		public final int texY;

		PartBackgroundType(int x, int y)
		{
			texX = 2 + x * 18;
			texY = 220 + y * 18;
		}
	}

	private static final ResourceLocation GUN_FABRICATION_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_fabrication.png");
	private static final int FAB_W = 256;
	private static final int FAB_H = 256;


	private final List<Pair<Integer, GunFabricationRecipe>> FilteredGunsList = new ArrayList<>();
	private float gunSelectorScrollOffset = 0.0f;
	private static final int GUN_SELECTOR_X_ORIGIN = 5;
	private static final int GUN_SELECTOR_Y_ORIGIN = 16;
	private static final int GUN_SELECTOR_COLUMNS = 2;
	private static final int GUN_SELECTOR_ROWS = 5;
	private Button[] GunSelectionButtons;
	private float ShowPotentialMatchTicker = 0.0f;

	private static final int GUN_STATS_X_ORIGIN = 50;
	private static final int GUN_STATS_Y_ORIGIN = 17;

	private static final String DISALLOWED_TAG = "crafting.disallowed_tag";
	private static final String REQUIRED_TAG = "crafting.required_tag";

	private float recipeSelectorScrollOffset = 0.0f;
	private static final int GUN_RECIPE_VIEWER_X_ORIGIN = 48;
	private static final int GUN_RECIPE_VIEWER_Y_ORIGIN = 54;
	private static final int GUN_RECIPE_VIEWER_COLUMNS = 4;
	private static final int GUN_RECIPE_VIEWER_ROWS = 2;
	private Button[] GoToPartCraftingButtons;
	private Button[] AutoFillCraftingButtons;

	public WorkbenchScreenTabGunCrafting(@Nonnull WorkbenchMenuGunCrafting menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
	}

	@Override
	protected boolean IsTabPresent() { return Workbench.Def.gunCrafting.isActive; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_gun_crafting"); }
	@Override
	protected void InitTab()
	{
		GunSelectionButtons = new Button[GUN_SELECTOR_ROWS * GUN_SELECTOR_COLUMNS];
		for(int j = 0; j < GUN_SELECTOR_ROWS; j++)
		{
			for(int i = 0; i < GUN_SELECTOR_COLUMNS; i++)
			{
				final int index = j * GUN_SELECTOR_COLUMNS + i;
				GunSelectionButtons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							SelectRecipe(index);
						})
					.bounds(xOrigin + GUN_SELECTOR_X_ORIGIN + i*18, yOrigin + GUN_SELECTOR_Y_ORIGIN + j*18, 18, 18)
					.build();
				addWidget(GunSelectionButtons[index]);
			}
		}

		GoToPartCraftingButtons = new Button[GUN_RECIPE_VIEWER_ROWS * GUN_RECIPE_VIEWER_COLUMNS];
		AutoFillCraftingButtons = new Button[GUN_RECIPE_VIEWER_ROWS * GUN_RECIPE_VIEWER_COLUMNS];
		for(int j = 0; j < GUN_RECIPE_VIEWER_ROWS; j++)
		{
			for(int i = 0; i < GUN_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * GUN_RECIPE_VIEWER_COLUMNS + i;
				GoToPartCraftingButtons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							GoToPartCrafting(index);
						})
					.bounds(xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 9 + 20 * i, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 19 + 30 * j, 9, 9)
					.build();
				addWidget(GoToPartCraftingButtons[index]);
				AutoFillCraftingButtons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							AutoFillCraftingSlot(index);
						})
					.bounds(xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 20 * i, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 19 + 30 * j, 9, 9)
					.build();
				addWidget(AutoFillCraftingButtons[index]);
			}
		}
	}
	@Override
	protected void OnTabSelected(boolean selected)
	{
		RefreshGunCraftingFilters();
		UpdateActiveGunSelectionButtons();
		UpdateActiveRecipeButtons();
	}
	@Override
	protected void UpdateTab(boolean selected)
	{
		ShowPotentialMatchTicker += 1.0f / 20.0f;
		UpdateActiveGunSelectionButtons();
		UpdateActiveRecipeButtons();
	}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll)
	{
		if(InBox(xMouse, yMouse, xOrigin + GUN_SELECTOR_X_ORIGIN, 18 * GUN_SELECTOR_COLUMNS + 6, yOrigin + GUN_SELECTOR_Y_ORIGIN, 18 * GUN_SELECTOR_ROWS))
		{
			int numRows = Maths.Max(FilteredGunsList.size() / GUN_SELECTOR_COLUMNS - GUN_SELECTOR_ROWS + 1, 0);
			gunSelectorScrollOffset -= scroll;
			gunSelectorScrollOffset = Maths.Clamp(gunSelectorScrollOffset, 0, numRows);
			UpdateActiveGunSelectionButtons();
			return true;
		}
		else if(InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN, 20 * GUN_RECIPE_VIEWER_COLUMNS + 6, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN, GUN_RECIPE_VIEWER_ROWS * 30))
		{
			GunFabricationRecipe gunFabRecipe = Workbench.BlockEntity.GetSelectedGunRecipe();
			int numSlots = gunFabRecipe != null ? gunFabRecipe.InputIngredients.size() : 0;
			int numRows = Maths.Max(numSlots / GUN_RECIPE_VIEWER_COLUMNS - GUN_RECIPE_VIEWER_ROWS + 1, 0);
			recipeSelectorScrollOffset -= scroll;
			recipeSelectorScrollOffset = Maths.Clamp(recipeSelectorScrollOffset, 0, numRows);
			UpdateActiveRecipeButtons();
			NetworkedButtonPress(WorkbenchMenuGunCrafting.BUTTON_SET_RECIPE_SCROLL_0 + Maths.Floor(recipeSelectorScrollOffset));
			return true;
		}

		return false;
	}
	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		GunFabricationRecipe gunFabRecipe = Workbench.BlockEntity.GetSelectedGunRecipe();
		int numSlots = gunFabRecipe != null ? gunFabRecipe.InputIngredients.size() : 0;

		int firstRow = Maths.Floor(recipeSelectorScrollOffset);
		for (int j = 0; j < GUN_RECIPE_VIEWER_ROWS; j++)
		{
			for (int i = 0; i < GUN_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * GUN_RECIPE_VIEWER_COLUMNS + i;
				if(gunFabRecipe != null && index < numSlots)
				{
					if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 1 + 20 * i, 18, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 1 + 30 * j, 18))
					{
						Ingredient ingredient = gunFabRecipe.InputIngredients.get(index);
						if(ingredient instanceof TieredPartIngredient partIngredient)
						{
							List<Component> lines = partIngredient.GenerateTooltip(false);
							graphics.renderTooltip(font, lines, Optional.empty(), xMouse, yMouse);
							return true;
						}
						else
						{
							//IngredientDefinition ingredientDef = CachedSlotInfo.get(index).GetAsAdditionalDef();
							//if(ingredientDef != null)
							//{
							//	if(ingredientDef.compareItemName)
							//		graphics.renderTooltip(font, Component.translatable(ingredientDef.itemName), xMouse, yMouse);
							//	else if(ingredientDef.compareItemTags)
							//	{
							//		List<FormattedCharSequence> lines = new ArrayList<>();
							//		for (String disallowed : ingredientDef.disallowedTags)
							//		{
							//			lines.add(Component.translatable(DISALLOWED_TAG, Component.translatable(disallowed)).getVisualOrderText());
							//		}
							//		for(String required : ingredientDef.requiredTags)
							//		{
							//			lines.add(Component.translatable(REQUIRED_TAG, Component.translatable(required)).getVisualOrderText());
							//		}
							//		graphics.renderTooltip(font, lines, xMouse, yMouse);
							//	}
							//	return true;
							//}
						}
					}
					if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 20 * i, 9, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 20 + 30 * j, 9))
					{
						graphics.renderTooltip(font, Component.translatable("crafting.auto_add_best_parts"), xMouse, yMouse);
						return true;
					}
					if(Workbench.Def.partCrafting.isActive)
					{
						if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 10 + 20 * i, 9, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 20 + 30 * j, 9))
						{
							graphics.renderTooltip(font, Component.translatable("crafting.switch_to_part_crafting"), xMouse, yMouse);
							return true;
						}
					}
				}
			}
		}

		firstRow = Maths.Floor(gunSelectorScrollOffset);
		for (int row = 0; row < GUN_SELECTOR_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
			for (int col = 0; col < GUN_SELECTOR_COLUMNS; col++)
			{
				final int index = firstIndexInRow + col;
				if (index < FilteredGunsList.size())
				{
					if (InBox(xMouse, yMouse, xOrigin + GUN_SELECTOR_X_ORIGIN + 18 * col, 18, yOrigin + GUN_SELECTOR_Y_ORIGIN + 18 * row, 18))
					{
						GunFabricationRecipe entry = FilteredGunsList.get(index).getSecond();
						graphics.renderTooltip(font, entry.Result.getDisplayName(), xMouse, yMouse);
						return true;
					}
				}
			}
		}

		if(gunFabRecipe != null)
		{
			ItemStack stack = gunFabRecipe.Result;
			GunContext context = GunContext.of(stack);
			if(context.IsValid())
			{
				int statBoxX = xOrigin + GUN_STATS_X_ORIGIN;
				int statBoxY = yOrigin + GUN_STATS_Y_ORIGIN + 10;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_SHOT_SPREAD, 1.0f, context, xMouse, yMouse, statBoxX, statBoxY))
					return true;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_IMPACT_DAMAGE, 1.0f, context, xMouse, yMouse, statBoxX, statBoxY + 10))
					return true;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_SHOT_SPEED, 1.0f, context, xMouse, yMouse, statBoxX + 32, statBoxY))
					return true;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_IMPACT_KNOCKBACK, 1.0f, context, xMouse, yMouse, statBoxX + 32, statBoxY + 10))
					return true;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_SHOT_BULLET_COUNT, 1.0f, context, xMouse, yMouse, statBoxX + 64, statBoxY))
					return true;
				if(RenderTooltipForStatComparison(graphics, ModifierDefinition.STAT_SHOT_VERTICAL_RECOIL, 1.0f, context, xMouse, yMouse, statBoxX + 64, statBoxY + 10))
					return true;
			}
		}

		return false;
	}
	private boolean RenderTooltipForStatComparison(@Nonnull GuiGraphics graphics, String stat, float baseValue, GunContext context, int xMouse, int yMouse, int boxX, int boxY)
	{
		if (InBox(xMouse, yMouse, boxX, 30, boxY, 9))
		{
			ModifierStack modStack = new ModifierStack(stat, Actions.DefaultPrimaryActionKey);
			context.Apply(modStack);
			graphics.renderTooltip(font, Component.translatable("tooltip.format." + stat + ".advanced", modStack.ApplyTo(baseValue)), xMouse, yMouse);
			return true;
		}
		return false;
	}

	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		GunFabricationRecipe gunFabRecipe = Workbench.BlockEntity.GetSelectedGunRecipe();

		if (gunFabRecipe != null)
		{
			// Render the gun before the background so it ends up behind
			ItemStack stack = gunFabRecipe.Result;
			Render3DGun(graphics, xOrigin + imageWidth + 64, yOrigin + 64,
				Minecraft.getInstance().level.getGameTime() * 0.01f, -45f, stack);
		}

		// Always render a BG
		graphics.blit(GUN_FABRICATION_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, FAB_W, FAB_H);


		if (gunFabRecipe != null)
		{
			int numSlots = gunFabRecipe.InputIngredients.size();
			int firstRow = Maths.Floor(recipeSelectorScrollOffset);
			int lastRow = firstRow + GUN_RECIPE_VIEWER_ROWS - 1;
			for (int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for (int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = y * GUN_RECIPE_VIEWER_COLUMNS + x;

					if (index < numSlots)
					{
						// Render this button
						Ingredient ingredient = gunFabRecipe.InputIngredients.get(index);
						int slotX = xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + x * 20;
						int slotY = yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + y * 30;
						graphics.blit(GUN_FABRICATION_BG, slotX, slotY, 172, 144, 20, 30, FAB_W, FAB_H);

						// Render the "Go to Part Crafting" button
						if (!Workbench.Def.partCrafting.isActive)
						{
							// Part Crafting Disabled
							graphics.blit(GUN_FABRICATION_BG, slotX + 10, slotY + 20, 182, 185, 9, 9, FAB_W, FAB_H);
						}

						if (ingredient instanceof TieredPartIngredient tieredPartIngredient)
						{
							int tier = tieredPartIngredient.MaterialTierMin;
							PartBackgroundType bgType = PartBackgroundType.GetFromTag(tieredPartIngredient.MatchTag);
							graphics.blit(GUN_FABRICATION_BG, slotX + 2, slotY + 2, bgType.texX, bgType.texY, 16, 16, FAB_W, FAB_H);
							graphics.blit(GUN_FABRICATION_BG, slotX, slotY + 9, (tier - 1) * 9, 247, 9, 9, FAB_W, FAB_H);
						} else
						{
							// Render the background shape

							// Render a faded item stack
							// (Defer to later)
						}
					} else if (index < Workbench.GunCraftingInputContainer.getContainerSize()
						&& !Workbench.GunCraftingInputContainer.getItem(index).isEmpty())
					{
						int slotX = xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + x * 20;
						int slotY = yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + y * 30;
						graphics.blit(GUN_FABRICATION_BG, slotX, slotY, 172, 144, 20, 20, FAB_W, FAB_H);
					}
				}
			}

			// Render a scrollbar
			int numRecipeRows = gunFabRecipe.InputIngredients.size() / GUN_RECIPE_VIEWER_COLUMNS - GUN_RECIPE_VIEWER_ROWS + 1;
			RenderScrollbar(graphics, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 20 * GUN_RECIPE_VIEWER_COLUMNS, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN, 6, 30 * GUN_RECIPE_VIEWER_ROWS, recipeSelectorScrollOffset, 0, numRecipeRows);

			// If the player has a gun in hand, we can do a comparison
			int statBoxX = xOrigin + GUN_STATS_X_ORIGIN;
			int statBoxY = yOrigin + GUN_STATS_Y_ORIGIN + 10;

			// Otherwise, just render the bars
			RenderStatComparisonBar(graphics, statBoxX, statBoxY, 10, 10, 0, 20, 0);
			RenderStatComparisonBar(graphics, statBoxX, statBoxY + 10, 8, 8, 0, 20, 1);
			RenderStatComparisonBar(graphics, statBoxX + 31, statBoxY, 6, 6, 0, 20, 2);
			RenderStatComparisonBar(graphics, statBoxX + 31, statBoxY + 10, 13, 13, 0, 20, 3);
			RenderStatComparisonBar(graphics, statBoxX + 62, statBoxY, 6, 6, 0, 20, 4);
			RenderStatComparisonBar(graphics, statBoxX + 62, statBoxY + 10, 13, 13, 0, 20, 5);
		}

		{
			int firstRow = Maths.Floor(gunSelectorScrollOffset);
			for (int row = 0; row < GUN_SELECTOR_ROWS; row++)
			{
				int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
				int numEntriesInRow = Maths.Min(GUN_SELECTOR_COLUMNS, FilteredGunsList.size() - firstIndexInRow);

				graphics.blit(GUN_FABRICATION_BG, xOrigin + GUN_SELECTOR_X_ORIGIN, yOrigin + GUN_SELECTOR_Y_ORIGIN + row * 18, 172, 0, 18 * numEntriesInRow, 18, FAB_W, FAB_H);
			}
		}

		// Render a scrollbar
		int numRows = FilteredGunsList.size() / GUN_SELECTOR_COLUMNS - GUN_SELECTOR_ROWS + 1;
		RenderScrollbar(graphics, xOrigin + GUN_SELECTOR_X_ORIGIN + 18 * GUN_SELECTOR_COLUMNS, yOrigin + GUN_SELECTOR_Y_ORIGIN, 6, 90, gunSelectorScrollOffset, 0, numRows);
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		GunFabricationRecipe gunFabRecipe = Workbench.BlockEntity.GetSelectedGunRecipe();
		if(gunFabRecipe != null)
		{
			graphics.drawString(font, gunFabRecipe.Result.getDisplayName(), GUN_STATS_X_ORIGIN, GUN_STATS_Y_ORIGIN, 0x404040, false);
		}
		else
		{
			graphics.drawString(font, Component.translatable("crafting.select_a_recipe"),50, 21, 0x404040, false);
		}

		// Render all ItemStacks into the menu
		if(gunFabRecipe != null)
		{
			int firstRow = Maths.Floor(recipeSelectorScrollOffset);
			for (int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for (int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = (firstRow + y) * GUN_RECIPE_VIEWER_COLUMNS + x;
					if (index < gunFabRecipe.InputIngredients.size())
					{
						// Render an example item that would fit into this slot
						//GunCraftingSlotInfo slotInfo = CachedSlotInfo.get(index);
						//ItemStack stack = slotInfo.GetPotentialMatch(Maths.Floor(ShowPotentialMatchTicker));
					}
				}
			}
		}

		// Render gun icons into the selector scrollbar
		int firstRow = Maths.Floor(gunSelectorScrollOffset);
		for (int row = 0; row < GUN_SELECTOR_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
			for(int col = 0; col < GUN_SELECTOR_COLUMNS; col++)
			{
				int index = firstIndexInRow + col;

				if(index < FilteredGunsList.size())
				{
					GunFabricationRecipe entry = FilteredGunsList.get(index).getSecond();
					if(entry != null)
					{
						ItemStack stack = entry.Result;
						RenderGUIItem(graphics, GUN_SELECTOR_X_ORIGIN + 1 + 18 * col, GUN_SELECTOR_Y_ORIGIN + 1 + 18 * row, stack, true);
					}
				}
			}

		}
	}

	// ------------------------------------------------------------------------------------------

	private void UpdateActiveGunSelectionButtons()
	{
		if(GunSelectionButtons != null)
		{
			for (int j = 0; j < GUN_SELECTOR_ROWS; j++)
			{
				for (int i = 0; i < GUN_SELECTOR_COLUMNS; i++)
				{
					final int firstIndex = Maths.Floor(gunSelectorScrollOffset) * GUN_SELECTOR_COLUMNS;
					final int relativeIndex = i + GUN_SELECTOR_COLUMNS * j;
					GunSelectionButtons[relativeIndex].active = IsActive && (firstIndex + relativeIndex < FilteredGunsList.size());
				}
			}
		}
	}

	private void UpdateActiveRecipeButtons()
	{
		final int firstIndex = Maths.Floor(recipeSelectorScrollOffset) * GUN_RECIPE_VIEWER_COLUMNS;
		GunFabricationRecipe gunFabRecipe = Workbench.BlockEntity.GetSelectedGunRecipe();
		int numSlots = gunFabRecipe != null ? gunFabRecipe.InputIngredients.size() : 0;
		for (int j = 0; j < GUN_RECIPE_VIEWER_ROWS; j++)
		{
			for (int i = 0; i < GUN_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int relativeIndex = i + GUN_RECIPE_VIEWER_COLUMNS * j;
				if(GoToPartCraftingButtons != null)
				{
					GoToPartCraftingButtons[relativeIndex].active =
						IsActive
							&& Workbench.Def.partCrafting.isActive
							&& (firstIndex + relativeIndex < numSlots);
				}
				if(AutoFillCraftingButtons != null)
				{
					AutoFillCraftingButtons[relativeIndex].active = IsActive && (firstIndex + relativeIndex < numSlots);
				}
			}
		}
	}
	private void RefreshGunCraftingFilters()
	{
		// When we open the gun crafting tab, refresh our filters
		FilteredGunsList.clear();
		List<GunFabricationRecipe> allRecipes = Workbench.BlockEntity.GetAllGunRecipes();
		for(int i = 0; i < allRecipes.size(); i++)
		{
			FilteredGunsList.add(Pair.of(i, allRecipes.get(i)));
		}
	}
	private void SelectRecipe(int relativeIndex)
	{
		int filteredIndex = Maths.Floor(gunSelectorScrollOffset) * GUN_SELECTOR_COLUMNS + relativeIndex;
		if(filteredIndex < FilteredGunsList.size())
		{
			int absoluteIndex = FilteredGunsList.get(filteredIndex).getFirst();
			NetworkedButtonPress(WorkbenchMenuGunCrafting.BUTTON_SELECT_GUN_RECIPE_0 + absoluteIndex);
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid (filtered) Gun recipe " + filteredIndex + "/" + FilteredGunsList.size());
		}
	}
	private void AutoFillCraftingSlot(int relativeIndex)
	{
		NetworkedButtonPress(WorkbenchMenuGunCrafting.BUTTON_AUTO_FILL_INGREDIENT_0 + relativeIndex + Maths.Floor(recipeSelectorScrollOffset));
	}
	private void GoToPartCrafting(int relativeIndex)
	{
		int partIndex = Maths.Floor(recipeSelectorScrollOffset) + relativeIndex;
		//if(partIndex < CachedSlotInfo.size())
		//{
		//	//SelectTab(WorkbenchScreen.Tab.PART_CRAFTING);
		//	// TODO: Go to the matching part / set filters
		//}
		//else
		//{
		//	FlansMod.LOGGER.warn("Tried to select invalid Part recipe " + partIndex + "/" + CachedSlotInfo.size());
		//}
	}
}
