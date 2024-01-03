package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ContextCache;
import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkbenchScreenTabGunCrafting extends WorkbenchScreenTab<WorkbenchMenuGunCrafting>
{
	private static class GunCraftingSlotInfo
	{
		public final RecipePartDefinition Def;
		public final int Index;

		public final List<ItemStack> PotentialMatches;

		public GunCraftingSlotInfo(RecipePartDefinition part, int slot)
		{
			Def = part;
			Index = slot;
			PotentialMatches = new ArrayList<>();
		}

		public void FindMatches()
		{
			if(IsTieredMaterialIngredient())
			{
				TieredIngredientDefinition tiered = GetAsTieredDef();
				if(tiered != null)
				{
					PotentialMatches.clear();
					tiered.GenerateMatches(PotentialMatches);
				}
			}
			else
			{
				IngredientDefinition ingredient = GetAsAdditionalDef();
				if(ingredient != null)
				{
					PotentialMatches.clear();
					ingredient.GenerateMatches(PotentialMatches);
				}
			}
		}

		public boolean IsTieredMaterialIngredient()
		{
			return Index < Def.tieredIngredients.length;
		}

		public TieredIngredientDefinition GetAsTieredDef()
		{
			if(0 <= Index && Index < Def.tieredIngredients.length)
				return Def.tieredIngredients[Index];
			return null;
		}

		public IngredientDefinition GetAsAdditionalDef()
		{
			int offsetIndex = Index - Def.tieredIngredients.length;
			if(0 <= offsetIndex && offsetIndex < Def.additionalIngredients.length)
				return Def.additionalIngredients[offsetIndex];
			return null;
		}

		public ItemStack GetPotentialMatch(int index)
		{
			if(PotentialMatches.size() > 0)
				return PotentialMatches.get(index % PotentialMatches.size());
			else
				return new ItemStack(Items.BARRIER);
		}
	}
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


	private ArrayList<GunCraftingEntryDefinition> GunCraftingEntries = new ArrayList<>();
	private float gunSelectorScrollOffset = 0.0f;
	private static final int GUN_SELECTOR_X_ORIGIN = 5;
	private static final int GUN_SELECTOR_Y_ORIGIN = 16;
	private static final int GUN_SELECTOR_COLUMNS = 2;
	private static final int GUN_SELECTOR_ROWS = 5;
	private Button[] GunSelectionButtons;
	private int SelectedGunRecipe = -1;
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

	private final List<GunCraftingSlotInfo> CachedSlotInfo = new ArrayList<>();


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
			int numRows = Maths.Max(GunCraftingEntries.size() / GUN_SELECTOR_COLUMNS - GUN_SELECTOR_ROWS + 1, 0);
			gunSelectorScrollOffset -= scroll;
			gunSelectorScrollOffset = Maths.Clamp(gunSelectorScrollOffset, 0, numRows);
			UpdateActiveGunSelectionButtons();
			return true;
		}
		else if(InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN, 20 * GUN_RECIPE_VIEWER_COLUMNS + 6, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN, GUN_RECIPE_VIEWER_ROWS * 30))
		{
			int numRows = Maths.Max(CachedSlotInfo.size() / GUN_RECIPE_VIEWER_COLUMNS - GUN_RECIPE_VIEWER_ROWS + 1, 0);
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
		int firstRow = Maths.Floor(recipeSelectorScrollOffset);
		for (int j = 0; j < GUN_RECIPE_VIEWER_ROWS; j++)
		{
			for (int i = 0; i < GUN_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * GUN_RECIPE_VIEWER_COLUMNS + i;
				if(index < CachedSlotInfo.size())
				{
					if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 1 + 20 * i, 18, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 1 + 30 * j, 18))
					{
						if(CachedSlotInfo.get(index).IsTieredMaterialIngredient())
						{
							TieredIngredientDefinition tieredDef = CachedSlotInfo.get(index).GetAsTieredDef();
							if(tieredDef != null)
							{
								List<FormattedCharSequence> lines = new ArrayList<>();

								// Allowed tags
								if(!tieredDef.tag.isEmpty())
								{
									lines.add(Component.translatable("crafting.with_tag", tieredDef.tag).getVisualOrderText());
									// ResourceLocation resLoc = new ResourceLocation(tieredDef.tag);
									//, Component.translatable("tag." + resLoc.getNamespace() + "." + resLoc.getPath())
								}

								// Allowed materials
								lines.add(tieredDef.GetAllowedMaterialsComponent().getVisualOrderText());

								// Allowed tiers
								lines.add(tieredDef.GetAllowedTiersComponent().getVisualOrderText());
								graphics.renderTooltip(font, lines, xMouse, yMouse);
								return true;
							}
						}
						else
						{
							IngredientDefinition ingredientDef = CachedSlotInfo.get(index).GetAsAdditionalDef();
							if(ingredientDef != null)
							{
								if(ingredientDef.compareItemName)
									graphics.renderTooltip(font, Component.translatable(ingredientDef.itemName), xMouse, yMouse);
								else if(ingredientDef.compareItemTags)
								{
									List<FormattedCharSequence> lines = new ArrayList<>();
									for (String disallowed : ingredientDef.disallowedTags)
									{
										lines.add(Component.translatable(DISALLOWED_TAG, Component.translatable(disallowed)).getVisualOrderText());
									}
									for(String required : ingredientDef.requiredTags)
									{
										lines.add(Component.translatable(REQUIRED_TAG, Component.translatable(required)).getVisualOrderText());
									}
									graphics.renderTooltip(font, lines, xMouse, yMouse);
								}
								return true;
							}
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
				if (index < GunCraftingEntries.size())
				{
					if (InBox(xMouse, yMouse, xOrigin + GUN_SELECTOR_X_ORIGIN + 18 * col, 18, yOrigin + GUN_SELECTOR_Y_ORIGIN + 18 * row, 18))
					{
						GunCraftingEntryDefinition entry = GunCraftingEntries.get(index);
						List<FormattedCharSequence> lines = new ArrayList<>();
						for (int i = 0; i < entry.outputs.length; i++)
						{
							ResourceLocation resLoc = new ResourceLocation(entry.outputs[i].item);
							lines.add(Component.translatable("item." + resLoc.getNamespace() + "." + resLoc.getPath()).getVisualOrderText());
						}
						graphics.renderTooltip(font, lines, xMouse, yMouse);
						return true;
					}
				}
			}
		}

		if(SelectedGunRecipe != -1)
		{

			GunCraftingEntryDefinition recipe = GunCraftingEntries.get(SelectedGunRecipe);
			if(recipe != null && recipe.outputs.length > 0)
			{
				ItemStack stack = recipe.outputs[0].CreateStack();
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
		// Render the gun before the background so it ends up behind
		if (SelectedGunRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(SelectedGunRecipe);
			if (entry != null)
			{
				ItemStack stack = MinecraftHelpers.CreateStack(entry.outputs[0]);
				Render3DGun(graphics, xOrigin + imageWidth + 64, yOrigin + 64,
					Minecraft.getInstance().level.getGameTime() * 0.01f, -45f, stack);
			}
		}

		graphics.blit(GUN_FABRICATION_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, FAB_W, FAB_H);

		{
			int firstRow = Maths.Floor(recipeSelectorScrollOffset);
			int lastRow = firstRow + GUN_RECIPE_VIEWER_ROWS - 1;
			for (int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for (int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = y * GUN_RECIPE_VIEWER_COLUMNS + x;

					if (SelectedGunRecipe != -1 && index < CachedSlotInfo.size())
					{
						// Render this button
						GunCraftingSlotInfo slotInfo = CachedSlotInfo.get(index);
						int slotX = xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + x * 20;
						int slotY = yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + y * 30;
						graphics.blit(GUN_FABRICATION_BG, slotX, slotY, 172, 144, 20, 30, FAB_W, FAB_H);


						// Render the "Go to Part Crafting" button
						if (!Workbench.Def.partCrafting.isActive)
						{
							// Part Crafting Disabled
							graphics.blit(GUN_FABRICATION_BG, slotX + 10, slotY + 20, 182, 185, 9, 9, FAB_W, FAB_H);
						}

						TieredIngredientDefinition tieredDef = slotInfo.GetAsTieredDef();
						if (tieredDef != null)
						{
							int tier = tieredDef.GetLowestAllowedTier();
							PartBackgroundType bgType = PartBackgroundType.GetFromTag(tieredDef.tag);
							graphics.blit(GUN_FABRICATION_BG, slotX + 2, slotY + 2, bgType.texX, bgType.texY, 16, 16, FAB_W, FAB_H);
							graphics.blit(GUN_FABRICATION_BG, slotX, slotY + 9, (tier - 1) * 9, 247, 9, 9, FAB_W, FAB_H);
						} else
						{
							IngredientDefinition additionalDef = slotInfo.GetAsAdditionalDef();
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
		}

		{
			int firstRow = Maths.Floor(gunSelectorScrollOffset);
			for (int row = 0; row < GUN_SELECTOR_ROWS; row++)
			{
				int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
				int numEntriesInRow = Maths.Min(GUN_SELECTOR_COLUMNS, GunCraftingEntries.size() - firstIndexInRow);

				graphics.blit(GUN_FABRICATION_BG, xOrigin + GUN_SELECTOR_X_ORIGIN, yOrigin + GUN_SELECTOR_Y_ORIGIN + row * 18, 172, 0, 18 * numEntriesInRow, 18, FAB_W, FAB_H);
			}
		}

		// Render a scrollbar
		int numRows = GunCraftingEntries.size() / GUN_SELECTOR_COLUMNS - GUN_SELECTOR_ROWS + 1;
		RenderScrollbar(graphics, xOrigin + GUN_SELECTOR_X_ORIGIN + 18 * GUN_SELECTOR_COLUMNS, yOrigin + GUN_SELECTOR_Y_ORIGIN, 6, 90, gunSelectorScrollOffset, 0, numRows);

		if(SelectedGunRecipe != -1)
		{
			// Render a scrollbar
			int numRecipeRows = CachedSlotInfo.size() / GUN_RECIPE_VIEWER_COLUMNS - GUN_RECIPE_VIEWER_ROWS + 1;
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
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if(SelectedGunRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(SelectedGunRecipe);
			if(entry != null)
			{
				ResourceLocation resLoc = new ResourceLocation(entry.outputs[0].item);
				graphics.drawString(font, Component.translatable("item." + resLoc.getNamespace() + "." + resLoc.getPath()), GUN_STATS_X_ORIGIN, GUN_STATS_Y_ORIGIN, 0x404040, false);
			}
		}
		else
		{
			graphics.drawString(font, Component.translatable("crafting.select_a_recipe"),50, 21, 0x404040, false);
		}

		// Render all ItemStacks into the menu
		if(SelectedGunRecipe != -1)
		{
			int firstRow = Maths.Floor(recipeSelectorScrollOffset);
			for(int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for(int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = (firstRow + y) * GUN_RECIPE_VIEWER_COLUMNS + x;
					if(index < CachedSlotInfo.size())
					{
						// Render an example item that would fit into this slot
						GunCraftingSlotInfo slotInfo = CachedSlotInfo.get(index);
						ItemStack stack = slotInfo.GetPotentialMatch(Maths.Floor(ShowPotentialMatchTicker));
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

				if(index < GunCraftingEntries.size())
				{
					GunCraftingEntryDefinition entry = GunCraftingEntries.get(index);
					if(entry.outputs.length > 0)
					{
						ItemStack stack = entry.outputs[0].CreateStack();
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
					GunSelectionButtons[relativeIndex].active = IsActive && (firstIndex + relativeIndex < GunCraftingEntries.size());
				}
			}
		}
	}

	private void UpdateActiveRecipeButtons()
	{
		final int firstIndex = Maths.Floor(recipeSelectorScrollOffset) * GUN_RECIPE_VIEWER_COLUMNS;
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
							&& (firstIndex + relativeIndex < CachedSlotInfo.size());
				}
				if(AutoFillCraftingButtons != null)
				{
					AutoFillCraftingButtons[relativeIndex].active = IsActive && (firstIndex + relativeIndex < CachedSlotInfo.size());
				}
			}
		}
	}
	private void RefreshGunCraftingFilters()
	{
		// When we open the gun crafting tab, refresh our filters
		GunCraftingEntries.clear();
		for(GunCraftingPageDefinition pageDef : Workbench.Def.gunCrafting.pages)
		{
			GunCraftingEntries.addAll(Arrays.asList(pageDef.entries));
		}
	}
	private void SelectRecipe(int relativeIndex)
	{
		int recipeIndex = Maths.Floor(gunSelectorScrollOffset) * GUN_SELECTOR_COLUMNS + relativeIndex;
		if(recipeIndex < GunCraftingEntries.size())
		{
			SelectedGunRecipe = recipeIndex;
			Workbench.UpdateGunCraftingOutput();
			NetworkedButtonPress(WorkbenchMenuGunCrafting.BUTTON_SELECT_GUN_RECIPE_0 + recipeIndex);

			GunCraftingEntryDefinition recipe = GunCraftingEntries.get(SelectedGunRecipe);
			CachedSlotInfo.clear();
			for (RecipePartDefinition part : recipe.parts)
			{
				int count = part.tieredIngredients.length + part.additionalIngredients.length;
				for (int i = 0; i < count; i++)
				{
					GunCraftingSlotInfo slotInfo = new GunCraftingSlotInfo(part, i);
					slotInfo.FindMatches();
					CachedSlotInfo.add(slotInfo);
				}
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid Gun recipe " + recipeIndex + "/" + GunCraftingEntries.size());
		}
	}
	private void AutoFillCraftingSlot(int relativeIndex)
	{
		NetworkedButtonPress(WorkbenchMenuGunCrafting.BUTTON_AUTO_FILL_INGREDIENT_0 + relativeIndex + Maths.Floor(recipeSelectorScrollOffset));
	}
	private void GoToPartCrafting(int relativeIndex)
	{
		int partIndex = Maths.Floor(recipeSelectorScrollOffset) + relativeIndex;
		if(partIndex < CachedSlotInfo.size())
		{
			//SelectTab(WorkbenchScreen.Tab.PART_CRAFTING);
			// TODO: Go to the matching part / set filters
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid Part recipe " + partIndex + "/" + CachedSlotInfo.size());
		}
	}
}
