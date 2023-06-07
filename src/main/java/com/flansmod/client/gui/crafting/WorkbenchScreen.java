package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.WorkbenchMenu;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.GunCraftingEntryDefinition;
import com.flansmod.common.types.crafting.elements.GunCraftingPageDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

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

	// === GUN CRAFTING ===
	private ArrayList<GunCraftingEntryDefinition> GunCraftingEntries = new ArrayList<>();
	private float scrollOffs;
	private static final int GUN_CRAFTING_COLUMNS = 2;
	private static final int GUN_CRAFTING_ROWS = 4;
	private Button[] buttons;
	private int selectedRecipe = -1;

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

		scrollOffs = 0.0f;
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
			buttons = new Button[GUN_CRAFTING_ROWS * GUN_CRAFTING_COLUMNS];
			for(int j = 0; j < GUN_CRAFTING_ROWS; j++)
			{
				for(int i = 0; i < GUN_CRAFTING_COLUMNS; i++)
				{
					final int index = j * GUN_CRAFTING_COLUMNS + i;
					buttons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							SelectRecipe(index);
						})
						.bounds(x + 6 + i*18, y + 23 + j*18, 18, 18)
						.build();
					addWidget(buttons[index]);
				}
			}
		}
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scroll)
	{
		if (!CanScroll())
			return false;

		scrollOffs -= scroll;
		scrollOffs = Maths.Clamp(scrollOffs, 0, GunCraftingEntries.size() / 2);

		for(int i = 0; i < GUN_CRAFTING_COLUMNS; i++)
		{
			for(int j = 0; j < GUN_CRAFTING_ROWS; j++)
			{
				int buttonIndex = j * GUN_CRAFTING_COLUMNS + i;
				int recipeIndex = Maths.Floor(scrollOffs) + buttonIndex;

				buttons[buttonIndex].active = recipeIndex < GunCraftingEntries.size();
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
								blit(pose, i + 5 + 26 * modSlot.x, j + 22 + 26 * modSlot.y, getBlitOffset(), 269 + 26 * modSlot.x, 26 * modSlot.y, 22, 22, TEXTURE_WIDTH, TEXTURE_HEIGHT);
								//ItemStack attachmentItem = flanItem.GetAttachmentInSlot(gunStack, modSlot.attachType, modSlot.attachIndex);

							}
						}
					}
					blit(pose, i + 3, j + 22, getBlitOffset(), 3, 22, 166, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);


				}
			}
		}

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

	protected void renderLabels(PoseStack pose, int x, int y)
	{
		super.renderLabels(pose, x, y);

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


	}


	// === GUN CRAFTING ===
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
		int recipeIndex = Maths.Floor(scrollOffs) + relativeIndex;
		if(recipeIndex < GunCraftingEntries.size())
			selectedRecipe = recipeIndex;
	}
	private void RenderGunCraftingBG(PoseStack pose, int i, int j)
	{
		// Render a background box
		blit(pose, i + 5, j + 22, getBlitOffset(), 172, 119, 162, 74, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		int firstRow = Maths.Floor(scrollOffs);
		for(int row = 0; row < GUN_CRAFTING_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * GUN_CRAFTING_COLUMNS;
			int numEntriesInRow = Maths.Min(GUN_CRAFTING_COLUMNS, GunCraftingEntries.size() - firstIndexInRow);

			blit(pose, i + 6, j + 23 + row * 18, getBlitOffset(), 172, 0, 18 * numEntriesInRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}

		// Render scrollbar
		int scrollbarPxHeight = 18 * GUN_CRAFTING_ROWS;
		int scrollbarMinSize = 8 + 8;
		int numRows = GunCraftingEntries.size() / GUN_CRAFTING_COLUMNS;

		float rowRatioDisplayable = (float)GUN_CRAFTING_ROWS / (float)numRows;
		if(rowRatioDisplayable < 1f)
		{
			blit(pose, i + 6 + 18 * GUN_CRAFTING_COLUMNS, j + 23, getBlitOffset(), 214, 18, 6, 18 * GUN_CRAFTING_ROWS, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			float scrollbarSize = Maths.Max(scrollbarPxHeight * rowRatioDisplayable, scrollbarMinSize);
			float parametricScroll = scrollOffs / numRows;

			float minPx = Maths.Lerp(0, scrollbarPxHeight - scrollbarSize, parametricScroll);
			float maxPx = Maths.Lerp(scrollbarSize, scrollbarPxHeight, parametricScroll);

			blit(pose, i + 6 + 18 * GUN_CRAFTING_COLUMNS, j + 23 + Maths.Floor(minPx), getBlitOffset(), 208, 18, 6, Maths.Floor(scrollbarSize) - 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, i + 6 + 18 * GUN_CRAFTING_COLUMNS, j + 23 + Maths.Floor(maxPx) - 8, getBlitOffset(), 208, 82, 6, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		if(selectedRecipe != -1)
		{
			// If the player has a gun in hand, we can do a comparison


			// Otherwise, just render the bars
			RenderStatComparisonBar(pose, i + 50, j + 36, 10, 10, 0, 20, 0);
			RenderStatComparisonBar(pose, i + 50, j + 46, 8, 8, 0, 20, 1);
			RenderStatComparisonBar(pose, i + 50, j + 56, 6, 6, 0, 20, 2);
			RenderStatComparisonBar(pose, i + 50, j + 66, 13, 13, 0, 20, 3);
		}

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
		if(selectedRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(selectedRecipe);
			if(entry != null)
			{
				font.draw(pose, entry.outputs[0].item,50, 26, 0x101010);

				ItemStack stack = MinecraftHelpers.CreateStack(entry.outputs[0]);
				//itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, pose, 50, 26, );
			}
		}
	}


}



























