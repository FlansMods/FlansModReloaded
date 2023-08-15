package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.crafting.FabricationRecipeMatcher;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.WorkbenchMenu;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.elements.MagazineSlotSettingsDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
		Component.translatable("workbench.tab_materials"),
		Component.translatable("workbench.tab_power"),
		Component.translatable("workbench.tab_gun_crafting"),
		Component.translatable("workbench.tab_armour_crafting"),
		Component.translatable("workbench.tab_part_crafting"),
		Component.translatable("workbench.tab_modification")
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

		if(AvailableTabs.length > 0)
			SelectTab(AvailableTabs[0]);
		else
			Minecraft.getInstance().setScreen(null);
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
		switch(SelectedTab)
		{
			case GUN_CRAFTING ->
			{
				return UpdateScrollGunCrafting(Maths.Floor(x), Maths.Floor(y), scroll);

			}
		}
		return true;
	}

	private void SelectTab(WorkbenchScreen.Tab tab)
	{
		SelectedTab = tab;
		SetGunCraftingEnabled(tab == Tab.GUN_CRAFTING);
		SetMaterialsEnabled(tab == Tab.MATERIALS);
		SetPowerEnabled(tab == Tab.POWER);
		SetGunModifyingEnabled(tab == Tab.MODIFICATION);
		//SetPartCraftingEnabled(tab == Tab.PART_CRAFTING);
		//SetArmourCraftingEnabled(tab == Tab.ARMOUR_CRAFTING);

		switch(tab)
		{
			case GUN_CRAFTING -> { Workbench.SwitchToGunCrafting(); }
			//case PART_CRAFTING -> { Workbench.SwitchToPartCrafting(); }
			case POWER -> { Workbench.SwitchToPower(); }
			case MATERIALS -> { Workbench.SwitchToMaterials(); }
			//case ARMOUR_CRAFTING -> { Workbench.SwitchToArmourCrafting(); }
			case MODIFICATION -> { Workbench.SwitchToGunModification(); }
		}

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
				//case PART_CRAFTING ->		{ RenderPartCraftingBG(pose, xOrigin, yOrigin); }
				//case ARMOUR_CRAFTING -> 	{ RenderArmourCraftingBG(pose, xOrigin, yOrigin); }
			}
		}
		pose.popPose();

	}

	@Override
	public void render(PoseStack pose, int xMouse, int yMouse, float f)
	{
		super.render(pose, xMouse, yMouse, f);

		if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem())
		{
			super.renderTooltip(pose, xMouse, yMouse);
			return;
		}
		if(RenderSharedTooltip(pose, xMouse, yMouse))
			return;
		switch(SelectedTab)
		{
			case MATERIALS -> { if(RenderMaterialsTooltip(pose, xMouse, yMouse)) return; }
			case POWER -> { if(RenderPowerTooltip(pose, xMouse, yMouse)) return; }
			case GUN_CRAFTING -> { if(RenderGunCraftingTooltip(pose, xMouse, yMouse)) return; }
			case MODIFICATION -> { if(RenderGunModifyingTooltip(pose, xMouse, yMouse)) return; }
		}
		//renderTooltip(pose, xMouse, yMouse);
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
	private static final int DISTANCE_BETWEEN_TABS = 32;
	private void InitShared(int xOrigin, int yOrigin)
	{
		if(AvailableTabs.length > 1)
		{
			int tabPosition = 0;
			for (Tab availableTab : AvailableTabs)
			{
				final int index = availableTab.ordinal();
				if(tabPosition >= MAX_TABS_PER_SIDE)
				{
					addWidget(Button.builder(TAB_TITLES[index],
							(t) ->
							{
								SelectTab(Tab.values()[index]);
							})
						.bounds(xOrigin + imageWidth, yOrigin + 20 + (tabPosition - MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30, 30)
						.build());
				}
				else
				{
					addWidget(Button.builder(TAB_TITLES[index],
							(t) ->
							{
								SelectTab(Tab.values()[index]);
							})
						.bounds(xOrigin - 30, yOrigin + 20 + tabPosition * DISTANCE_BETWEEN_TABS, 30, 30)
						.build());
				}
				tabPosition++;
			}
		}
	}

	private boolean RenderSharedTooltip(PoseStack pose, int xMouse, int yMouse)
	{
		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

		if(AvailableTabs.length > 1)
		{
			int tabPosition = 0;
			for (Tab availableTab : AvailableTabs)
			{
				boolean rightSide = tabPosition <= MAX_TABS_PER_SIDE;
				if(rightSide)
				{
					if (InBox(xMouse, yMouse, xOrigin - 30, 30, yOrigin + 20 + (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30))
					{
						renderTooltip(pose, TAB_TITLES[availableTab.ordinal()], xMouse, yMouse);
						return true;
					}
				}
				else
				{
					if (InBox(xMouse, yMouse, xOrigin + imageWidth, 30, yOrigin + 20 + (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30))
					{
						renderTooltip(pose, TAB_TITLES[availableTab.ordinal()], xMouse, yMouse);
						return true;
					}
				}
				tabPosition++;
			}
		}

		return false;
	}

	private void RenderSharedBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);
		RenderPowerBar(pose, xOrigin + 116, yOrigin + 105);
	}

	private void RenderTabButton(PoseStack pose, int xOrigin, int yOrigin, Tab tab, int yHeight, boolean leftSide)
	{
		if(leftSide)
		{
			if(tab == SelectedTab)
				blit(pose, xOrigin - 30, yOrigin + 20 + yHeight, getBlitOffset(), 0, 34, 33, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			else
				blit(pose, xOrigin - 30, yOrigin + 20 + yHeight, getBlitOffset(), 0, 0, 30, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, xOrigin - 30 + 6, yOrigin + 28 + yHeight, getBlitOffset(), 141, 8 + 34 * tab.ordinal(), 21, 14, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		else
		{
			if(tab == SelectedTab)
				blit(pose, xOrigin + imageWidth - 3, yOrigin + 20 + yHeight, getBlitOffset(), 35, 34, 33, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			else
				blit(pose, xOrigin + imageWidth, yOrigin + 20 + yHeight, getBlitOffset(), 38, 0, 30, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			blit(pose, xOrigin + imageWidth + 6, yOrigin + 28 + yHeight, getBlitOffset(), 141, 8 + 34 * tab.ordinal(), 21, 14, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	private void RenderSharedFG(PoseStack pose, int xMouse, int yMouse)
	{
		//if Banner == null
		{
			font.draw(pose, TAB_TITLES[SelectedTab.ordinal()], 5, 5, 0x505050);
		}

		// Render tabs over BG
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);
		if(AvailableTabs.length > 1)
		{
			int xOrigin = (width - imageWidth) / 2;
			int yOrigin = (height - imageHeight) / 2;
			int tabPosition = 0;
			for (Tab availableTab : AvailableTabs)
			{
				RenderTabButton(pose, 0, 0, availableTab, (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, tabPosition <= MAX_TABS_PER_SIDE);
				tabPosition++;
			}
		}
	}

	// =================================================================================================================
	// =================================================== MATERIALS ===================================================
	// =================================================================================================================
	private static final ResourceLocation MATERIALS_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/materials.png");
	public boolean HasMaterialTab()
	{
		return Workbench.Def.itemHolding.slots.length > 0;
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

	private boolean RenderMaterialsTooltip(PoseStack pose, int xMouse, int yMouse)
	{
		return false;
	}

	private void RenderMaterialsBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, MATERIALS_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		for(int j = 0; j < 5; j++)
		{
			int numSlotsOnThisRow = Maths.Min(Workbench.MaterialContainer.getContainerSize() - j * 9, 9);
			if(numSlotsOnThisRow > 0)
			{
				blit(pose, xOrigin + 5, yOrigin + 22 + 18 * j, getBlitOffset(), 5, 136, 18 * numSlotsOnThisRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			}
		}

	}

	private void RenderMaterialsFG(PoseStack pose, int xMouse, int yMouse)
	{

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

	private boolean RenderPowerTooltip(PoseStack pose, int xMouse, int yMouse)
	{
		return false;
	}

	private void RenderPowerBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, POWER_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Render a fuel slot
		if(Workbench.FuelContainer.getContainerSize() > 0)
		{
			int litTime = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_TIME);
			int litDuration = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_DURATION);
			int px = (int) (11f * (float) litTime / (float) litDuration);
			blit(pose, xOrigin + 132, yOrigin + 51 + 11 - px, getBlitOffset(), 344, 213 + 11 - px, 9, px, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		else // Cover up the fuel slot
		{
			blit(pose, xOrigin + 125, yOrigin + 65, getBlitOffset(), 24, 65, 18, 34, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		// Render a battery slot
		if(Workbench.BatteryContainer.getContainerSize() > 0)
		{
			blit(pose, xOrigin + 77, yOrigin + 65, getBlitOffset(), 244, 232, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		else // Cover up the battery slot
		{
			blit(pose, xOrigin + 74, yOrigin + 81, getBlitOffset(), 24, 81, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		RenderPowerBar(pose, xOrigin + 58, yOrigin + 52);
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

	private static final int ATTACHMENT_SLOTS_ORIGIN_X = 5;
	private static final int ATTACHMENT_SLOTS_ORIGIN_Y = 26;

	private static final int PAINT_BUCKET_SLOT_ORIGIN_X = 76;
	private static final int PAINT_BUCKET_SLOT_ORIGIN_Y = 25;
	private static final int MAG_UPGRADE_SLOT_ORIGIN_X = 76;
	private static final int MAG_UPGRADE_SLOT_ORIGIN_Y = 83;

	private static final int SKIN_SELECTOR_ORIGIN_X = 96;
	private static final int SKIN_SELECTOR_ORIGIN_Y = 16;
	private static final int SKINS_PER_ROW = 4;
	private static final int SKIN_ROWS = 3;
	private final Button[] SkinButtons = new Button[SKINS_PER_ROW * SKIN_ROWS];

	private static final int MAGAZINE_SELECTOR_ORIGIN_X = 96;
	private static final int MAGAZINE_SELECTOR_ORIGIN_Y = 74;
	private static final int MAGAZINES_PER_ROW = 4;
	private static final int MAGAZINE_ROWS = 2;
	private final Button[] MagazineButtons = new Button[MAGAZINES_PER_ROW * MAGAZINE_ROWS];


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

			int numMagButtons = 0;
			if (enabled && Workbench.GunContainer.getContainerSize() > 0 && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				if(flanItem.Def() instanceof GunDefinition gunDefinition)
				{
					numMagButtons = gunDefinition.primaryMagazines.GetMatchingMagazines().size();
				}
			}

			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					if (MagazineButtons[index] != null)
						MagazineButtons[index].active = index < numMagButtons;
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
						.bounds(xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * i, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(SkinButtons[index]);
				}
			}

			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					MagazineButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenu.BUTTON_SELECT_MAGAZINE_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(MagazineButtons[index]);
				}
			}
		}
	}

	private boolean RenderGunModifyingTooltip(PoseStack pose, int xMouse, int yMouse)
	{
		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

		if(InBox(xMouse, yMouse, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, 18, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, 18))
		{
			renderTooltip(pose, Component.translatable("workbench.slot.paint_can"), xMouse, yMouse);
			return true;
		}
		if(InBox(xMouse, yMouse, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, 18, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, 18))
		{
			renderTooltip(pose, Component.translatable("workbench.slot.mag_upgrade"), xMouse, yMouse);
			return true;
		}


		if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, 24))
		{
			renderTooltip(pose, Component.translatable("workbench.slot.gun"), xMouse, yMouse);
			return true;
		}




		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);

				for (WorkbenchMenu.ModSlot modSlot : WorkbenchMenu.ModSlot.values())
				{
					if(flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex))
					{
						if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + modSlot.x * 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + modSlot.y * 26, 24))
						{
							renderTooltip(pose, Component.translatable("workbench.slot.attachments." + modSlot.attachType.toString().toLowerCase()), xMouse, yMouse);
							return true;
						}
					}
				}

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 18))
					{
						List<FormattedCharSequence> lines = new ArrayList<>();
						lines.add(Component.translatable("paintjob.default").getVisualOrderText());
						renderTooltip(pose, lines, xMouse, yMouse);
						return true;
					}
					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 18))
						{
							List<FormattedCharSequence> lines = new ArrayList<>();
							lines.add(Component.translatable("paintjob." + flanItem.DefinitionLocation.getNamespace() + "." + paintableDefinition.paintjobs[p].textureName).getVisualOrderText());
							lines.add(Component.translatable("paintjob.cost").getVisualOrderText());

							renderTooltip(pose, lines, xMouse, yMouse);
							return true;
						}
					}
				}

				if (flanItem.Def() instanceof GunDefinition gunDef)
				{
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(EActionInput.PRIMARY);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();



					for (int j = 0; j < MAGAZINE_ROWS; j++)
					{
						for (int i = 0; i < MAGAZINES_PER_ROW; i++)
						{
							final int index = j * MAGAZINES_PER_ROW + i;
							if(index < matchingMags.size())
							{
								if(InBox(xMouse, yMouse, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18))
								{
									List<FormattedCharSequence> lines = new ArrayList<>();
									lines.add(Component.translatable("magazine." + matchingMags.get(i).Location.getNamespace() + "." + matchingMags.get(i).Location.getPath()).getVisualOrderText());
									lines.add(Component.translatable("magazine.num_rounds", matchingMags.get(i).numRounds).getVisualOrderText());

									renderTooltip(pose, lines, xMouse, yMouse);
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private void RenderGunModifyingBG(PoseStack pose, int xOrigin, int yOrigin)
	{
		// Render the gun before the background so it ends up behind
		if(Workbench.GunContainer.getContainerSize() > 0)
		{
			pose.pushPose();
			pose.translate(xOrigin + imageWidth + 64f, yOrigin + 64f, 0f);
			pose.mulPose(new Quaternionf()
				.rotateLocalZ(-Maths.PiF * 0.25f)
				.rotateLocalY(Minecraft.getInstance().level.getGameTime() * 0.01f));
			pose.translate(-10f, 0f, 0f);

			RenderGunStack(pose, 0, 0, Workbench.GunContainer.getItem(0));
			//xOrigin + 126, yOrigin + 31
			pose.popPose();
		}

		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUN_MODIFICATION_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);


		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// Render the slot BG for the gun slot
			blit(pose, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, getBlitOffset(), 198, 26, 22, 22, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			// Paint Can Slot
			blit(pose, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, getBlitOffset(), 208, 201, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			// Mag Slot
			blit(pose, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, getBlitOffset(), 190, 201, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
							xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26 * modSlot.x,
							yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26 * modSlot.y,
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
					if(flanItem.GetPaintjobName(gunStack).equals("default"))
					{
						blit(pose, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, getBlitOffset(), 172, 201, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}
					else blit(pose, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, getBlitOffset(), 172, 165, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(flanItem.GetPaintjobName(gunStack).equals(paintableDefinition.paintjobs[p].textureName))
						{
							blit(pose, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, getBlitOffset(), 172, 201, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}
						else blit(pose, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, getBlitOffset(), 172, 165, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}
				}


				// Magazine selector
				if(flanItem instanceof GunItem gunItem)
				{
					MagazineSlotSettingsDefinition magSettings = gunItem.Def().GetMagazineSettings(EActionInput.PRIMARY);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					MagazineDefinition currentMagType = gunItem.GetMagazineType(gunStack, EActionInput.PRIMARY, 0);
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % SKINS_PER_ROW;
						int yIndex = i / SKINS_PER_ROW;

						if(matchingMags.get(i) == currentMagType)
						{
							blit(pose, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, getBlitOffset(), 172, 201, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}
						else blit(pose, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, getBlitOffset(), 172, 165, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
						itemRenderer.renderGuiItem(paintedStack, SKIN_SELECTOR_ORIGIN_X + 1,  SKIN_SELECTOR_ORIGIN_Y + 1);
					}

					// And other skins
					for (int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						ItemStack paintedStack = gunStack.copy();
						flanItem.SetPaintjobName(paintedStack, paintableDefinition.paintjobs[p].textureName);
						itemRenderer.renderGuiItem(paintedStack, SKIN_SELECTOR_ORIGIN_X + 1 + 18 * xIndex, SKIN_SELECTOR_ORIGIN_Y + 1 + 18 * yIndex);
					}
				}

				if(flanItem.Def() instanceof GunDefinition gunDef)
				{
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(EActionInput.PRIMARY);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % MAGAZINES_PER_ROW;
						int yIndex = i / MAGAZINES_PER_ROW;

						// RENDER MAG
						TextureAtlasSprite sprite = FlansModClient.MAGAZINE_ATLAS.GetIcon(matchingMags.get(i).Location);
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						RenderSystem.setShaderTexture(0, sprite.atlasLocation());


						blit(pose, MAGAZINE_SELECTOR_ORIGIN_X + 1 + xIndex * 18, MAGAZINE_SELECTOR_ORIGIN_Y + 1 + yIndex * 18, getBlitOffset(), 16, 16, sprite);
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
	private static final int GUN_SELECTOR_X_ORIGIN = 5;
	private static final int GUN_SELECTOR_Y_ORIGIN = 16;
	private static final int GUN_SELECTOR_COLUMNS = 2;
	private static final int GUN_SELECTOR_ROWS = 5;
	private Button[] GunSelectionButtons;
	private int SelectedGunRecipe = -1;

	private static final int GUN_STATS_X_ORIGIN = 50;
	private static final int GUN_STATS_Y_ORIGIN = 17;


	private float recipeSelectorScrollOffset = 0.0f;
	private static final int GUN_RECIPE_VIEWER_X_ORIGIN = 48;
	private static final int GUN_RECIPE_VIEWER_Y_ORIGIN = 54;
	private static final int GUN_RECIPE_VIEWER_COLUMNS = 4;
	private static final int GUN_RECIPE_VIEWER_ROWS = 2;
	private Button[] GoToPartCraftingButtons;
	private float ShowPotentialMatchTicker = 0.0f;
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

		public void FindMatches()
		{
			if(IsTieredMaterialIngredient())
			{
				TieredIngredientDefinition tiered = GetAsTieredDef();
				if(tiered != null)
				{
					List<PartDefinition> parts = FlansMod.PARTS.Find((part) ->
					{
						if (tiered.tag != null && !tiered.tag.isEmpty())
						{
							boolean foundTag = false;
							for (String tag : part.itemSettings.tags)
							{
								if (tag.equals(tiered.tag))
								{
									foundTag = true;
									break;
								}
							}
							if (!foundTag)
								return false;
						}

						boolean foundTier = false;
						for (int tier : tiered.allowedTiers)
						{
							if (part.materialTier == tier)
							{
								foundTier = true;
								break;
							}
						}
						if (!foundTier)
							return false;

						boolean matchesMaterial = false;
						for (EMaterialType material : tiered.allowedMaterials)
						{
							if (part.materialType == material)
							{
								matchesMaterial = true;
								break;
							}
						}
						return matchesMaterial;
					});
					for (PartDefinition part : parts)
					{
						Item item = ForgeRegistries.ITEMS.getValue(part.Location);
						if(item != null)
							PotentialMatches.add(new ItemStack(item));
					}
				}
			}
			else
			{
				IngredientDefinition ingredient = GetAsAdditionalDef();
				if(ingredient != null)
				{
					if (ingredient.compareItemName)
					{
						try
						{
							ResourceLocation resLoc = new ResourceLocation(ingredient.itemName);
							Item item = ForgeRegistries.ITEMS.getValue(resLoc);
							PotentialMatches.add(new ItemStack(item, ingredient.count));
						} catch (Exception e)
						{
							FlansMod.LOGGER.error("Failed to match ingredient " + ingredient.itemName);
						}
					}
					else
					{
						try
						{
							List<TagKey<Item>> requiredItemTags = new ArrayList<>();
							for (String tag : ingredient.requiredTags)
								requiredItemTags.add(ItemTags.create(new ResourceLocation(tag)));
							List<TagKey<Item>> disallowedItemTags = new ArrayList<>();
							for (String tag : ingredient.disallowedTags)
								disallowedItemTags.add(ItemTags.create(new ResourceLocation(tag)));

							for (var kvp : ForgeRegistries.ITEMS.getEntries())
							{
								if (ingredient.compareItemTags)
								{
									boolean passes = true;
									for(TagKey<Item> tag : disallowedItemTags)
										if(kvp.getValue().builtInRegistryHolder().is(tag))
											passes = false;

									for(TagKey<Item> tag : requiredItemTags)
										if(!kvp.getValue().builtInRegistryHolder().is(tag))
											passes = false;

									if(passes)
									{
										PotentialMatches.add(new ItemStack(kvp.getValue(), ingredient.count));
									}
								}
							}
						}
						catch(Exception e)
						{
							FlansMod.LOGGER.error("Failed to match ingredient by tags");
						}
					}
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

	private final List<GunCraftingSlotInfo> CachedSlotInfo = new ArrayList<>();

	public boolean HasGunCrafting()
	{
		return Workbench.Def.gunCrafting.isActive;
	}

	public void SetGunCraftingEnabled(boolean enable)
	{
		RefreshGunCraftingFilters();
		UpdateActiveGunSelectionButtons();
	}

	private void UpdateGunCrafting(boolean enabled)
	{
		ShowPotentialMatchTicker += 1.0f / 20.0f;
		UpdateActiveGunSelectionButtons();
	}

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
					GunSelectionButtons[relativeIndex].active = SelectedTab == Tab.GUN_CRAFTING && (firstIndex + relativeIndex < GunCraftingEntries.size());
				}
			}
		}
	}

	private boolean UpdateScrollGunCrafting(int xMouse, int yMouse, double scroll)
	{

		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

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
			return true;
		}

		return false;
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
					.bounds(xOrigin + GUN_SELECTOR_X_ORIGIN + i*18, yOrigin + GUN_SELECTOR_Y_ORIGIN + j*18, 18, 18)
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
					.bounds(xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 9 + 20 * i, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 19 + 20 * j, 9, 9)
					.build();
				addWidget(GoToPartCraftingButtons[index]);
			}
		}
	}

	private static final String DISALLOWED_TAG = "crafting.disallowed_tag";
	private static final String REQUIRED_TAG = "crafting.required_tag";


	private boolean RenderGunCraftingTooltip(PoseStack pose, int xMouse, int yMouse)
	{
		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

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
									renderTooltip(pose, lines, xMouse, yMouse);
								}
							}
							else
							{
								IngredientDefinition ingredientDef = CachedSlotInfo.get(index).GetAsAdditionalDef();
								if(ingredientDef != null)
								{
									if(ingredientDef.compareItemName)
										renderTooltip(pose, Component.translatable(ingredientDef.itemName), xMouse, yMouse);
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
										renderTooltip(pose, lines, xMouse, yMouse);
									}
									return true;
								}
							}
						}
						if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 20 * i, 9, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 20 + 30 * j, 9))
						{
							renderTooltip(pose, Component.translatable("crafting.auto_add_best_parts"), xMouse, yMouse);
							return true;
						}
						if (InBox(xMouse, yMouse, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 10 + 20 * i, 9, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + 20 + 30 * j, 9))
						{
							renderTooltip(pose, Component.translatable("crafting.switch_to_part_crafting"), xMouse, yMouse);
							return true;
						}
					}
				}
			}
		}

		{
			int firstRow = Maths.Floor(gunSelectorScrollOffset);
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
							renderTooltip(pose, lines, xMouse, yMouse);
							return true;
						}
					}
				}
			}
		}
		return false;
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
		int recipeIndex = Maths.Floor(gunSelectorScrollOffset) * GUN_SELECTOR_COLUMNS + relativeIndex;
		if(recipeIndex < GunCraftingEntries.size())
		{
			SelectedGunRecipe = recipeIndex;

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
		// Render the gun before the background so it ends up behind
		if(SelectedGunRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(SelectedGunRecipe);
			if(entry != null)
			{
				pose.pushPose();
				pose.translate(xOrigin + imageWidth + 64f, yOrigin + 64f, 0f);
				pose.mulPose(new Quaternionf()
					.rotateLocalZ(-Maths.PiF * 0.25f)
					.rotateLocalY(Minecraft.getInstance().level.getGameTime() * 0.01f));
				pose.translate(-10f, 0f, 0f);

				ItemStack stack = MinecraftHelpers.CreateStack(entry.outputs[0]);
				RenderGunStack(pose, 0, 0, stack);
				//xOrigin + 126, yOrigin + 31
				pose.popPose();
			}
		}

		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUN_FABRICATION_BG);
		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
						int slotX = xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + x * 20;
						int slotY = yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + y * 30;
						blit(pose, slotX, slotY, getBlitOffset(), 172, 144, 20, 30, TEXTURE_WIDTH, TEXTURE_HEIGHT);



						// Render the "Go to Part Crafting" button
						if(true) // !Def.partCrafting.isActive)
						{
							// Part Crafting Disabled
							blit(pose, slotX + 10, slotY + 20, getBlitOffset(), 182, 185, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}

						TieredIngredientDefinition tieredDef = slotInfo.GetAsTieredDef();
						if(tieredDef != null)
						{
							int tier = tieredDef.GetLowestAllowedTier();
							PartBackgroundType bgType = PartBackgroundType.GetFromTag(tieredDef.tag);
							blit(pose, slotX + 2, slotY + 2, getBlitOffset(), bgType.texX, bgType.texY, 16, 16, TEXTURE_WIDTH, TEXTURE_HEIGHT);
							blit(pose, slotX, slotY + 9, getBlitOffset(), (tier - 1) * 9, 247, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
						}
						else
						{
							IngredientDefinition additionalDef = slotInfo.GetAsAdditionalDef();
							// Render the background shape

							// Render a faded item stack
							// (Defer to later)
						}
					}
					else
					{
						// Cover up the background for this button
						//blit(pose, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + x * 20, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN + y * 20, getBlitOffset(), 229, 72, 18, 28, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					}

				}
			}
		}
		else
		{
			// Render over slots as disabled
		}

		{
			int firstRow = Maths.Floor(gunSelectorScrollOffset);
			for (int row = 0; row < GUN_SELECTOR_ROWS; row++)
			{
				int firstIndexInRow = (firstRow + row) * GUN_SELECTOR_COLUMNS;
				int numEntriesInRow = Maths.Min(GUN_SELECTOR_COLUMNS, GunCraftingEntries.size() - firstIndexInRow);

				blit(pose, xOrigin + GUN_SELECTOR_X_ORIGIN, yOrigin + GUN_SELECTOR_Y_ORIGIN + row * 18, getBlitOffset(), 172, 0, 18 * numEntriesInRow, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			}
		}

		// Render a scrollbar
		int numRows = GunCraftingEntries.size() / GUN_SELECTOR_COLUMNS - GUN_SELECTOR_ROWS + 1;
		RenderScrollbar(pose, xOrigin + GUN_SELECTOR_X_ORIGIN + 18 * GUN_SELECTOR_COLUMNS, yOrigin + GUN_SELECTOR_Y_ORIGIN, 6, 90, gunSelectorScrollOffset, 0, numRows);

		if(SelectedGunRecipe != -1)
		{
			// Render a scrollbar
			int numRecipeRows = CachedSlotInfo.size() / GUN_RECIPE_VIEWER_COLUMNS - GUN_RECIPE_VIEWER_ROWS + 1;
			RenderScrollbar(pose, xOrigin + GUN_RECIPE_VIEWER_X_ORIGIN + 20 * GUN_RECIPE_VIEWER_COLUMNS, yOrigin + GUN_RECIPE_VIEWER_Y_ORIGIN, 6, 30 * GUN_RECIPE_VIEWER_ROWS, recipeSelectorScrollOffset, 0, numRecipeRows);


			// If the player has a gun in hand, we can do a comparison

			int statBoxX = xOrigin + GUN_STATS_X_ORIGIN;
			int statBoxY = yOrigin + GUN_STATS_Y_ORIGIN + 10;

			// Otherwise, just render the bars
			RenderStatComparisonBar(pose, statBoxX, statBoxY, 10, 10, 0, 20, 0);
			RenderStatComparisonBar(pose, statBoxX, statBoxY + 10, 8, 8, 0, 20, 1);
			RenderStatComparisonBar(pose, statBoxX + 31, statBoxY, 6, 6, 0, 20, 2);
			RenderStatComparisonBar(pose, statBoxX + 31, statBoxY + 10, 13, 13, 0, 20, 3);
			RenderStatComparisonBar(pose, statBoxX + 62, statBoxY, 6, 6, 0, 20, 4);
			RenderStatComparisonBar(pose, statBoxX + 62, statBoxY + 10, 13, 13, 0, 20, 5);
		}
	}

	private void RenderGunCraftingFG(PoseStack pose, int xMouse, int yMouse)
	{
		if(SelectedGunRecipe != -1)
		{
			GunCraftingEntryDefinition entry = GunCraftingEntries.get(SelectedGunRecipe);
			if(entry != null)
			{
				ResourceLocation resLoc = new ResourceLocation(entry.outputs[0].item);
				font.draw(pose, Component.translatable("item." + resLoc.getNamespace() + "." + resLoc.getPath()), GUN_STATS_X_ORIGIN, GUN_STATS_Y_ORIGIN, 0x404040);
			}
		}
		else
		{
			font.draw(pose, Component.translatable("crafting.select_a_recipe"),50, 21, 0x404040);
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

/*
						//
						{
							Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
							RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
							RenderSystem.enableBlend();
							RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
							RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
							PoseStack posestack = RenderSystem.getModelViewStack();
							posestack.pushPose();
							posestack.translate((float)GUN_RECIPE_VIEWER_X_ORIGIN + 2 + 20 * x,
										(float)GUN_RECIPE_VIEWER_Y_ORIGIN + 2 + 30 * y,
										100.0F + getBlitOffset());
							posestack.translate(8.0F, 8.0F, 0.0F);
							posestack.scale(1.0F, -1.0F, 1.0F);
							posestack.scale(16.0F, 16.0F, 16.0F);
							RenderSystem.applyModelViewMatrix();
							PoseStack posestack1 = new PoseStack();




							VertexConsumer vc = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.translucent());
							MultiBufferSource.BufferSource multibuffersource$buffersource = //Minecraft.getInstance().renderBuffers().bufferSource();
								MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());


							BakedModel model = itemRenderer.getModel(stack, null, null, 0);
							boolean flag = !model.usesBlockLight();
							if (flag) {
								Lighting.setupForFlatItems();
							}

							itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);

							RenderSystem.enableBlend();
							RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
							RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

							multibuffersource$buffersource.endBatch();
							RenderSystem.enableDepthTest();
							if (flag) {
								Lighting.setupFor3DItems();
							}

							posestack.popPose();
							RenderSystem.applyModelViewMatrix();
						}
*/


						//itemRenderer.renderGuiItem(stack, GUN_RECIPE_VIEWER_X_ORIGIN + 2 + 20 * x, GUN_RECIPE_VIEWER_Y_ORIGIN + 2 + 30 * y);
						//itemRenderer.renderGuiItemDecorations(font, stack, GUN_RECIPE_VIEWER_X_ORIGIN + 2 + 20 * x, GUN_RECIPE_VIEWER_Y_ORIGIN + 2 + 30 * y, null);
					}
				}
			}

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, GUN_FABRICATION_BG);
			for(int y = 0; y < GUN_RECIPE_VIEWER_ROWS; y++)
			{
				for(int x = 0; x < GUN_RECIPE_VIEWER_COLUMNS; x++)
				{
					final int index = (firstRow + y) * GUN_RECIPE_VIEWER_COLUMNS + x;
					if(index < CachedSlotInfo.size())
					{
						//blit(pose, GUN_RECIPE_VIEWER_X_ORIGIN + 2 + 20 * x, GUN_RECIPE_VIEWER_Y_ORIGIN + 2 + 30 * y, getBlitOffset(), 214, 146, 16, 16, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
						itemRenderer.renderGuiItem(stack, GUN_SELECTOR_X_ORIGIN + 1 + 18 * col, GUN_SELECTOR_Y_ORIGIN + 1 + 18 * row);
						itemRenderer.renderGuiItemDecorations(font, stack, GUN_RECIPE_VIEWER_X_ORIGIN + 2 + 20 * col, GUN_RECIPE_VIEWER_Y_ORIGIN + 2 + 20 * row, null);
					}
				}
			}

		}
	}


	// -----------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------- GENERAL METHODS --------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public void RenderPowerBar(PoseStack pose, int x, int y)
	{
		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);

		// Render a power bar
		blit(pose, x, y, getBlitOffset(), 76, 0, 51, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Render the partial fill texture
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;
		int px = (int)(51f * (float)fe / (float)feMax);
		blit(pose, x, y, getBlitOffset(), 76, 13, px, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}
	private void RenderCraftingSlot(PoseStack pose, int x, int y)
	{

	}
	private void RenderStatComparisonBar(PoseStack pose, int x, int y, float value, float compareTo, float minValue, float maxValue, int icon)
	{
		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);

		// Icon
		blit(pose, x, y, getBlitOffset(), 220, 18 + icon * 9, 8, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Empty bar
		blit(pose, x + 8, y, getBlitOffset(), 173, 108, 22, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
			blit(pose, x + 9, y + 2, getBlitOffset(), 174, 119, valuePx, 5, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}


	// Misc
	private void RenderScrollbar(PoseStack pose, int x, int y, int scrollbarPxWidth, int scrollbarPxHeight, float value, float min, float max)
	{
		if(max < 0.0f || Maths.Approx(min, max))
			return;

		RenderSystem.setShaderTexture(0, WORKBENCH_SHARED);

		blit(pose, x, y, getBlitOffset(), 214, 18, scrollbarPxWidth, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		for(int i = 8; i < scrollbarPxHeight; i += 32)
		{
			int tileMin = i;
			int tileMax = Maths.Min(i + 32, scrollbarPxHeight - 8);
			blit(pose, x, y + tileMin, getBlitOffset(), 214, 26, scrollbarPxWidth, tileMax - tileMin, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}
		blit(pose, x, y + scrollbarPxHeight - 8, getBlitOffset(), 214, 82, scrollbarPxWidth, 8, TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
				pose.scale(-32f, 32f, 32f);
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

	private boolean InBox(int xMouse, int yMouse, int x, int w, int y, int h)
	{
		return x <= xMouse && xMouse < x + w
			&& y <= yMouse && yMouse < y + h;
	}
}



























