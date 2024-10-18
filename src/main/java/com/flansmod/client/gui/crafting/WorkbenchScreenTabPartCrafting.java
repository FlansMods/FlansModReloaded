package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.ingredients.StackedIngredient;
import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import com.flansmod.common.crafting.menus.WorkbenchMenuPartCrafting;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.physics.common.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkbenchScreenTabPartCrafting extends WorkbenchScreenTab<WorkbenchMenuPartCrafting>
{
	public static final ResourceLocation PARTS_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/part_fabrication_large.png");
	public static final int PARTS_W = 512;
	public static final int PARTS_H = 256;

	private static final int PART_RECIPE_VIEWER_ROWS = 2;
	private static final int PART_RECIPE_VIEWER_COLUMNS = 8;

	private static final int PART_CRAFTING_NUM_INPUT_SLOTS_X = WorkbenchMenuPartCrafting.PART_CRAFTING_NUM_INPUT_SLOTS_X;
	private static final int PART_CRAFTING_NUM_INPUT_SLOTS_Y = WorkbenchMenuPartCrafting.PART_CRAFTING_NUM_INPUT_SLOTS_Y;
	private static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_X = WorkbenchMenuPartCrafting.PART_CRAFTING_NUM_OUTPUT_SLOTS_X;
	private static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_Y = WorkbenchMenuPartCrafting.PART_CRAFTING_NUM_OUTPUT_SLOTS_Y;
	private static final int PART_CRAFTING_INPUT_SLOTS_X =  WorkbenchMenuPartCrafting.PART_CRAFTING_INPUT_SLOTS_X;
	private static final int PART_CRAFTING_INPUT_SLOTS_Y =  WorkbenchMenuPartCrafting.PART_CRAFTING_INPUT_SLOTS_Y;
	private static final int PART_CRAFTING_OUTPUT_SLOTS_X = WorkbenchMenuPartCrafting.PART_CRAFTING_OUTPUT_SLOTS_X;
	private static final int PART_CRAFTING_OUTPUT_SLOTS_Y = WorkbenchMenuPartCrafting.PART_CRAFTING_OUTPUT_SLOTS_Y;

	private static final int PART_RECIPE_VIEWER_ORIGIN_X = 103;
	private static final int PART_RECIPE_VIEWER_ORIGIN_Y = 17;
	private static final int BLUEPRINT_ORIGIN_X = 98;
	private static final int BLUEPRINT_ORIGIN_Y = 64;

	private static final int QUEUE_VIEWER_NUM_ENTRIES_Y = 4;
	private static final int QUEUE_VIEWER_ORIGIN_X = 279;
	private static final int QUEUE_VIEWER_ORIGIN_Y = 17;


	protected float ShowPotentialMatchTicker = 0.0f;
	private float partSelectorScrollOffset = 0.0f;
	private int SelectedPartIndex = -1;
	private int CachedMaxPartsCraftable = 0;
	private Button[] PartSelectionButtons;
	private Button[] PartCraftingButtons;
	private Button[] PartQueueCancelButtons;

	public static final int CRAFT_BULK_AMOUNT = 8;

	private final List<ResourceKey<Item>> TagFilters = new ArrayList<>();
	private final List<Integer> TierFilters = new ArrayList<>();
	private final List<EMaterialType> MaterialFilters = new ArrayList<>();
	private boolean OnlyCraftableFilter = false;
	private final List<Pair<Integer, PartFabricationRecipe>> FilteredPartsList = new ArrayList<>();

	public WorkbenchScreenTabPartCrafting(@Nonnull WorkbenchMenuPartCrafting menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);

		imageWidth = 356;
		inventoryLabelX = 97;
	}
	@Override
	protected boolean IsTabPresent() { return Workbench.Def.partCrafting.isActive; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_part_crafting"); }
	@Override
	protected void InitTab()
	{
		PartSelectionButtons = new Button[PART_RECIPE_VIEWER_ROWS * PART_RECIPE_VIEWER_COLUMNS];
		for(int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
		{
			for(int i = 0; i < PART_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * PART_RECIPE_VIEWER_COLUMNS + i;
				PartSelectionButtons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							SelectPartRecipe(index);
						})
					.bounds(xOrigin + PART_RECIPE_VIEWER_ORIGIN_X + 18 * i, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y + 18 * j, 18, 18)
					.build();
				addWidget(PartSelectionButtons[index]);
			}
		}

		PartCraftingButtons = new Button[3];

		addWidget(PartCraftingButtons[0] = Button.builder(Component.empty(),
				(t) -> { CraftSelectedPart(1); })
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 105, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17, 17)
			.build());
		addWidget(PartCraftingButtons[1] = Button.builder(Component.empty(),
				(t) -> { CraftSelectedPart(CRAFT_BULK_AMOUNT); })
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 105 + 18, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17, 17)
			.build());
		addWidget(PartCraftingButtons[2] = Button.builder(Component.empty(),
				(t) -> { CraftSelectedPart(-1); })
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 105 + 36, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17, 17)
			.build());

		PartQueueCancelButtons = new Button[QUEUE_VIEWER_NUM_ENTRIES_Y];
		for(int i = 0; i < QUEUE_VIEWER_NUM_ENTRIES_Y; i++)
		{
			final int index = i;
			addWidget(PartQueueCancelButtons[i] = Button.builder(Component.empty(),
					(t) -> { CancelPartCrafting(index); })
				.bounds(xOrigin + QUEUE_VIEWER_ORIGIN_X + 56, yOrigin + QUEUE_VIEWER_ORIGIN_Y + 2 + i * 18, 9, 9)
				.build());
		}

		// Filter buttons
		addWidget(Button.builder(Component.empty(),
				(t) ->
				{
					OnlyCraftableFilter = !OnlyCraftableFilter;
					UpdatePartCraftingFilters();
				})
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 2, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6, 6)
			.build());
		addWidget(Button.builder(Component.empty(),
				(t) ->
				{
					if(TierFilters.contains(1))
						TierFilters.remove(Integer.valueOf(1));
					else TierFilters.add(1);
					UpdatePartCraftingFilters();
				})
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 62, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6, 6)
			.build());
		addWidget(Button.builder(Component.empty(),
				(t) ->
				{
					if(TierFilters.contains(2))
						TierFilters.remove(Integer.valueOf(2));
					else TierFilters.add(2);
					UpdatePartCraftingFilters();
				})
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 82, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6, 6)
			.build());
		addWidget(Button.builder(Component.empty(),
				(t) ->
				{
					if(TierFilters.contains(3))
						TierFilters.remove(Integer.valueOf(3));
					else TierFilters.add(3);
					UpdatePartCraftingFilters();
				})
			.bounds(xOrigin + BLUEPRINT_ORIGIN_X + 102, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6, 6)
			.build());


		UpdatePartCraftingFilters();
		UpdateActivePartSelectionButtons();
	}
	@Override
	protected void OnTabSelected(boolean selected)
	{
		UpdatePartCraftingFilters();
	}
	@Override
	protected void UpdateTab(boolean selected)
	{
		ShowPotentialMatchTicker += 1.0f / 20.0f;
	}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll)
	{
		if(InBox(xMouse, yMouse, xOrigin + PART_RECIPE_VIEWER_ORIGIN_X, 18 * PART_RECIPE_VIEWER_COLUMNS + 6, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y, 18 * PART_RECIPE_VIEWER_ROWS))
		{
			int numRows = Maths.max(FilteredPartsList.size() / PART_RECIPE_VIEWER_COLUMNS - PART_RECIPE_VIEWER_ROWS + 1, 0);
			partSelectorScrollOffset -= scroll;
			partSelectorScrollOffset = Maths.clamp(partSelectorScrollOffset, 0, numRows);
			UpdateActivePartSelectionButtons();
			return true;
		}
		return false;
	}
	@Override
	protected int GetWidth() { return 356; }
	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		Level level = Minecraft.getInstance().level;
		if(level == null)
			return false;

		int craftingSelection = SelectedPartIndex;
		if(craftingSelection != -1)
		{
			List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
			PartFabricationRecipe recipe = recipes.get(craftingSelection);
			if (recipe != null)
			{
				// Render info for this recipe
				int[] matching = Workbench.Workbench.GetQuantityOfEachIngredientForRecipe(level, craftingSelection);
				int[] required = Workbench.Workbench.GetRequiredOfEachIngredientForRecipe(level, craftingSelection);

				for (int i = 0; i < recipe.getIngredients().size(); i++)
				{
					if (InBox(xMouse, yMouse,
						xOrigin + BLUEPRINT_ORIGIN_X + 6 + 25 * i, 25,
						yOrigin + BLUEPRINT_ORIGIN_Y + 18, 23))
					{
						graphics.renderTooltip(font, recipe.GenerateTooltip(i, required[i], matching[i]), Optional.empty(), xMouse, yMouse);
						return true;
					}
				}

				if (InBox(xMouse, yMouse,
					xOrigin + BLUEPRINT_ORIGIN_X + 105, 17,
					yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
				{
					graphics.renderTooltip(font, Component.translatable("workbench.craft.1"), xMouse, yMouse);
					return true;
				}
				if (InBox(xMouse, yMouse,
					xOrigin + BLUEPRINT_ORIGIN_X + 105 + 18, 17,
					yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
				{
					graphics.renderTooltip(font, Component.translatable("workbench.craft.n", CRAFT_BULK_AMOUNT), xMouse, yMouse);
					return true;
				}
				if (InBox(xMouse, yMouse,
					xOrigin + BLUEPRINT_ORIGIN_X + 105 + 36, 17,
					yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
				{
					int maxCanCraft = Workbench.Workbench.GetMaxPartsCraftableFromInput(level, SelectedPartIndex);
					graphics.renderTooltip(font, Component.translatable("workbench.craft.all", maxCanCraft), xMouse, yMouse);
					return true;
				}
			}
		}

		// Render the crafting queue
		for(int i = 0; i < QUEUE_VIEWER_NUM_ENTRIES_Y; i++)
		{
			int queueSelection = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_SELECTION_0 + i);
			if (queueSelection != -1)
			{
				List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
				PartFabricationRecipe recipe = recipes.get(queueSelection);
				if (recipe != null)
				{
					if (InBox(xMouse, yMouse,
						xOrigin + QUEUE_VIEWER_ORIGIN_X, 67,
						yOrigin + QUEUE_VIEWER_ORIGIN_Y + 18 * i, 18))
					{
						int craftingCount = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_QUEUE_COUNT_0 + i);
						if (craftingCount != 0)
						{
							int craftingTime = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_TIME);
							int craftingDuration = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_DURATION);

							graphics.renderTooltip(font, Component.translatable("crafting.parts.num_in_progress.named", craftingCount, recipe.getResultItem(RegistryAccess.EMPTY).getHoverName()), xMouse, yMouse);
							return true;
						}
					}
				}
			}
		}

		// Recipe viewer tooltips
		for(int i = 0; i < PART_RECIPE_VIEWER_COLUMNS; i++)
		{
			for(int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
			{
				if(InBox(xMouse, yMouse, xOrigin + PART_RECIPE_VIEWER_ORIGIN_X + i * 18, 18, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y + 18 * j, 18))
				{
					int index = (Maths.floor(partSelectorScrollOffset) + j) * PART_RECIPE_VIEWER_COLUMNS + i;
					if(index < FilteredPartsList.size())
					{
						graphics.renderTooltip(font, FilteredPartsList.get(index).getSecond().getResultItem(RegistryAccess.EMPTY).getHoverName(), xMouse, yMouse);
						return true;
					}
				}
			}
		}

		// Filter tooltips
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 2, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
		{
			graphics.renderTooltip(font,  Component.translatable("crafting.parts.filter.only_craftable"), xMouse, yMouse);
			return true;
		}
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 62, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
		{
			graphics.renderTooltip(font, Component.translatable("crafting.parts.filter.match_tier", 1), xMouse, yMouse);
			return true;
		}
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 82, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
		{
			graphics.renderTooltip(font, Component.translatable("crafting.parts.filter.match_tier", 2), xMouse, yMouse);
			return true;
		}
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 102, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
		{
			graphics.renderTooltip(font, Component.translatable("crafting.parts.filter.match_tier", 3), xMouse, yMouse);
			return true;
		}

		return false;
	}

	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		graphics.blit(PARTS_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, PARTS_W, PARTS_H);

		Level level = Minecraft.getInstance().level;
		if(level == null)
			return;

		// Input and Output Slot Backgrounds
		for(int j = 0; j < PART_CRAFTING_NUM_INPUT_SLOTS_Y; j++)
		{
			int numSlotsOnThisRow = Maths.min(Workbench.PartCraftingInputContainer.getContainerSize() - j * PART_CRAFTING_NUM_INPUT_SLOTS_X, PART_CRAFTING_NUM_INPUT_SLOTS_X);
			if (numSlotsOnThisRow > 0)
			{
				graphics.blit(PARTS_BG, xOrigin + PART_CRAFTING_INPUT_SLOTS_X, yOrigin + PART_CRAFTING_INPUT_SLOTS_Y + 18 * j, 97, 136, 18 * numSlotsOnThisRow, 18, PARTS_W, PARTS_H);
			}
		}
		for(int j = 0; j < PART_CRAFTING_NUM_OUTPUT_SLOTS_Y; j++)
		{
			int numOutputSlotsOnThisRow = Maths.min(Workbench.PartCraftingOutputContainer.getContainerSize() - j * PART_CRAFTING_NUM_OUTPUT_SLOTS_X, PART_CRAFTING_NUM_OUTPUT_SLOTS_X);
			if(numOutputSlotsOnThisRow > 0)
			{
				graphics.blit(PARTS_BG, xOrigin + PART_CRAFTING_OUTPUT_SLOTS_X, yOrigin + PART_CRAFTING_OUTPUT_SLOTS_Y + 18 * j, 97, 136, 18 * numOutputSlotsOnThisRow, 18, PARTS_W, PARTS_H);
			}
		}

		int maxProduce = 0;
		int craftingSelection = SelectedPartIndex;
		if(craftingSelection != -1)
		{
			List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
			PartFabricationRecipe recipe = recipes.get(craftingSelection);
			if(recipe != null)
			{
				int[] matching = Workbench.Workbench.GetQuantityOfEachIngredientForRecipe(level, craftingSelection);
				int[] required = Workbench.Workbench.GetRequiredOfEachIngredientForRecipe(level, craftingSelection);

				// Render info for this recipe
				for (int i = 0; i < recipe.getIngredients().size(); i++)
				{
					Ingredient ingredient = recipe.getIngredients().get(i);
					maxProduce = matching[i] / required[i];

					// Ingredient outline
					boolean isLast = i == recipe.getIngredients().size() - 1;
					graphics.blit(PARTS_BG,
						xOrigin + BLUEPRINT_ORIGIN_X + 6 + 25*i,
						yOrigin + BLUEPRINT_ORIGIN_Y + 23,
						275, 233,
						isLast ? 18 : 25, 18, // Include the blueprint "+" if not last
						PARTS_W, PARTS_H);
					// Count marker
					graphics.blit(PARTS_BG,
						xOrigin + BLUEPRINT_ORIGIN_X + 6 + 25*i,
						yOrigin + BLUEPRINT_ORIGIN_Y + 18,
						275, 228,
						24, 4,
						PARTS_W, PARTS_H);
					graphics.blit(PARTS_BG,
						xOrigin + BLUEPRINT_ORIGIN_X + 6 + 25*i,
						yOrigin + BLUEPRINT_ORIGIN_Y + 18,
						275, 222,
						1 + 3 * Maths.clamp(maxProduce, 0, 5), 4,
						PARTS_W, PARTS_H);
					if(maxProduce > 5)
						graphics.blit(PARTS_BG,
							xOrigin + BLUEPRINT_ORIGIN_X + 23 + 25*i,
							yOrigin + BLUEPRINT_ORIGIN_Y + 18,
							292, 223,
							3, 3,
							PARTS_W, PARTS_H);
					if(maxProduce > 10)
						graphics.blit(PARTS_BG,
							xOrigin + BLUEPRINT_ORIGIN_X + 27 + 25*i,
							yOrigin + BLUEPRINT_ORIGIN_Y + 18,
							292, 223,
							3, 3,
							PARTS_W, PARTS_H);
				}


			}
		}


		// Render the crafting queue
		for(int i = 0; i < QUEUE_VIEWER_NUM_ENTRIES_Y; i++)
		{
			int queueSelection = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_SELECTION_0 + i);
			if (queueSelection != -1)
			{
				List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
				PartFabricationRecipe recipe = recipes.get(queueSelection);
				if (recipe != null)
				{
					int craftingCount = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_QUEUE_COUNT_0 + i);
					if (craftingCount != 0)
					{
						// Blit a background into the queue panel
						graphics.blit(PARTS_BG,
							xOrigin + QUEUE_VIEWER_ORIGIN_X,
							yOrigin + QUEUE_VIEWER_ORIGIN_Y + 18 * i,
							357, 1,
							67, 18,
							PARTS_W, PARTS_H);
						// And fill in the crafting progress bar
						if(i == 0)
						{
							int craftingTime = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_TIME);
							int craftingDuration = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_DURATION);

							graphics.blit(PARTS_BG,
								xOrigin + QUEUE_VIEWER_ORIGIN_X + 18,
								yOrigin + QUEUE_VIEWER_ORIGIN_Y + 13,
								375, 21,
								Maths.ceil(48f * (1.0f - ((float) craftingTime / (float) craftingDuration))), 4,
								PARTS_W, PARTS_H);
						}

						if(craftingCount == -1)
						{
							graphics.blit(PARTS_BG,
								xOrigin + QUEUE_VIEWER_ORIGIN_X + 29,
								yOrigin + QUEUE_VIEWER_ORIGIN_Y + 3 + 18 * i,
								450, 3,
								13, 9,
								PARTS_W, PARTS_H);
						}

						// If hovering the cancel button
						if (InBox(xMouse, yMouse, xOrigin + QUEUE_VIEWER_ORIGIN_X + 56, 9, yOrigin + QUEUE_VIEWER_ORIGIN_Y + 2, 9))
						{
							graphics.blit(PARTS_BG,
								xOrigin + QUEUE_VIEWER_ORIGIN_X + 56,
								yOrigin + QUEUE_VIEWER_ORIGIN_Y + 2,
								426, 3,
								9, 9,
								PARTS_W, PARTS_H);
						}
					}
				}
			}
		}

		// Render recipe craft button overlays
		if(maxProduce < 1)
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 105, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 357, 218, 17, 17, PARTS_W, PARTS_H);
		else if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 105, 17, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 105, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 357, 236, 17, 17, PARTS_W, PARTS_H);

		if(maxProduce < 8)
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 123, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 375, 218, 17, 17, PARTS_W, PARTS_H);
		else if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 123, 17, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 123, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 375, 236, 17, 17, PARTS_W, PARTS_H);

		// Infinite Button
		//graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 141, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 393, 218, 17, 17, PARTS_W, PARTS_H);
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 141, 17, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 17))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 141, yOrigin + BLUEPRINT_ORIGIN_Y + 25, 411, 236, 17, 17, PARTS_W, PARTS_H);


		// Render filter buttons
		if(OnlyCraftableFilter)
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 2, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 29, 6, 6, PARTS_W, PARTS_H);
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 2, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 2, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 36, 6, 6, PARTS_W, PARTS_H);

		if(TierFilters.contains(1))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 62, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 29, 6, 6, PARTS_W, PARTS_H);
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 62, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 62, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 36, 6, 6, PARTS_W, PARTS_H);
		if(TierFilters.contains(2))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 82, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 29, 6, 6, PARTS_W, PARTS_H);
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 82, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 82, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 36, 6, 6, PARTS_W, PARTS_H);
		if(TierFilters.contains(3))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 102, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 29, 6, 6, PARTS_W, PARTS_H);
		if(InBox(xMouse, yMouse, xOrigin + BLUEPRINT_ORIGIN_X + 102, 6, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 6))
			graphics.blit(PARTS_BG, xOrigin + BLUEPRINT_ORIGIN_X + 102, yOrigin + BLUEPRINT_ORIGIN_Y - 9, 359, 36, 6, 6, PARTS_W, PARTS_H);



		// Recipe selector scroller
		for(int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
		{
			int rowIndex = Maths.floor(partSelectorScrollOffset) + j;
			int numSlotsOnThisRow = Maths.min(FilteredPartsList.size() - j * PART_RECIPE_VIEWER_COLUMNS, PART_RECIPE_VIEWER_COLUMNS);
			if(numSlotsOnThisRow > 0)
			{
				graphics.blit(PARTS_BG, xOrigin + PART_RECIPE_VIEWER_ORIGIN_X, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y + 18 * j, 97, 217, 18 * numSlotsOnThisRow, 18, PARTS_W, PARTS_H);

				for(int i = 0; i < numSlotsOnThisRow; i++)
				{
					Pair<Integer, PartFabricationRecipe> recipe = FilteredPartsList.get(j * PART_RECIPE_VIEWER_COLUMNS + i);
					int maxCraftable = Workbench.Workbench.GetMaxPartsCraftableFromInput(level, recipe.getFirst());
					if(maxCraftable <= 0)
						graphics.blit(PARTS_BG, xOrigin + PART_RECIPE_VIEWER_ORIGIN_X + 18 * i, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y + 18 * j, 359, 43, 18, 18, PARTS_W, PARTS_H);
				}


			}
		}

		int numRows = FilteredPartsList.size() / PART_RECIPE_VIEWER_COLUMNS - PART_RECIPE_VIEWER_ROWS + 1;
		RenderScrollbar(graphics, xOrigin + PART_RECIPE_VIEWER_ORIGIN_X + PART_RECIPE_VIEWER_COLUMNS * 18, yOrigin + PART_RECIPE_VIEWER_ORIGIN_Y, 6, 18 * PART_RECIPE_VIEWER_ROWS, partSelectorScrollOffset, 0, numRows);
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		graphics.drawString(font, Component.translatable("workbench.input"), PART_CRAFTING_INPUT_SLOTS_X, PART_CRAFTING_INPUT_SLOTS_Y - 11, 0x404040, false);
		graphics.drawString(font, Component.translatable("workbench.queue"), PART_CRAFTING_OUTPUT_SLOTS_X, 5, 0x404040, false);
		graphics.drawString(font, Component.translatable("workbench.output"), PART_CRAFTING_OUTPUT_SLOTS_X, PART_CRAFTING_OUTPUT_SLOTS_Y - 11, 0x404040, false);

		Level level = Minecraft.getInstance().level;
		if(level == null)
			return;

		// Render info about the selected part
		if(SelectedPartIndex >= 0 && SelectedPartIndex < Workbench.Workbench.GetAllPartRecipes(level).size())
		{
			ItemStack selectedPart = Workbench.Workbench.GetAllPartRecipes(level).get(SelectedPartIndex).getResultItem(RegistryAccess.EMPTY);
			List<FormattedCharSequence> wordWrap = font.split(selectedPart.getHoverName(), 151);
			graphics.drawString(font, wordWrap.get(0), 104, 70, 0xffffff, false);
		}

		int craftingSelection = SelectedPartIndex;
		//int craftingSelection = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_CRAFT_SELECTION);
		if(craftingSelection != -1)
		{
			List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
			PartFabricationRecipe recipe = recipes.get(craftingSelection);
			if (recipe != null)
			{
				// Render info for this recipe
				for (int i = 0; i < recipe.getIngredients().size(); i++)
				{
					Ingredient ingredient = recipe.getIngredients().get(i);
					ItemStack[] possibleInputs = ingredient.getItems();
					if (possibleInputs.length > 0)
					{
						int pick = Maths.modulo(Maths.floor(ShowPotentialMatchTicker), possibleInputs.length);
						ItemStack possibleInput = ingredient.getItems()[pick];

						if(!possibleInput.isEmpty())
						{
							int slotX = BLUEPRINT_ORIGIN_X + 7 + 25 * i;
							int slotY = BLUEPRINT_ORIGIN_Y + 24;
							graphics.renderItem(possibleInput, slotX, slotY);

							if(ingredient instanceof StackedIngredient stacked)
							{
								int countPerItem = stacked.Count(possibleInput);
								int countTarget = stacked.Count;

								if(countPerItem != 0)
								{
									int multi = countTarget / countPerItem;
									int remainder = countTarget % countPerItem;
									if (multi <= 0)
									{
										graphics.renderItemDecorations(font, possibleInput, slotX, slotY, "<1");
									}
									else if(remainder == 0)
									{
										graphics.renderItemDecorations(font, possibleInput, slotX, slotY, ""+multi);
									}
									else
									{
										graphics.renderItemDecorations(font, possibleInput, slotX, slotY, "<"+(multi+1));
									}
								}

							}
						}


						//RenderGUIItem(graphics,
						//	BLUEPRINT_ORIGIN_X + 7 + 25 * i,
						//	BLUEPRINT_ORIGIN_Y + 24,
						//	possibleInput,
						//	false);
					}
				}
			}
		}

		// Render the crafing queue
		for(int i = 0; i < QUEUE_VIEWER_NUM_ENTRIES_Y; i++)
		{
			int queueSelection = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_SELECTION_0 + i);
			if (queueSelection != -1)
			{
				List<PartFabricationRecipe> recipes = Workbench.Workbench.GetAllPartRecipes(level);
				PartFabricationRecipe recipe = recipes.get(queueSelection);
				if (recipe != null)
				{
					// Render icons into queue
					int craftingCount = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_QUEUE_COUNT_0 + i);
					if (craftingCount != 0)
					{
						int stackCount = craftingCount > 0 ? craftingCount : 1;
						ItemStack queueStack = recipe.getResultItem(RegistryAccess.EMPTY).copyWithCount(recipe.getResultItem(RegistryAccess.EMPTY).getCount() * stackCount);
						RenderGUIItem(graphics,
							QUEUE_VIEWER_ORIGIN_X + 1,
							QUEUE_VIEWER_ORIGIN_Y + 1 + 18 * i,
							queueStack,
							true);
					}
				}
			}
		}

		// Render part icons into the selector scrollbar
		int firstRow = Maths.floor(partSelectorScrollOffset);
		for (int row = 0; row < PART_RECIPE_VIEWER_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * PART_RECIPE_VIEWER_COLUMNS;
			for(int col = 0; col < PART_RECIPE_VIEWER_COLUMNS; col++)
			{
				int index = firstIndexInRow + col;

				if(0 <= index && index < FilteredPartsList.size())
				{
					ItemStack entry = FilteredPartsList.get(index).getSecond().getResultItem(RegistryAccess.EMPTY);
					RenderGUIItem(graphics,
						PART_RECIPE_VIEWER_ORIGIN_X + 1 + 18 * col,
						PART_RECIPE_VIEWER_ORIGIN_Y + 1 + 18 * row,
						entry,
						true);
				}
			}
		}
	}

	// ---------------------------------------------------------------------------------
	//   Filter settings
	// ---------------------------------------------------------------------------------
	private void UpdatePartCraftingFilters()
	{
		Level level = Minecraft.getInstance().level;
		if(level == null)
			return;

		FilteredPartsList.clear();
		List<PartFabricationRecipe> allRecipes = Workbench.Workbench.GetAllPartRecipes(level);

		for(int i = 0; i < allRecipes.size(); i++)
		{
			PartFabricationRecipe recipe = allRecipes.get(i);
			if(recipe.getResultItem(RegistryAccess.EMPTY).getItem() instanceof PartItem partItem)
			{
				MaterialDefinition material = partItem.Def().GetMaterial();

				// Apply filters
				if(TierFilters.size() > 0)
				{
					if(!TierFilters.contains(material.craftingTier))
						continue;
				}

				//if(MaterialFilters.size() > 0)
				//	if(!MaterialFilters.contains(PartDefinition.GetPartMaterial(stack)))
				//		continue;
			}

			if(OnlyCraftableFilter)
			{
				int numCraftable = Workbench.Workbench.GetMaxPartsCraftableFromInput(level, i);
				if(numCraftable <= 0)
					continue;
			}

			FilteredPartsList.add(Pair.of(i, allRecipes.get(i)));
		}

		int maxScroll = Maths.max(FilteredPartsList.size() / PART_RECIPE_VIEWER_COLUMNS - 2, 0);
		if(partSelectorScrollOffset > maxScroll)
			partSelectorScrollOffset = maxScroll;
		UpdateActivePartSelectionButtons();
	}

	private void UpdateActivePartSelectionButtons()
	{
		Level level = Minecraft.getInstance().level;
		if(level == null)
			return;

		if(PartSelectionButtons != null)
		{
			for (int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
			{
				for (int i = 0; i < PART_RECIPE_VIEWER_COLUMNS; i++)
				{
					final int firstIndex = Maths.floor(partSelectorScrollOffset) * PART_RECIPE_VIEWER_COLUMNS;
					final int relativeIndex = i + PART_RECIPE_VIEWER_COLUMNS * j;
					PartSelectionButtons[relativeIndex].active = IsActive && (firstIndex + relativeIndex < FilteredPartsList.size());
				}
			}
		}
		if(PartCraftingButtons != null)
		{
			for(int i = 0; i < 3; i++)
			{
				if(i == 2)
				{
					PartCraftingButtons[i].active = true;
				}
				else
				{
					int numButtonRepresents = i == 1 ? 5 : 1;
					int numCanCraft = Workbench.Workbench.GetMaxPartsCraftableFromInput(level, SelectedPartIndex);
					PartCraftingButtons[i].active = IsActive && SelectedPartIndex != -1 && numCanCraft >= numButtonRepresents;
				}
			}
		}
		if(PartQueueCancelButtons != null)
		{
			for(int i = 0; i < QUEUE_VIEWER_NUM_ENTRIES_Y; i++)
			{
				int queueSize = Workbench.WorkbenchData.get(AbstractWorkbench.DATA_CRAFT_QUEUE_COUNT_0 + i);
				PartQueueCancelButtons[i].active = IsActive && queueSize != 0;
			}
		}
	}

	private void SelectPartRecipe(int relativeIndex)
	{
		Level level = Minecraft.getInstance().level;
		if(level == null)
			return;

		int filteredIndex = Maths.floor(partSelectorScrollOffset) * PART_RECIPE_VIEWER_COLUMNS + relativeIndex;
		if(filteredIndex >= FilteredPartsList.size())
			filteredIndex = -1;

		SelectedPartIndex = FilteredPartsList.get(filteredIndex).getFirst();
		NetworkedButtonPress(WorkbenchMenuPartCrafting.BUTTON_SELECT_PART_RECIPE_0 + SelectedPartIndex);
		CachedMaxPartsCraftable = Workbench.Workbench.GetMaxPartsCraftableFromInput(level, SelectedPartIndex);
		UpdateActivePartSelectionButtons();
	}

	private void CraftSelectedPart(int count)
	{
		if(count <= 0)
			NetworkedButtonPress(WorkbenchMenuPartCrafting.BUTTON_CRAFT_ALL);
		else
			NetworkedButtonPress(WorkbenchMenuPartCrafting.BUTTON_CRAFT_1 + (count - 1));
	}

	private void CancelPartCrafting(int queueIndex)
	{
		NetworkedButtonPress(WorkbenchMenuPartCrafting.BUTTON_QUEUE_CANCEL_0 + queueIndex);
	}
}
