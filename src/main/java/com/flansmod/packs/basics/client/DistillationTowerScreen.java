package com.flansmod.packs.basics.client;

import com.flansmod.packs.basics.BasicPartsMod;
import com.flansmod.packs.basics.common.DistillationTowerBlockEntity;
import com.flansmod.packs.basics.common.DistillationTowerMenu;
import com.flansmod.util.Maths;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

		titleLabelY = -1000;
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
	protected void renderBg(PoseStack pose, float f, int x, int y)
	{
		int xOrigin = (width - imageWidth) / 2;
		int yOrigin = (height - imageHeight) / 2;

		// Render the background image
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);

		blit(pose, xOrigin, yOrigin, getBlitOffset(), 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		DistillationTowerBlockEntity[] distillerStack = DistillationTower.Distiller.GetStack();
		DistillationTowerBlockEntity topDistiller = distillerStack[0];

		// Hide the input if there is no top on the stack
		if(topDistiller == null)
			blit(pose, xOrigin + 10, yOrigin + 18, getBlitOffset(), 172, 101, 47, 68, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Hide output 1 if there is no output in the stack
		if(distillerStack[1] == null)
			blit(pose, xOrigin + 43, yOrigin + 39, getBlitOffset(), 172, 39, 53, 19, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Hide output 2 if the stack is only 1 high
		if(distillerStack[2] == null)
			blit(pose, xOrigin + 43, yOrigin + 58, getBlitOffset(), 172, 58, 53, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Hide output 3 if the stack is 1 or 2 high
		if(distillerStack[3] == null)
			blit(pose, xOrigin + 43, yOrigin + 78, getBlitOffset(), 172, 78, 53, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		if(topDistiller != null)
		{
			// If distillation is in progress, render progress bars
			int distillTotalTime = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_DISTILLING_TOTAL_TIME);
			int distillProgress = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_DISTILLING_PROGRESS);
			float craftProportion = distillTotalTime > 0 ? (float)distillProgress / (float)distillTotalTime : 0f;
			for(int i = 0; i < DistillationTowerBlockEntity.MAX_DISTILLATION_STACK_HEIGHT; i++)
				if(topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_HAS_VALID_RECIPE_AT_DEPTH_1 + i) != 0)
				{
					blit(pose, xOrigin + 64, yOrigin + 44 + 20 * i, getBlitOffset(), 27, 221, Maths.Ceil(12 * craftProportion), 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				}

			// If fuel is being added, render the burn time
			int litTotalTime = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_LIT_DURATION);
			int litProgress = topDistiller.DataAccess.get(DistillationTowerBlockEntity.DATA_LIT_TIME);
			float burnProgress = litTotalTime > 0 ? ((float)litProgress / (float)litTotalTime) : 0f;
			int burnPx = Maths.Ceil(11f * burnProgress);
			blit(pose, xOrigin + 19, yOrigin + 64 - burnPx, getBlitOffset(), 7, 232 - burnPx, 9, burnPx, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	@Override
	public void render(PoseStack pose, int xMouse, int yMouse, float f)
	{
		super.render(pose, xMouse, yMouse, f);
		if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem())
		{
			super.renderTooltip(pose, xMouse, yMouse);
		}
	}

	@Override
	protected void renderLabels(PoseStack pose, int x, int y)
	{
		super.renderLabels(pose, x, y);

		DistillationTowerBlockEntity[] distillerStack = DistillationTower.Distiller.GetStack();


		// If this stack lacks a top piece, hint the player
		if(distillerStack[0] == null)
			font.draw(pose, HINT_TOP_NOT_INSTALLED, 62, 25, 0x404040);

		// If this stack lacks any stack pieces, hint the player
		if(distillerStack[1] == null)
			font.draw(pose, HINT_STACKS_NOT_INSTALLED, 99, 45, 0x404040);

		// If this stack is capped at 1 and there is a possible output at 2, hint
		if(distillerStack[2] == null)
			font.draw(pose, HINT_TIER_2_OUTPUT_AVAILABLE, 99, 65, 0x404040);

		// If this stack is capped at 2 and there is a possible output at 3, hint
		if(distillerStack[3] == null)
			font.draw(pose, HINT_TIER_3_OUTPUT_AVAILABLE, 99, 85, 0x404040);
	}
}
