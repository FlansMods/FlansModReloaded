package com.flansmod.packs.basics.client;

import com.flansmod.packs.basics.BasicPartsMod;
import com.flansmod.packs.basics.common.DistillationTowerBlockEntity;
import com.flansmod.packs.basics.common.DistillationTowerMenu;
import com.flansmod.physics.common.util.Maths;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class DistillationTowerScreen extends AbstractContainerScreen<DistillationTowerMenu>
{
	private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(BasicPartsMod.MODID, "textures/gui/distillation.png");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;
	public final DistillationTowerMenu DistillationTower;

	private static final Component SCREEN_TITLE = Component.translatable("distillation.menu.title");
	private static final Component HINT_TOP_NOT_INSTALLED = Component.translatable("distillation.menu.top_not_installed");
	private static final Component HINT_STACKS_NOT_INSTALLED = Component.translatable("distillation.menu.stacks_not_installed");
	private static final Component HINT_TIER_2_OUTPUT_AVAILABLE = Component.translatable("distillation.menu.extra_output_available.2");
	private static final Component HINT_TIER_3_OUTPUT_AVAILABLE = Component.translatable("distillation.menu.extra_output_available.3");


	public DistillationTowerScreen(DistillationTowerMenu menu, Inventory inventory, Component component)
	{
		super(menu, inventory, component);
		DistillationTower = menu;

		imageWidth = 172;
		imageHeight = 217;
		titleLabelX = 6;
		titleLabelY = 6;
		inventoryLabelX = 6;
		inventoryLabelY = 124;
	}

	@Override
	protected void init()
	{
		super.init();
		int xOrigin = width / 2 - imageWidth / 2;
		int yOrigin = height / 2 - imageHeight / 2;

	}

	@Override
	protected void renderBg(@Nonnull GuiGraphics graphics, float f, int x, int y)
	{
		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);

		graphics.blit(BACKGROUND_LOCATION, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		DistillationTowerBlockEntity[] distillerStack = DistillationTower.Distiller.GetStack();
		DistillationTowerBlockEntity topDistiller = distillerStack[0];

		// Hint warning labels
		if(topDistiller == null || !topDistiller.IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 102, yOrigin + 13, 207, 176, 49, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		else if(distillerStack[1] == null || distillerStack[1].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 102, yOrigin + 38, 207, 196, 49, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		else if(distillerStack[2] == null || distillerStack[2].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 102, yOrigin + 58,207, 216, 49, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		else if(distillerStack[3] == null || distillerStack[3].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 102, yOrigin + 78,207, 236, 49, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Hide the input if there is no top on the stack
		if(topDistiller == null || !topDistiller.IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 10, yOrigin + 18, 172, 101, 47, 68, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Hide output 1 if there is no output in the stack
		if(distillerStack[1] == null || distillerStack[1].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 43, yOrigin + 39, 172, 39, 53, 19, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Hide output 2 if the stack is only 1 high
		if(distillerStack[2] == null || distillerStack[2].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 43, yOrigin + 58, 172, 58, 53, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		// Hide output 3 if the stack is 1 or 2 high
		if(distillerStack[3] == null || distillerStack[3].IsTop)
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 43, yOrigin + 78, 172, 78, 53, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		if(topDistiller != null)
		{
			// If distillation is in progress, render progress bars
			int distillTotalTime = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_DISTILLING_TOTAL_TIME);
			int distillProgress = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_DISTILLING_PROGRESS);
			float craftProportion = distillTotalTime > 0 ? (float)distillProgress / (float)distillTotalTime : 0f;
			for(int i = 0; i < DistillationTowerBlockEntity.MAX_DISTILLATION_STACK_HEIGHT; i++)
				if(topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_HAS_VALID_RECIPE_AT_DEPTH_1 + i) != 0)
				{
					graphics.blit(BACKGROUND_LOCATION, xOrigin + 64, yOrigin + 44 + 20 * i, 27, 221, Maths.ceil(12 * craftProportion), 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				}

			// If fuel is being added, render the burn time
			int litTotalTime = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_LIT_DURATION);
			int litProgress = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_LIT_TIME);
			float burnProgress = litTotalTime > 0 ? ((float)litProgress / (float)litTotalTime) : 0f;
			int burnPx = Maths.ceil(11f * burnProgress);
			graphics.blit(BACKGROUND_LOCATION, xOrigin + 19, yOrigin + 64 - burnPx, 7, 232 - burnPx, 9, burnPx, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int xMouse, int yMouse, float f)
	{
		super.render(graphics, xMouse, yMouse, f);
		if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem())
		{
			super.renderTooltip(graphics, xMouse, yMouse);
		}
		else
		{
			int xOrigin = (width - imageWidth) / 2;
			int yOrigin = (height - imageHeight) / 2;

			int x = xMouse - xOrigin;
			int y = yMouse - yOrigin;

			if(99 <= x && x < 139)
			{
				DistillationTowerBlockEntity[] distillerStack = DistillationTower.Distiller.GetStack();
				DistillationTowerBlockEntity topDistiller = distillerStack[0];

				if(topDistiller == null || !topDistiller.IsTop)
				{
					if (20 <= y && y < 40)
						graphics.renderTooltip(font, HINT_TOP_NOT_INSTALLED, xMouse, yMouse);
				}
				else if(distillerStack[1] == null)
				{
					if (40 <= y && y < 60)
						graphics.renderTooltip(font, HINT_STACKS_NOT_INSTALLED, xMouse, yMouse);
				}
				else if(distillerStack[2] == null)
				{
					if (60 <= y && y < 80)
						graphics.renderTooltip(font, HINT_TIER_2_OUTPUT_AVAILABLE, xMouse, yMouse);
				}
				else if(distillerStack[3] == null)
				{
					if (80 <= y && y < 100)
						graphics.renderTooltip(font, HINT_TIER_3_OUTPUT_AVAILABLE, xMouse, yMouse);
				}
			}
		}
	}

	@Override
	protected void renderLabels(@Nonnull GuiGraphics graphics, int x, int y)
	{
		super.renderLabels(graphics, x, y);

		DistillationTowerBlockEntity[] distillerStack = DistillationTower.Distiller.GetStack();


		//// If this stack lacks a top piece, hint the player
		//if(distillerStack[0] == null)
		//	font.draw(pose, HINT_TOP_NOT_INSTALLED, 62, 25, 0x404040);
//
		//// If this stack lacks any stack pieces, hint the player
		//if(distillerStack[1] == null)
		//	font.draw(pose, HINT_STACKS_NOT_INSTALLED, 99, 45, 0x404040);
//
		//// If this stack is capped at 1 and there is a possible output at 2, hint
		//if(distillerStack[2] == null)
		//	font.draw(pose, HINT_TIER_2_OUTPUT_AVAILABLE, 99, 65, 0x404040);
//
		//// If this stack is capped at 2 and there is a possible output at 3, hint
		//if(distillerStack[3] == null)
		//	font.draw(pose, HINT_TIER_3_OUTPUT_AVAILABLE, 99, 85, 0x404040);
	}
}
