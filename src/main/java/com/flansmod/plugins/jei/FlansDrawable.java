package com.flansmod.plugins.jei;

import com.flansmod.client.gui.crafting.WorkbenchScreenTabPartCrafting;
import com.flansmod.common.FlansMod;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public abstract class FlansDrawable implements IDrawable
{
	protected static final int TEXTURE_W = 256;
	protected static final int TEXTURE_H = 256;
	protected static final ResourceLocation TEXTURE = new ResourceLocation(FlansMod.MODID, "textures/gui/jei_backgrounds.png");

	// Helper methods
	public static boolean InBox(double xMouse, double yMouse, int x, int w, int y, int h)
	{
		return x <= xMouse && xMouse < x + w
			&& y <= yMouse && yMouse < y + h;
	}
}
