package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.WorkbenchMenu;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu>
{
	//private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;

	private enum Tab
	{
		MATERIALS,
		POWER,
		GUN_CRAFTING,
		ARMOUR_CRAFTING,
		PART_CRAFTING,
		MODIFICATION
	}



	private static final Component[] TAB_TITLES = new Component[]{
		Component.translatable("gui.workbench.tab_materials"),
		Component.translatable("gui.workbench.tab_power"),
		Component.translatable("gui.workbench.tab_gun_crafting"),
		Component.translatable("gui.workbench.tab_armour_crafting"),
		Component.translatable("gui.workbench.tab_part_crafting"),
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
		else
			Minecraft.getInstance().setScreen(null);

		imageWidth = 172;
		imageHeight = 217;

		titleLabelY = -1000;
		inventoryLabelX = 6;
		inventoryLabelY = 124;

		gunSelectorScrollOffset = 0.0f;
	}

	@Override
	protected void init()
	{
		super.init();
		int xOrigin = width / 2 - imageWidth / 2;
		int yOrigin = height / 2 - imageHeight / 2;

		// Init shared
		InitShared(xOrigin, yOrigin);

		// And each of the components
		if(HasGunCrafting())
			InitGunCrafting(xOrigin, yOrigin);
		if(HasGunModifying())
			InitGunModifying(xOrigin, yOrigin);
		if(HasMaterialTab())
			InitMaterials(xOrigin, yOrigin);
		if(HasPowerTab())
			InitPower(xOrigin, yOrigin);
	}

	@Override
	protected void containerTick()
	{
		UpdateGunCrafting(SelectedTab == Tab.GUN_CRAFTING);
		UpdateGunModifying(SelectedTab == Tab.MODIFICATION);
		UpdatePower(SelectedTab == Tab.POWER);
		UpdateMaterials(SelectedTab == Tab.MATERIALS);
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
		SetGunCraftingEnabled(tab == Tab.GUN_CRAFTING);
		SetMaterialsEnabled(tab == Tab.MATERIALS);
		SetPowerEnabled(tab == Tab.POWER);
		SetGunModifyingEnabled(tab == Tab.MODIFICATION);
	}

	@Override
	protected void renderBg(PoseStack pose, float f, int x, int y)
	{
		pose.pushPose();
		{
			int xOrigin = (width - imageWidth) / 2;
			int yOrigin = (height - imageHeight) / 2;

			RenderSharedBG(pose, xOrigin, yOrigin);
			switch(SelectedTab)
			{
				case MATERIALS -> 			{ RenderMaterialsBG(pose, xOrigin, yOrigin); }
				case POWER -> 				{ RenderPowerBG(pose, xOrigin, yOrigin); }
				case GUN_CRAFTING ->		{ RenderGunCraftingBG(pose, xOrigin, yOrigin); }
				case MODIFICATION -> 		{ RenderGunModifyingBG(pose, xOrigin, yOrigin); }
			}
		}
		pose.popPose();

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
		{
			RenderSharedFG(pose, x, y);
			switch(SelectedTab)
			{
				case MATERIALS -> 			{ RenderMaterialsFG(pose, x, y); }
				case POWER -> 				{ RenderPowerFG(pose, x, y); }
				case GUN_CRAFTING -> 		{ RenderGunCraftingFG(pose, x, y); }
				case MODIFICATION -> 		{ RenderGunModifyingFG(pose, x, y); }
			}
		}
		pose.popPose();
	}

	// =================================================================================================================
	// ================================================ SHARED ELEMENTS ================================================
	// =================================================================================================================
	private static final ResourceLocation WORKBENCH_SHARED = new ResourceLocation(FlansMod.MODID, "textures/gui/workbench_shared.png");
	private static final int MAX_TABS_PER_SIDE = 3;
	private static final int DISTANCE_BETWEEN_TABS = 36;
	private void InitShared(int xOrigin, int yOrigin)
	{
		if(AvailableTabs.length > 1)
		{
			int tabPosition = 0;
			for (Tab availableTab : AvailableTabs)
			{
				final int index = availableTab.ordinal();
				if(tabPosition >= 3)
				{
					addWidget(Button.builder(TAB_TITLES[index],
							(t) ->
							{
								SelectTab(Tab.values()[index]);
							})
						.bounds(xOrigin + imageWidth + 5, yOrigin + 20 + (tabPosition - 3) * DISTANCE_BETWEEN_TABS, 20, 20)
						.build());
				}
				else
				{
					addWidget(Button.builder(TAB_TITLES[index],
							(t) ->
							{
								SelectTab(Tab.values()[index]);
							})
						.bounds(xOrigin - 30 + 5, yOrigin + 20 + tabPosition * DISTANCE_BETWEEN_TABS, 20, 20)
						.build());
				}
				tabPosition++;
			}
		}
	}

	private void RenderSharedBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);
		//blit(pose, i, j, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		if(AvailableTabs.length > 1)
		{
			int tabPosition = 0;
			for (Tab availableTab : AvailableTabs)
			{
				RenderTabButton(pose, xOrigin, yOrigin, availableTab, tabPosition * DISTANCE_BETWEEN_TABS, tabPosition > MAX_TABS_PER_SIDE);
				tabPosition++;
			}
		}

		RenderPowerBar(pose, xOrigin + 116, yOrigin + 105);
	}

	private void RenderTabButton(PoseStack pose, int xOrigin, int yOrigin, Tab tab, int yHeight, boolean rightSide)
	{
		if(rightSide)
		{
			blit(pose, xOrigin - 30, yOrigin + 20 + yHeight, getBlitOffset(), 0, 0, 30, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, xOrigin - 30 + 5, yOrigin + 20 + yHeight + 5, getBlitOffset(), 140, 5 + 38 * tab.ordinal(), 20, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		else
		{
			blit(pose, xOrigin + imageWidth, yOrigin + 20 + yHeight, getBlitOffset(), 38, 0, 30, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, xOrigin + imageWidth + 5, yOrigin + 20 + yHeight + 5, getBlitOffset(), 140, 5 + 38 * tab.ordinal(), 20, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	private void RenderSharedFG(PoseStack pose, int xMouse, int yMouse)
	{
		if(AvailableTabs.length == 1) // && Banner == null
		{
			font.draw(pose, TAB_TITLES[AvailableTabs[0].ordinal()], 5, 5, 0x505050);
		}
	}

	// =================================================================================================================
	// =================================================== MATERIALS ===================================================
	// =================================================================================================================
	private static final ResourceLocation MATERIALS_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/materials.png");
	public boolean HasMaterialTab()
	{
		return Workbench.MaterialContainer.getContainerSize() > 0;
	}

	private void InitMaterials(int xOrigin, int yOrigin)
	{

	}

	private void SetMaterialsEnabled(boolean enable)
	{

	}

	private void UpdateMaterials(boolean enabled)
	{

	}

	private void RenderMaterialsBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		RenderPowerBar(pose, xOrigin + 61, yOrigin + 36);

		// Render a fuel slot
		if(Workbench.FuelContainer.getContainerSize() > 0)
		{
			blit(pose, xOrigin + 128, yOrigin + 48, getBlitOffset(), 295, 215, 18, 35, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			int litTime = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_TIME);
			int litDuration = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_DURATION);
			int px = (int) (11f * (float) litTime / (float) litDuration);
			blit(pose, xOrigin + 132, yOrigin + 51 + 11 - px, getBlitOffset(), 344, 213 + 11 - px, 9, px, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		// Render a battery slot
		if(Workbench.BatteryContainer.getContainerSize() > 0)
		{
			blit(pose, xOrigin + 77, yOrigin + 65, getBlitOffset(), 244, 232, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	private void RenderMaterialsFG(PoseStack pose, int xMouse, int yMouse)
	{
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;

		String storedEnergyAmount = MinecraftHelpers.GetFEString(fe) + " / " + MinecraftHelpers.GetFEString(feMax);
		font.draw(pose, storedEnergyAmount, imageWidth * 0.5f -font.width(storedEnergyAmount) / 2f, 23, 0x505050);
	}


	// =================================================================================================================
	// ===================================================== POWER =====================================================
	// =================================================================================================================
	private static final ResourceLocation POWER_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/power.png");
	private boolean HasPowerTab()
	{
		return Workbench.BatteryContainer.getContainerSize() > 0 ||
				Workbench.FuelContainer.getContainerSize() > 0;
	}

	private void InitPower(int xOrigin, int yOrigin)
	{

	}

	private void SetPowerEnabled(boolean enable)
	{

	}

	private void UpdatePower(boolean enabled)
	{

	}

	private void RenderPowerBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, POWER_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		RenderPowerBar(pose, xOrigin + 61, yOrigin + 36);

		// Render a fuel slot
		if(Workbench.FuelContainer.getContainerSize() > 0)
		{
			blit(pose, xOrigin + 128, yOrigin + 48, getBlitOffset(), 295, 215, 18, 35, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			int litTime = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_TIME);
			int litDuration = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_DURATION);
			int px = (int) (11f * (float) litTime / (float) litDuration);
			blit(pose, xOrigin + 132, yOrigin + 51 + 11 - px, getBlitOffset(), 344, 213 + 11 - px, 9, px, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		// Render a battery slot
		if(Workbench.BatteryContainer.getContainerSize() > 0)
		{
			blit(pose, xOrigin + 77, yOrigin + 65, getBlitOffset(), 244, 232, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	private void RenderPowerFG(PoseStack pose, int mouseX, int mouseY)
	{
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;

		String storedEnergyAmount = MinecraftHelpers.GetFEString(fe) + " / " + MinecraftHelpers.GetFEString(feMax);
		font.draw(pose, storedEnergyAmount, imageWidth * 0.5f - (font.width(storedEnergyAmount) / 2f), 23, 0x505050);
	}

	// =================================================================================================================
	// ================================================= GUN MODIFYING =================================================
	// =================================================================================================================
	private static final ResourceLocation GUN_MODIFICATION_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int SKINS_PER_ROW = 4;
	private static final int SKIN_ROWS = 3;
	private final Button[] SkinButtons = new Button[SKINS_PER_ROW * SKIN_ROWS];

	private boolean HasGunModifying()
	{
		return Workbench.Def.gunModifying.isActive;
	}

	private void SetGunModifyingEnabled(boolean enable)
	{
		UpdateGunModifying(enable);
	}

	private void UpdateGunModifying(boolean enabled)
	{
		if(HasGunModifying())
		{
			int numSkinButtons = 0;

			if (enabled && Workbench.GunContainer.getContainerSize() > 0 && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				PaintableDefinition paintableDef = flanItem.GetPaintDef();
				if (paintableDef.IsValid())
				{
					numSkinButtons = paintableDef.paintjobs.length + 1;
				}
			}

			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					if (SkinButtons[index] != null)
						SkinButtons[index].active = index < numSkinButtons;
				}
			}
		}
	}

	private void InitGunModifying(int xOrigin, int yOrigin)
	{
		if(HasGunModifying())
		{
			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					SkinButtons[index] = Button.builder(
						Component.empty(),
						(t) ->
						{
							NetworkedButtonPress(WorkbenchMenu.BUTTON_SELECT_SKIN_0 + index);
							//SelectSkin(index);
						})
						.bounds(xOrigin + 84 + 18 * i, yOrigin + 45 + 18 * j, 18, 18)
						.build();
					addWidget(SkinButtons[index]);
				}
			}
		}
	}

	private void RenderGunModifyingBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the gun before the background so it ends up behind
		if(Workbench.GunContainer.getContainerSize() > 0)
		{
			RenderGunStack(pose, xOrigin + 126, yOrigin + 31, Workbench.GunContainer.getItem(0));
		}

		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUN_MODIFICATION_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);


		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// Render the slot BG for the gun slot
			blit(pose, xOrigin + 31, yOrigin + 48, getBlitOffset(), 198, 26, 22, 22, TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
							xOrigin + 5 + 26 * modSlot.x,
							yOrigin + 22 + 26 * modSlot.y,
							getBlitOffset(),
							172 + 26 * modSlot.x,
							26 * modSlot.y,
							22, 22,
							TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}
				}

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					blit(pose, xOrigin + 84, yOrigin + 45, getBlitOffset(), 172, 165, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						blit(pose, xOrigin + 84 + 18 * xIndex, yOrigin + 45 + 18 * yIndex, getBlitOffset(), 172, 165, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}
				}
			}
		}
	}

	private void RenderGunModifyingFG(PoseStack pose, int xMouse, int yMouse)
	{
		//int xOrigin = (width - imageWidth) / 2;
		//int yOrigin = (height - imageHeight) / 2;
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if (paintableDefinition.paintjobs.length > 0)
				{
					// Render default skin
					{
						ItemStack paintedStack = gunStack.copy();
						flanItem.SetPaintjobName(paintedStack, "default");
						itemRenderer.renderGuiItem(paintedStack, 85, 46);
					}

					// And other skins
					for (int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						ItemStack paintedStack = gunStack.copy();
						flanItem.SetPaintjobName(paintedStack, paintableDefinition.paintjobs[p].textureName);
						itemRenderer.renderGuiItem(paintedStack, 85 + 18 * xIndex, 46 + 18 * yIndex);
					}
				}
			}
		}
	}

	// =================================================================================================================
	// ================================================ PART CRAFTING ==================================================
	// =================================================================================================================
	private static final ResourceLocation PART_CRAFTING_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/part_crafting.png");

	private Button[] PartCraftingButtons;

	/*
	public boolean HasPartCrafting()
	{
		return Workbench.Def.partCrafting.isActive;
	}

	public void SetPartCraftingEnabled(boolean enable)
	{
		RefreshGunCraftingFilters();
		for(int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
		{
			for (int i = 0; i < PART_RECIPE_VIEWER_COLUMNS; i++)
			{

			}
		}
	}

	private void InitPartCrafting(int xOrigin, int yOrigin)
	{
		PartCraftingButtons = new Button[PART_RECIPE_VIEWER_ROWS * PART_RECIPE_VIEWER_COLUMNS];
		for(int j = 0; j < PART_RECIPE_VIEWER_ROWS; j++)
		{
			for(int i = 0; i < PART_RECIPE_VIEWER_COLUMNS; i++)
			{
				final int index = j * PART_RECIPE_VIEWER_COLUMNS + i;
				PartCraftingButtons[index] = Button.builder(Component.empty(),
						(t) ->
						{
							GoToPartCrafting(index);
						})
					.bounds(xOrigin + 57 + 20 * i, yOrigin + 75 + 20 * j, 9, 9)
					.build();
				addWidget(PartCraftingButtons[index]);
			}
		}
	}
	*/

	// =================================================================================================================
	// ================================================= GUN CRAFTING ==================================================
	// =================================================================================================================
	private ArrayList<GunCraftingEntryDefinition> GunCraftingEntries = new ArrayList<>();
	private float gunSelectorScrollOffset = 0.0f;
	private float recipeSelectorScrollOffset = 0.0f;
	private static final int GUN_SELECTOR_COLUMNS = 2;
	private static final int GUN_SELECTOR_ROWS = 4;
	private static final int GUN_RECIPE_VIEWER_COLUMNS = 4;
	private static final int GUN_RECIPE_VIEWER_ROWS = 2;
	private Button[] GunSelectionButtons;
	private Button[] GoToPartCraftingButtons;
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

	public boolean HasGunCrafting()
	{
		return Workbench.Def.gunCrafting.isActive;
	}

	public void SetGunCraftingEnabled(boolean enable)
	{
		RefreshGunCraftingFilters();
		for(int j = 0; j < GUN_SELECTOR_ROWS; j++)
		{
			for (int i = 0; i < GUN_SELECTOR_COLUMNS; i++)
			{

			}
		}
	}

	private void InitGunCrafting(int xOrigin, int yOrigin)
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
					.bounds(xOrigin + 6 + i*18, yOrigin + 23 + j*18, 18, 18)
					.build();
				addWidget(GunSelectionButtons[index]);
			}
		}

		GoToPartCraftingButtons = new Button[GUN_RECIPE_VIEWER_ROWS * GUN_RECIPE_VIEWER_COLUMNS];
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
					.bounds(xOrigin + 57 + 20 * i, yOrigin + 75 + 20 * j, 9, 9)
					.build();
				addWidget(GoToPartCraftingButtons[index]);
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

	private void UpdateGunCrafting(boolean enabled)
	{
		ShowPotentialMatchTicker += 1.0f / 20.0f;
	}

	private void GoToPartCrafting(int relativeIndex)
	{
		int partIndex = Maths.Floor(recipeSelectorScrollOffset) + relativeIndex;
		if(partIndex < CachedSlotInfo.size())
		{
			// TODO:
			SelectTab(Tab.PART_CRAFTING);
		}
		else
		{
			FlansMod.LOGGER.warn("Tried to select invalid Part recipe " + partIndex + "/" + CachedSlotInfo.size());
		}
	}
	private void RenderGunCraftingBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUN_FABRICATION_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Render a scrollbar
		int numRows = GunCraftingEntries.size() / GUN_SELECTOR_COLUMNS;
		RenderScrollbar(pose, xOrigin + 40, yOrigin + 25, 6, 89, gunSelectorScrollOffset, 0, numRows);

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
							blit(pose, xOrigin + 48 + 20 * x, yOrigin + 75 + 20 * y, getBlitOffset(), 0, 247 + tier * 9, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}
						else
						{
							IngredientDefinition additionalDef = slotInfo.GetAsAdditionalDef();
							// Cover up the part crafting button
							blit(pose, xOrigin + 57 + 20 * x, yOrigin + 75 + 20 * y, getBlitOffset(), 229, 62, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
							// Render a faded item stack
							// (Defer to later)
						}
					}
					else
					{
						// Cover up the background for this button
						blit(pose, xOrigin + 48 + x * 20, yOrigin + 56 + y * 20, getBlitOffset(), 229, 72, 18, 28, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
						itemRenderer.renderGuiItem(stack, xOrigin + 49 + 20 * x, yOrigin + 57 + 20 * y);
					}
				}
			}
		}




		int firstRow = Maths.Floor(gunSelectorScrollOffset);
		for(int row = 0; row < GUN_SELECTOR_ROWS; row++)
		{
			int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
			int numEntriesInRow = Maths.Min(GUN_SELECTOR_COLUMNS, GunCraftingEntries.size() - firstIndexInRow);

			blit(pose, xOrigin + 6, yOrigin + 23 + row * 18, getBlitOffset(), 172, 0, 18 * numEntriesInRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}

		// Render scrollbar
		int scrollbarPxHeight = 18 * GUN_SELECTOR_ROWS;
		int scrollbarMinSize = 8 + 8;

		float rowRatioDisplayable = (float) GUN_SELECTOR_ROWS / (float)numRows;
		if(rowRatioDisplayable < 1f)
		{
			blit(pose, xOrigin + 6 + 18 * GUN_SELECTOR_COLUMNS, yOrigin + 23, getBlitOffset(), 214, 18, 6, 18 * GUN_SELECTOR_ROWS, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			float scrollbarSize = Maths.Max(scrollbarPxHeight * rowRatioDisplayable, scrollbarMinSize);
			float parametricScroll = gunSelectorScrollOffset / numRows;

			float minPx = Maths.Lerp(0, scrollbarPxHeight - scrollbarSize, parametricScroll);
			float maxPx = Maths.Lerp(scrollbarSize, scrollbarPxHeight, parametricScroll);

			blit(pose, xOrigin + 6 + 18 * GUN_SELECTOR_COLUMNS, yOrigin + 23 + Maths.Floor(minPx), getBlitOffset(), 208, 18, 6, Maths.Floor(scrollbarSize) - 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, xOrigin + 6 + 18 * GUN_SELECTOR_COLUMNS, yOrigin + 23 + Maths.Floor(maxPx) - 8, getBlitOffset(), 208, 82, 6, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		if(SelectedGunRecipe != -1)
		{
			// If the player has a gun in hand, we can do a comparison


			// Otherwise, just render the bars
			RenderStatComparisonBar(pose, xOrigin + 50, yOrigin + 36, 10, 10, 0, 20, 0);
			RenderStatComparisonBar(pose, xOrigin + 50, yOrigin + 46, 8, 8, 0, 20, 1);
			RenderStatComparisonBar(pose, xOrigin + 50, yOrigin + 56, 6, 6, 0, 20, 2);
			RenderStatComparisonBar(pose, xOrigin + 50, yOrigin + 66, 13, 13, 0, 20, 3);
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


	// -----------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------- GENERAL METHODS --------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public void RenderPowerBar(PoseStack pose, int x, int y)
	{
		// Render a power bar
		blit(pose, x, y, getBlitOffset(), 228, 203, 51, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Render the partial fill texture
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;
		int px = (int)(51f * (float)fe / (float)feMax);
		blit(pose, x, y, getBlitOffset(), 433, 15, px, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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

	private void RenderGunStack(PoseStack pose, int x, int y, ItemStack gunStack)
	{
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(gunStack);
		if(gunRenderer != null)
		{
			pose.pushPose();
			{
				MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
				pose.translate(x + 8, y + 12, getBlitOffset());
				pose.scale(32f, 32f, 32f);
				pose.mulPose(new Quaternionf().rotateLocalX(Maths.PiF));
				pose.mulPose(new Quaternionf().rotateLocalY(Maths.PiF));
				gunRenderer.Render(null, gunStack, new RenderContext(
					buffers,
					ItemTransforms.TransformType.GROUND,
					pose,
					0xffffff,
					0
				));
				buffers.endBatch();
			}
			pose.popPose();
		}

	}

	private boolean NetworkedButtonPress(int buttonID)
	{
		if(minecraft == null || minecraft.gameMode == null)
			return false;
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonID);
		return true;
	}


}



























