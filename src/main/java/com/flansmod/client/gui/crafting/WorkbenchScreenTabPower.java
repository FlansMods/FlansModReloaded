package com.flansmod.client.gui.crafting;

import com.flansmod.client.gui.FMScreen;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.crafting.menus.WorkbenchMenuModification;
import com.flansmod.common.crafting.menus.WorkbenchMenuPower;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class WorkbenchScreenTabPower extends WorkbenchScreenTab<WorkbenchMenuPower>
{
	private static final ResourceLocation POWER_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/power.png");
	private final int POWER_W = 256;
	private final int POWER_H = 256;

	public WorkbenchScreenTabPower(@Nonnull WorkbenchMenuPower menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
	}
	@Override
	protected boolean IsTabPresent()
	{
		return Workbench.BatteryContainer.getContainerSize() > 0 ||
			Workbench.FuelContainer.getContainerSize() > 0;
	}
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_power"); }
	@Override
	protected void InitTab()
	{

	}
	@Override
	protected void OnTabSelected(boolean enable)
	{

	}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll)
	{
		return false;
	}
	@Override
	protected void UpdateTab(boolean enabled)
	{

	}
	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		return false;
	}
	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		graphics.blit(POWER_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, POWER_W, POWER_H);

		// Render a fuel slot
		if(Workbench.FuelContainer.getContainerSize() > 0)
		{
			int litTime = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_TIME);
			int litDuration = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_LIT_DURATION);
			int px = (int) (11f * (float) litTime / (float) litDuration);
			graphics.blit(POWER_BG, xOrigin + 132, yOrigin + 51 + 11 - px, 344, 213 + 11 - px, 9, px, POWER_W, POWER_H);
		}
		else // Cover up the fuel slot
		{
			graphics.blit(POWER_BG, xOrigin + 125, yOrigin + 65, 24, 65, 18, 34, POWER_W, POWER_H);
		}

		// Render a battery slot
		if(Workbench.BatteryContainer.getContainerSize() > 0)
		{
			graphics.blit(POWER_BG, xOrigin + 77, yOrigin + 65, 244, 232, 18, 18, POWER_W, POWER_H);
		}
		else // Cover up the battery slot
		{
			graphics.blit(POWER_BG, xOrigin + 74, yOrigin + 81, 24, 81, 18, 18, POWER_W, POWER_H);
		}

		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;

		RenderPowerBarBG(graphics, xOrigin + 58, yOrigin + 52, fe, feMax);
	}
	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		int fe = Workbench.WorkbenchData.get(WorkbenchBlockEntity.DATA_FORGE_ENERGY);
		int feMax = Workbench.Def.energy.maxFE;

		RenderPowerBarFG(graphics, imageWidth / 2, 23, fe, feMax);
	}
}
