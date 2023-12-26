package com.flansmod.client.gui.crafting;

import com.flansmod.client.gui.FMScreen;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.crafting.menus.WorkbenchMenuMaterials;
import com.flansmod.common.crafting.menus.WorkbenchMenuModification;
import com.flansmod.util.Maths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class WorkbenchScreenTabMaterials extends WorkbenchScreenTab<WorkbenchMenuMaterials>
{
	private static final ResourceLocation MATERIALS_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/materials.png");
	private static final int MATERIALS_W = 256;
	private static final int MATERIALS_H = 256;

	private static final int SLOTS_PER_ROW = 9;
	private static final int MAX_ROWS = 5;

	private static final int SLOTS_ORIGIN_X = 5;
	private static final int SLOTS_ORIGIN_Y = 22;

	public WorkbenchScreenTabMaterials(@Nonnull WorkbenchMenuMaterials menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
	}

	@Override
	protected boolean IsTabPresent() { return Workbench.Def.itemHolding.slots.length > 0; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_materials"); }
	@Override
	protected void InitTab() {}
	@Override
	protected void OnTabSelected(boolean selected) {}
	@Override
	protected void UpdateTab(boolean selected) {}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll) { return false; }

	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		return false;
	}

	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		graphics.blit(MATERIALS_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, MATERIALS_W, MATERIALS_H);
		for(int j = 0; j < MAX_ROWS; j++)
		{
			int numSlotsOnThisRow = Maths.Min(Workbench.MaterialContainer.getContainerSize() - j * SLOTS_PER_ROW, SLOTS_PER_ROW);
			if (numSlotsOnThisRow > 0)
			{
				graphics.blit(MATERIALS_BG, xOrigin + SLOTS_ORIGIN_X, yOrigin + SLOTS_ORIGIN_Y + 18 * j, 5, 136, 18 * numSlotsOnThisRow, 18, MATERIALS_W, MATERIALS_H);
			}
		}
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{

	}
}
