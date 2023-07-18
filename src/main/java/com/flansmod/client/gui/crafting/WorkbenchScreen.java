package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.WorkbenchMenu;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu>
{
	private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int TEXTURE_WIDTH = 512;
	private static final int TEXTURE_HEIGHT = 256;

	private enum Tab
	{
		MATERIALS,
		POWER,
		GUN_CRAFTING,
		ARMOUR_CRAFTING,
		MODIFICATION
	}



	private static final Component[] TAB_TITLES = new Component[]{
		Component.translatable("gui.workbench.tab_materials"),
		Component.translatable("gui.workbench.tab_power"),
		Component.translatable("gui.workbench.tab_gun_crafting"),
		Component.translatable("gui.workbench.tab_armour_crafting"),
		Component.translatable("gui.workbench.tab_modification")
	};

	private Tab SelectedTab = Tab.MATERIALS;
	private final WorkbenchMenu Workbench;
	private final WorkbenchDefinition Def;

	private final Tab[] AvailableTabs;




	public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component component)
	{
		super(menu, inventory, component);

		Workbench = menu;
		Def = menu.Def;

		ArrayList<Tab> tabs = new ArrayList<>();
		if(Def.gunCrafting.isActive)
			tabs.add(Tab.GUN_CRAFTING);
		if(Def.itemHolding.slots.length > 0)
			tabs.add(Tab.MATERIALS);
		if(Def.energy.maxFE > 0)
			tabs.add(Tab.POWER);
		if(Def.armourCrafting.isActive)
			tabs.add(Tab.ARMOUR_CRAFTING);
		if(Def.gunModifying.isActive)
			tabs.add(Tab.MODIFICATION);
		AvailableTabs = new Tab[tabs.size()];
		for(int i = 0; i < tabs.size(); i++)
			AvailableTabs[i] = tabs.get(i);

		if(AvailableTabs.length > 0)
			SelectTab(AvailableTabs[0]);

		imageWidth = 172;
		imageHeight = 200;

		titleLabelY = -1000;
		inventoryLabelX = 6;
		inventoryLabelY = 106;

		gunSelectorScrollOffset = 0.0f;
	}

	@Override
	protected void init()
	{
		super.init();
		int x = width / 2 - imageWidth / 2;
		int y = height / 2 - imageHeight / 2;

		if(AvailableTabs.length > 1)
		{
			for (Tab availableTab : AvailableTabs)
			{
				final int index = availableTab.ordinal();
				addWidget(Button.builder(TAB_TITLES[index],
						(t) ->
						{
							SelectTab(Tab.values()[index]);
						})
					.bounds(x + 2 + 28 * index, y, 28, 18)
					.build());
			}
		}

		// Create gun selection buttons
		if(Workbench.Def.gunCrafting.isActive)
		{
			InitGunCrafting(x, y);
		}
	}

	@Override
	protected void containerTick()
	{
		UpdateGunCrafting();
	}


	@Override
	public boolean mouseScrolled(double x, double y, double scroll)
	{
		if (!CanScroll())
			return false;

		gunSelectorScrollOffset -= scroll;
		gunSelectorScrollOffset = Maths.Clamp(gunSelectorScrollOffset, 0, GunCraftingEntries.size() / 2);

		for(int i = 0; i < GUN_SELECTOR_COLUMNS; i++)
		{
			for(int j = 0; j < GUN_SELECTOR_ROWS; j++)
			{
				int buttonIndex = j * GUN_SELECTOR_COLUMNS + i;
				int recipeIndex = Maths.Floor(gunSelectorScrollOffset) + buttonIndex;

				GunSelectionButtons[buttonIndex].active = recipeIndex < GunCraftingEntries.size();
			}
		}

		return true;
	}

	private boolean CanScroll() {
		return SelectedTab == Tab.GUN_CRAFTING;
	}
	private void SelectTab(WorkbenchScreen.Tab tab)
	{
		SelectedTab = tab;
		switch (SelectedTab)
		{
			case GUN_CRAFTING ->
			{
				RefreshGunCraftingFilters();

			}
		}
	}



	@Override
	protected void renderBg(PoseStack pose, float f, int x, int y)
	{
		pose.pushPose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);

		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		blit(pose, i, j, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// If this is a single-purpose workbench, block out the tabs and render a banner if we have one
		if(AvailableTabs.length <= 1)
		{
			blit(pose, i, j, getBlitOffset(), 0, 0, imageWidth, 22, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			//if(Def.banner != null)
			{

			}
		}
		// Else, we have a multi-purpose workbench and need to tabulate
		else
		{
			int numTabs = AvailableTabs.length;
			blit(pose, i, j, getBlitOffset(), 340,74 + 23 * (numTabs - 2), 172, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			//blit(pose, i + 3 + 28*SelectedTab.ordinal(), j + 16, getBlitOffset(), 263, 165, 26, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);


		}

		// Always draw the energy bar
		// Render a power bar
		RenderPowerBar(pose, i + 116, j + 105);

		switch(SelectedTab)
		{
			case MATERIALS ->
			{
				// Change background to be slots
				for(int row = 0; row < 4; row++)
				{
					int numSlotsInThisRow = Maths.Clamp(Workbench.MaterialContainer.getContainerSize() - (row * 9), 0, 9);

					blit(pose, i + 5, j + 22 + 18 * row, getBlitOffset(), 5, 119, 18 * numSlotsInThisRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				}
			}
			case POWER ->
			{
				// Render a power bar
				RenderPowerBar(pose, i + 61, j + 36);

				// Render a fuel slot
				if(Workbench.FuelContainer.getContainerSize() > 0)
				{
					blit(pose, i + 128, j + 48, getBlitOffset(), 295, 215, 18, 35, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					int litTime = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_TIME);
					int litDuration = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_DURATION);
					int px = (int) (11f * (float) litTime / (float) litDuration);
					blit(pose, i + 132, j + 51 + 11 - px, getBlitOffset(), 344, 213 + 11 - px, 9, px, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				}

				// Render a battery slot
				if(Workbench.BatteryContainer.getContainerSize() > 0)
				{
					blit(pose, i + 77, j + 65, getBlitOffset(), 244, 232, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				}
			}
			case GUN_CRAFTING ->
			{
				RenderGunCraftingBG(pose, i, j);
			}
			case MODIFICATION ->
			{
				if (Workbench.GunContainer.getContainerSize() >= 0)
				{
					// Render the slot BG for the gun slot
					blit(pose, i + 31, j + 48, getBlitOffset(), 295, 26, 22, 22, TEXTURE_WIDTH, TEXTURE_HEIGHT);

					// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
					if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
					{
						ItemStack gunStack = Workbench.GunContainer.getItem(0);
						for (WorkbenchMenu.ModSlot modSlot : WorkbenchMenu.ModSlot.values())
						{

							// If this item has this slot, blit the slot BG in
							if (flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex))
							{
								blit(pose,
									i + 5 + 26 * modSlot.x,
									j + 22 + 26 * modSlot.y,
									getBlitOffset(),
									269 + 26 * modSlot.x,
									26 * modSlot.y,
									22, 22,
									TEXTURE_WIDTH, TEXTURE_HEIGHT);
								//ItemStack attachmentItem = flanItem.GetAttachmentInSlot(gunStack, modSlot.attachType, modSlot.attachIndex);

							}
						}
					}
					//blit(pose, i + 3, j + 22, getBlitOffset(), 3, 22, 166, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);


				}
			}
		}

		pose.popPose();

	}

	protected void RenderPowerBar(PoseStack pose, int x, int y)
	{
		// Render a power bar
		blit(pose, x, y, getBlitOffset(), 228, 203, 51, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Render the partial fill texture
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;
		int px = (int)(51f * (float)fe / (float)feMax);
		blit(pose, x, y, getBlitOffset(), 433, 15, px, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	}

	@Override
	public void render(PoseStack pose, int x, int y, float f)
	{
		super.render(pose, x, y, f);
		renderTooltip(pose, x, y);
	}

	@Override
	protected void renderLabels(PoseStack pose, int x, int y)
	{
		super.renderLabels(pose, x, y);

		pose.pushPose();

		int i = imageWidth / 2;
		int j = imageHeight / 2;

		if(AvailableTabs.length <= 1) // && Banner == null
 		{
			font.draw(pose, "Workbench", 9, 5, 0x505050);
		}

		switch(SelectedTab)
		{
			case MODIFICATION ->
			{
				// Render floating rotating gun

			}
			case POWER ->
			{
				int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
				int feMax = Workbench.Def.energy.maxFE;

				String storedEnergyAmount = MinecraftHelpers.GetFEString(fe) + " / " + MinecraftHelpers.GetFEString(feMax);
				font.draw(pose, storedEnergyAmount, i - font.width(storedEnergyAmount) / 2f, 23, 0x505050);
			}
			case GUN_CRAFTING ->
			{
				RenderGunCraftingFG(pose, x, y);
			}
		}
		pose.popPose();


		//renderTooltip(pose, x, y);


	}


	// === GUN CRAFTING ===
	private ArrayList<GunCraftingEntryDefinition> GunCraftingEntries = new ArrayList<>();
	private float gunSelectorScrollOffset = 0.0f;
	private float recipeSelectorScrollOffset = 0.0f;
	private static final int GUN_SELECTOR_COLUMNS = 2;
	private static final int GUN_SELECTOR_ROWS = 4;
	private static final int GUN_RECIPE_VIEWER_COLUMNS = 4;
	private static final int GUN_RECIPE_VIEWER_ROWS = 2;
	private Button[] GunSelectionButtons;
	private Button[] PartCraftingButtons;
	private float ShowPotentialMatchTicker = 0.0f;
	private int SelectedGunRecipe = -1;
	private static final ResourceLocation GUN_FABRICATION_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_fabrication.png");
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

		public boolean IsTieredMaterialIngredient()
		{
			return Index < Def.tieredIngredients.length;
		}

		public TieredIngredientDefinition GetAsTieredDef()
		{
			if(Index < Def.tieredIngredients.length)
				return Def.tieredIngredients[Index];
			return null;
		}

		public IngredientDefinition GetAsAdditionalDef()
		{
			if(Index >= Def.tieredIngredients.length)
				return Def.additionalIngredients[Index - Def.tieredIngredients.length];
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
	private final List<GunCraftingSlotInfo> CachedSlotInfo = new ArrayList<>();

	private void InitGunCrafting(int x, int y)
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
					.bounds(x + 6 + i*18, y + 23 + j*18, 18, 18)
					.build();
				addWidget(GunSelectionButtons[index]);
			}
		}

		PartCraftingButtons = new Button[GUN_RECIPE_VIEWER_ROWS * GUN_RECIPE_VIEWER_COLUMNS];
		for(int j = 0; j < GUN_RECIPE_VIEWER_ROWS; j++)
		{
			for(int i = 0; i < GUN_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * GUN_RECIPE_VIEWER_COLUMNS + i;
				PartCraftingButtons[index] = Button.builder(Component.empty(),
					(t) ->
					{
						GoToPartCrafting(index);
					})
					.bounds(x + 57 + 20 * i, y + 75 + 20 * j, 9, 9)
					.build();
				addWidget(PartCraftingButtons[index]);
			}
		}
	}

	private void RefreshGunCraftingFilters()
	{
		// When we open the gun crafting tab, refresh our filters
		GunCraftingEntries.clear();
		for(GunCraftingPageDefinition pageDef : Def.gunCrafting.pages)
		{
			for(GunCraftingEntryDefinition entryDef : pageDef.entries)
			{
				GunCraftingEntries.add(entryDef);
			}
		}
	}
	private void SelectRecipe(int relativeIndex)
	{
		int recipeIndex = Maths.Floor(gunSelectorScrollOffset) + relativeIndex;
		if(recipeIndex < GunCraftingEntries.size())
		{
			SelectedGunRecipe = recipeIndex;

			GunCraftingEntryDefinition recipe = GunCraftingEntries.get(SelectedGunRecipe);
			CachedSlotInfo.clear();
			for (RecipePartDefinition part : recipe.parts)
			{
				int count = part.tieredIngredients.length + part.additionalIngredients.length;
				for (int i = 0; i < count; i++)
					CachedSlotInfo.add(new GunCraftingSlotInfo(part, count));
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid Gun recipe " + recipeIndex + "/" + GunCraftingEntries.size());
		}
	}
	private void GoToPartCrafting(int relativeIndex)
	{
		int partIndex = Maths.Floor(recipeSelectorScrollOffset) + relativeIndex;
		if(partIndex < CachedSlotInfo.size())
		{
			// TODO:
			SelectTab(Tab.MATERIALS);
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid Part recipe " + partIndex + "/" + CachedSlotInfo.size());
		}
	}
	private void RenderGunCraftingBG(PoseStack pose, int i, int j)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUN_FABRICATION_BG);
		blit(pose, i, j, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Render a scrollbar
		int numRows = GunCraftingEntries.size() / GUN_SELECTOR_COLUMNS;
		RenderScrollbar(pose, i + 40, j + 25, 6, 89, gunSelectorScrollOffset, 0, numRows);

		if(SelectedGunRecipe != -1)
		{
			int firstRow = Maths.Floor(recipeSelectorScrollOffset);
			int lastRow = firstRow + GUN_RECIPE_VIEWER_ROWS - 1;
			for(int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for(int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = y * GUN_RECIPE_VIEWER_COLUMNS + x;
					if(index < CachedSlotInfo.size())
					{
						// Render this button
						GunCraftingSlotInfo slotInfo = CachedSlotInfo.get(index);
						TieredIngredientDefinition tieredDef = slotInfo.GetAsTieredDef();
						if(tieredDef != null)
						{
							int tier = tieredDef.GetLowestAllowedTier();
							blit(pose, i + 48 + 20 * x, j + 75 + 20 * y, getBlitOffset(), 0, 247 + tier * 9, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}
						else
						{
							IngredientDefinition additionalDef = slotInfo.GetAsAdditionalDef();
							// Cover up the part crafting button
							blit(pose, i + 57 + 20 * x, j + 75 + 20 * y, getBlitOffset(), 229, 62, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
							// Render a faded item stack
							// (Defer to later)
						}
					}
					else
					{
						// Cover up the background for this button
						blit(pose, i + 48 + x * 20, j + 56 + y * 20, getBlitOffset(), 229, 72, 18, 28, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}

				}
			}
		}
		else
		{
			// Render over slots as disabled
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
						itemRenderer.renderGuiItem(stack, i + 49 + 20 * x, j + 57 + 20 * y);
					}
				}
			}
		}




		int firstRow = Maths.Floor(gunSelectorScrollOffset);
		for(int row = 0; row < GUN_SELECTOR_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
			int numEntriesInRow = Maths.Min(GUN_SELECTOR_COLUMNS, GunCraftingEntries.size() - firstIndexInRow);

			blit(pose, i + 6, j + 23 + row * 18, getBlitOffset(), 172, 0, 18 * numEntriesInRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}

		// Render scrollbar
		int scrollbarPxHeight = 18 * GUN_SELECTOR_ROWS;
		int scrollbarMinSize = 8 + 8;

		float rowRatioDisplayable = (float) GUN_SELECTOR_ROWS / (float)numRows;
		if(rowRatioDisplayable < 1f)
		{
			blit(pose, i + 6 + 18 * GUN_SELECTOR_COLUMNS, j + 23, getBlitOffset(), 214, 18, 6, 18 * GUN_SELECTOR_ROWS, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			float scrollbarSize = Maths.Max(scrollbarPxHeight * rowRatioDisplayable, scrollbarMinSize);
			float parametricScroll = gunSelectorScrollOffset / numRows;

			float minPx = Maths.Lerp(0, scrollbarPxHeight - scrollbarSize, parametricScroll);
			float maxPx = Maths.Lerp(scrollbarSize, scrollbarPxHeight, parametricScroll);

			blit(pose, i + 6 + 18 * GUN_SELECTOR_COLUMNS, j + 23 + Maths.Floor(minPx), getBlitOffset(), 208, 18, 6, Maths.Floor(scrollbarSize) - 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, i + 6 + 18 * GUN_SELECTOR_COLUMNS, j + 23 + Maths.Floor(maxPx) - 8, getBlitOffset(), 208, 82, 6, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		if(SelectedGunRecipe != -1)
		{
			// If the player has a gun in hand, we can do a comparison


			// Otherwise, just render the bars
			RenderStatComparisonBar(pose, i + 50, j + 36, 10, 10, 0, 20, 0);
			RenderStatComparisonBar(pose, i + 50, j + 46, 8, 8, 0, 20, 1);
			RenderStatComparisonBar(pose, i + 50, j + 56, 6, 6, 0, 20, 2);
			RenderStatComparisonBar(pose, i + 50, j + 66, 13, 13, 0, 20, 3);
		}

	}
	private void RenderCraftingSlot(PoseStack pose, int x, int y)
	{

	}
	private void RenderStatComparisonBar(PoseStack pose, int x, int y, float value, float compareTo, float minValue, float maxValue, int icon)
	{
		// Icon
		blit(pose, x + 2, y, getBlitOffset(), 220, 18 + icon * 9, 8, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Empty bar
		blit(pose, x + 12, y, getBlitOffset(), 241, 21, 22, 7, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		float valueParam = (value - minValue) / (maxValue - minValue);
		float compareToParam = (compareTo - minValue) / (maxValue - minValue);

		int valuePx = Maths.Floor(20f * Maths.Clamp(valueParam, 0f, 1f));
		int compareToPx = Maths.Floor(20f * Maths.Clamp(compareToParam, 0f, 1f));

		// Fill
		if(value > compareTo)
		{
			// Render yellow up to the compareTo value


			// Then green arrows from the compareTo to the new
		}
		else if(value < compareTo)
		{
			// Render yellow up to the new value

			// Then red arrows from the old value to the compareTo value

		}
		else
		{
			// Just render the bar in solid yellow
			blit(pose, x + 13, y + 1, getBlitOffset(), 242, 31, valuePx, 5, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}
	private void RenderGunCraftingFG(PoseStack pose, int x, int y)
	{
		if(SelectedGunRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(SelectedGunRecipe);
			if(entry != null)
			{
				font.draw(pose, entry.outputs[0].item,50, 26, 0x101010);

				ItemStack stack = MinecraftHelpers.CreateStack(entry.outputs[0]);
				//itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, pose, 50, 26, );
			}
		}
	}

	// Misc
	private void RenderScrollbar(PoseStack pose, int x, int y, int scrollbarPxWidth, int scrollbarPxHeight, float value, float min, float max)
	{
		int scrollbarMinSize = 8 + 8;

		float parametric = value / (max - min);
		float scrollbarPxSize = Maths.Max(scrollbarPxHeight / (max - min), scrollbarMinSize);

		float minPx = Maths.Lerp(0, scrollbarPxHeight - scrollbarPxSize, parametric);
		float maxPx = Maths.Lerp(scrollbarPxSize, scrollbarPxHeight, parametric);

		blit(pose, x, y + Maths.Floor(minPx), getBlitOffset(), 208, 18, scrollbarPxWidth, Maths.Floor(scrollbarPxSize) - 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		blit(pose, x, y + Maths.Floor(maxPx) - 8, getBlitOffset(), 208, 82, scrollbarPxWidth, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	private void UpdateGunCrafting()
	{
		ShowPotentialMatchTicker += 1.0f / 20.0f;

	}
}



























