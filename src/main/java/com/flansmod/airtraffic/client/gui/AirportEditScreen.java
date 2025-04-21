package com.flansmod.airtraffic.client.gui;

import com.flansmod.airtraffic.common.AirTrafficControlMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class AirportEditScreen extends Screen
{
	private static final ResourceLocation AIRPORT_EDIT_BG =
		new ResourceLocation(AirTrafficControlMod.MODID, "textures/gui/airport_edit.png");
	private static final int AIRPORT_EDIT_BG_W = 256;
	private static final int AIRPORT_EDIT_BG_H = 256;


	public int xOrigin;
	public int yOrigin;

	public final int imageWidth = 170;
	public final int imageHeight = 116;

	protected AirportEditScreen(@Nonnull Component title)
	{
		super(title);
	}



	@Override
	protected void init() {
		super.init();
		xOrigin = width / 2 - imageWidth / 2;
		yOrigin = height / 2 - imageHeight / 2;

	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int xMouse, int yMouse, float f)
	{
		graphics.blit(AIRPORT_EDIT_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, AIRPORT_EDIT_BG_W, AIRPORT_EDIT_BG_H);

		super.render(graphics, xMouse, yMouse, f);
	}
}
