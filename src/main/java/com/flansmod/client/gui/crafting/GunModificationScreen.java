package com.flansmod.client.gui.crafting;

import com.flansmod.client.gui.poser.AnimationPoserScreen;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.GunModificationMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GunModificationScreen extends AbstractContainerScreen<GunModificationMenu>
{

	private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int BACKGROUD_WIDTH = 512;
	private static final int BACKGROUD_HEIGHT = 256;

	private enum Tab
	{
		MATERIALS,


		BARREL,
		GRIP,
		MAGAZINE,
		SCOPE,
		STOCK,
	}
	private static final Component[] TAB_TITLES = new Component[]{
		Component.translatable("gui.gun_modification_table.tab_materials"),
		Component.translatable("gui.gun_modification_table.tab_barrel"),
		Component.translatable("gui.gun_modification_table.tab_grip"),
		Component.translatable("gui.gun_modification_table.tab_magazine"),
		Component.translatable("gui.gun_modification_table.tab_scope"),
		Component.translatable("gui.gun_modification_table.tab_stock")
	};

	private Tab SelectedTab = Tab.MATERIALS;


	public GunModificationScreen(GunModificationMenu menu, Inventory inventory, Component component)
	{
		super(menu, inventory, component);

		imageWidth = 256;
		imageHeight = 208;

		titleLabelY = -1000;
		inventoryLabelX = 48;
		inventoryLabelY = 118;
	}

	@Override
	protected void init()
	{
		super.init();
		int x = width / 2 - imageWidth / 2;
		int y = height / 2 - imageHeight / 2;

		for(int i = 0; i < GunModificationScreen.Tab.values().length; i++)
		{
			final int index = i;
			addWidget(Button.builder(TAB_TITLES[i],
				(t) -> { SelectTab(Tab.values()[index]);})
				.bounds(x + 2 + 28*i, y + 0, 28, 18)
				.build());
		}


	}

	private void SelectTab(GunModificationScreen.Tab tab)
	{
		SelectedTab = tab;
	}


	@Override
	protected void renderBg(PoseStack pose, float f, int x, int y)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);

		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		blit(pose, i, j, getBlitOffset(), 0, 0, imageWidth, imageHeight, BACKGROUD_WIDTH, BACKGROUD_HEIGHT);

		blit(pose, i + 3 + 28*SelectedTab.ordinal(), j + 16, getBlitOffset(), 263, 165, 26, 4, BACKGROUD_WIDTH, BACKGROUD_HEIGHT);

		switch(SelectedTab)
		{
			case MATERIALS ->
			{
				// Change background to be slots
				blit(pose, i + 5, j + 22, getBlitOffset(), 47, 127, 162, 54, BACKGROUD_WIDTH, BACKGROUD_HEIGHT);
			}
			case GRIP ->
			{

			}
		}
	}
}
