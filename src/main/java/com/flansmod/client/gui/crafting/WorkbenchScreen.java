package com.flansmod.client.gui.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchMenu;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.GunCraftingEntryDefinition;
import com.flansmod.common.types.crafting.elements.GunCraftingPageDefinition;
import com.flansmod.util.Maths;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
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
		MODIFICATION
	}



	private static final Component[] TAB_TITLES = new Component[]{
		Component.translatable("gui.workbench.tab_materials"),
		Component.translatable("gui.workbench.tab_power"),
		Component.translatable("gui.workbench.tab_gun_crafting"),
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

	public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component component)
	{
		super(menu, inventory, component);

		Workbench = menu;
		Def = menu.Def;

		AvailableTabs = new Tab[] { Tab.MATERIALS };
		SelectedTab = AvailableTabs[0];

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

		for(int i = 0; i < WorkbenchScreen.Tab.values().length; i++)
		{
			final int index = i;
			addWidget(Button.builder(TAB_TITLES[i],
					(t) -> { SelectTab(WorkbenchScreen.Tab.values()[index]);})
				.bounds(x + 2 + 28*i, y + 0, 28, 18)
				.build());
		}


	}

	@Override
	public boolean mouseScrolled(double x, double y, double scroll)
	{
		if (!CanScroll())
			return false;

		scrollOffs += scroll;
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
			blit(pose, i + 3 + 28*SelectedTab.ordinal(), j + 16, getBlitOffset(), 263, 165, 26, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);


		}

		switch(SelectedTab)
		{
			case MATERIALS ->
			{
				// Change background to be slots
				blit(pose, i + 3, j + 22, getBlitOffset(), 3, 22, 166, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				blit(pose, i + 3, j + 40, getBlitOffset(), 3, 22, 166, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				blit(pose, i + 3, j + 58, getBlitOffset(), 3, 22, 166, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
			case POWER ->
			{
				// Render a power bar

				// Render a fuel slot

				// Render a battery slot

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

	protected void renderLabels(PoseStack pose, int x, int y)
	{
		super.renderLabels(pose, x, y);

		switch(SelectedTab)
		{
			case MODIFICATION ->
			{
				// Render floating rotating gun

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
	private void RenderGunCraftingBG(PoseStack pose, int i, int j)
	{
		// Render a background box
		blit(pose, i + 5, j + 22, getBlitOffset(), 172, 119, 162, 74, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Render scrollable gun recipe selection
		int gunRecipes = GunCraftingEntries.size();
		int numFullRows = gunRecipes / GUN_CRAFTING_COLUMNS;
		int numRows = numFullRows + (gunRecipes % GUN_CRAFTING_COLUMNS == 0 ? 0 : 1);
		// This will be rendered into the box topleft [6,23] dims [36,72]
		int firstVisibleRow = Maths.Floor(scrollOffs);
		float firstSlotYOffset = 18.0f * (firstVisibleRow - scrollOffs);

		int lastVisibleRow = Maths.Ceil(GUN_CRAFTING_ROWS + scrollOffs);
		float rowsToRender = lastVisibleRow < numFullRows ? 4f : 4f + firstSlotYOffset;

		blit(pose, i + 6, j + 23, getBlitOffset(), 172, 18f * (1 + firstSlotYOffset), 36, Maths.Ceil(72f * lastVisibleRow), TEXTURE_WIDTH, TEXTURE_HEIGHT);


		// Render some background buttons for the recipe selectors

	}

}



























