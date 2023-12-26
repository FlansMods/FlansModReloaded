package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.gui.FMScreen;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public abstract class WorkbenchScreenTab<T extends WorkbenchMenu> extends FMScreen<T>
{
	@Nonnull
	protected final T Workbench;
	@Nonnull
	protected final Font font;
	protected boolean IsActive;
	public int xOrigin;
	public int yOrigin;


	public WorkbenchScreenTab(@Nonnull T menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
		font = Minecraft.getInstance().font;
		Workbench = menu;
		IsActive = false;

		imageWidth = 172;
		imageHeight = 217;

		titleLabelY = -1000;
		inventoryLabelX = 6;
		inventoryLabelY = 124;
	}

	@Override
	protected void init()
	{
		xOrigin = width / 2 - imageWidth / 2;
		yOrigin = height / 2 - imageHeight / 2;

		if(IsTabPresent())
			InitTab();
		else
			Minecraft.getInstance().setScreen(null);


		// Add tabs?
		//if(Tabs.size() > 1)
		//{
		//	int tabPosition = 0;
		//	for (WorkbenchScreenTab tab : Tabs)
		//	{
		//		if (tabPosition >= MAX_TABS_PER_SIDE)
		//		{
		//			addWidget(Button.builder(Component.empty(),
		//					(t) -> { SelectTab(tab); })
		//				.bounds(xOrigin + imageWidth, yOrigin + 20 + (tabPosition - MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30, 30)
		//				.build());
		//		} else
		//		{
		//			addWidget(Button.builder(Component.empty(),
		//					(t) -> { SelectTab(tab); })
		//				.bounds(xOrigin - 30, yOrigin + 20 + tabPosition * DISTANCE_BETWEEN_TABS, 30, 30)
		//				.build());
		//		}
		//		tabPosition++;
		//	}
		//}
	}

	@Override
	protected void containerTick() { UpdateTab(true); }
	@Override
	public boolean mouseScrolled(double x, double y, double scroll) { return OnMouseScroll(Maths.Floor(x), Maths.Floor(y), scroll); }
	@Override
	protected void renderBg(@Nonnull GuiGraphics graphics, float f, int xMouse, int yMouse)
	{
		graphics.pose().pushPose();
		//RenderSharedBG(graphics, xMouse, yMouse);
		RenderBG(graphics, xMouse, yMouse);
		graphics.pose().popPose();
	}
	@Override
	public void render(@Nonnull GuiGraphics graphics, int xMouse, int yMouse, float f)
	{
		super.render(graphics, xMouse, yMouse, f);

		if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem())
		{
			super.renderTooltip(graphics, xMouse, yMouse);
			return;
		}

		//if(Tabs.size() > 1)
		//{
		//	int tabPosition = 0;
		//	for (WorkbenchScreenTab availableTab : Tabs)
		//	{
		//		boolean rightSide = tabPosition <= MAX_TABS_PER_SIDE;
		//		if(rightSide)
		//		{
		//			if (InBox(xMouse, yMouse, xOrigin - 30, 30, yOrigin + 20 + (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30))
		//			{
		//				graphics.renderTooltip(font, availableTab.GetTitle(), xMouse, yMouse);
		//				return;
		//			}
		//		}
		//		else
		//		{
		//			if (InBox(xMouse, yMouse, xOrigin + imageWidth, 30, yOrigin + 20 + (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, 30))
		//			{
		//				graphics.renderTooltip(font, availableTab.GetTitle(), xMouse, yMouse);
		//				return;
		//			}
		//		}
		//		tabPosition++;
		//	}
		//}

		RenderTooltip(graphics, xMouse, yMouse);
	}
	@Override
	protected void renderLabels(@Nonnull GuiGraphics graphics, int x, int y)
	{
		super.renderLabels(graphics, x, y);

		graphics.pose().pushPose();
		// Render Shared FG
		{
			//if Banner == null
			{
				int xPos = 5;
				if(GetWidth() > 172)
					xPos = 97;
				graphics.drawString(font, GetTitle(), xPos, 5, 0x505050);
			}

			// Render tabs over BG
			//if(Tabs.size() > 1)
			//{
			//	int tabPosition = 0;
			//	for (WorkbenchScreenTab availableTab : Tabs)
			//	{
			//		RenderTabButton(graphics, 0, 0, availableTab, (tabPosition % MAX_TABS_PER_SIDE) * DISTANCE_BETWEEN_TABS, tabPosition <= MAX_TABS_PER_SIDE);
			//		tabPosition++;
			//	}
			//}
		}
		RenderFG(graphics, x, y);
		graphics.pose().popPose();
	}



	@Nonnull
	protected abstract Component GetTitle();
	protected abstract boolean IsTabPresent();
	protected abstract void InitTab();
	protected abstract void OnTabSelected(boolean selected);
	protected abstract void UpdateTab(boolean selected);
	protected abstract boolean OnMouseScroll(int xMouse, int yMouse, double scroll);
	protected int GetWidth() { return 172; }
	protected int GetHeight() { return 217; }
	protected abstract boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse);
	protected abstract void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse);
	protected abstract void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse);

	// -----------------------------------------------------------------------------------------------
	//   Helpers
	// -----------------------------------------------------------------------------------------------
	private final ResourceLocation FM_SHARED_GUI = new ResourceLocation(FlansMod.MODID, "textures/gui/workbench_shared.png");
	private final int SHARED_W = 256;
	private final int SHARED_H = 256;

	public void RenderPowerBarBG(@Nonnull GuiGraphics graphics, int x, int y, int fe, int feMax)
	{
		// Render a power bar
		graphics.blit(FM_SHARED_GUI, x, y, 76, 0, 51, 12, SHARED_W, SHARED_H);
		// Render the partial fill texture
		int px = (int)(51f * (float)fe / (float)feMax);
		graphics.blit(FM_SHARED_GUI, x, y, 76, 13, px, 12, SHARED_W, SHARED_H);
	}
	public void RenderPowerBarFG(@Nonnull GuiGraphics graphics, int x, int y, int fe, int feMax)
	{
		String storedEnergyAmount = MinecraftHelpers.GetFEString(fe) + " / " + MinecraftHelpers.GetFEString(feMax);
		graphics.drawString(font, storedEnergyAmount, Maths.Floor(x - (font.width(storedEnergyAmount) / 2f)), y, 0x505050);
	}
	protected void RenderGUIItem(@Nonnull GuiGraphics graphics, int x, int y, @Nonnull ItemStack stack, boolean withText)
	{
		graphics.renderItem(stack, x, y);
		if(withText)
			graphics.renderItemDecorations(font, stack, x, y);
	}
	protected void Render3DGun(@Nonnull GuiGraphics graphics, int x, int y, float yaw, float pitch, @Nonnull ItemStack stack)
	{
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0f);
		graphics.pose().mulPose(new Quaternionf()
			.rotateLocalZ(pitch * Maths.DegToRadF)
			.rotateLocalY(yaw * Maths.DegToRadF));
		graphics.pose().translate(-10f, 0f, 0f);

		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(stack);
		if(gunRenderer != null)
		{
			graphics.pose().pushPose();
			{
				Lighting.setupForEntityInInventory();
				MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
				graphics.pose().translate(x + 8, y + 12, 0f); // TODO: This was blit offset?
				graphics.pose().scale(-32f, 32f, 32f);
				graphics.pose().mulPose(new Quaternionf().rotateLocalX(Maths.PiF));
				graphics.pose().mulPose(new Quaternionf().rotateLocalY(Maths.PiF));
				graphics.pose().translate(-0.5f, -0.5f, -0.5f);
				gunRenderer.RenderDirect(null, stack, new RenderContext(
					buffers,
					ItemDisplayContext.GROUND,
					graphics.pose(),
					0xf000f0,
					0
				));
				buffers.endBatch();
				Lighting.setupFor3DItems();
			}
			graphics.pose().popPose();
		}

		//xOrigin + 126, yOrigin + 31
		graphics.pose().popPose();
	}
	protected void RenderStatComparisonBar(@Nonnull GuiGraphics graphics, int x, int y, float value, float compareTo, float minValue, float maxValue, int icon)
	{
		// Icon
		graphics.blit(FM_SHARED_GUI, x, y, 220, 18 + icon * 9, 8, 8, SHARED_W, SHARED_H);
		// Empty bar
		graphics.blit(FM_SHARED_GUI,x + 8, y, 173, 108, 22, 9, SHARED_W, SHARED_H);

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
			graphics.blit(FM_SHARED_GUI, x + 9, y + 2, 174, 119, valuePx, 5, SHARED_W, SHARED_H);
		}
	}
	protected void RenderScrollbar(@Nonnull GuiGraphics graphics, int x, int y, int scrollbarPxWidth, int scrollbarPxHeight, float value, float min, float max)
	{
		if(max < 0.0f || Maths.Approx(min, max))
			return;

		graphics.blit(FM_SHARED_GUI, x, y, 214, 18, scrollbarPxWidth, 8, SHARED_W, SHARED_H);
		for(int i = 8; i < scrollbarPxHeight; i += 32)
		{
			int tileMin = i;
			int tileMax = Maths.Min(i + 32, scrollbarPxHeight - 8);
			graphics.blit(FM_SHARED_GUI, x, y + tileMin, 214, 26, scrollbarPxWidth, tileMax - tileMin, SHARED_W, SHARED_H);

		}
		graphics.blit(FM_SHARED_GUI, x, y + scrollbarPxHeight - 8, 214, 82, scrollbarPxWidth, 8, SHARED_W, SHARED_H);

		int scrollbarMinSize = 8 + 8;

		float parametric = value / (max - min);
		float scrollbarPxSize = Maths.Max(scrollbarPxHeight / (max - min), scrollbarMinSize);

		float minPx = Maths.LerpF(0, scrollbarPxHeight - scrollbarPxSize, parametric);
		float maxPx = Maths.LerpF(scrollbarPxSize, scrollbarPxHeight, parametric);

		graphics.blit(FM_SHARED_GUI, x, y + Maths.Floor(minPx), 208, 18, scrollbarPxWidth, Maths.Floor(scrollbarPxSize) - 8, SHARED_W, SHARED_H);
		graphics.blit(FM_SHARED_GUI, x, y + Maths.Floor(maxPx) - 8, 208, 82, scrollbarPxWidth, 8, SHARED_W, SHARED_H);
	}
	protected boolean NetworkedButtonPress(int buttonID)
	{
		if(Minecraft.getInstance().gameMode == null || Minecraft.getInstance().player == null)
			return false;
		Workbench.clickMenuButton(Minecraft.getInstance().player, buttonID);
		Minecraft.getInstance().gameMode.handleInventoryButtonClick(Workbench.containerId, buttonID);
		return true;
	}
}
