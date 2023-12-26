package com.flansmod.client.gui;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;

public abstract class FMScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{



	public FMScreen(T menu, Inventory inventory, Component title)
	{
		super(menu, inventory, title);
	}


	@Nonnull
	public <W extends GuiEventListener & Renderable & NarratableEntry> W AddRenderableWidget(@Nonnull W widget) {
		return super.addRenderableWidget(widget);
	}
	@Nonnull
	public <W extends Renderable> W AddRenderableOnly(@Nonnull W widget) {
		return super.addRenderableOnly(widget);
	}
	@Nonnull
	public <W extends GuiEventListener & NarratableEntry> W AddWidget(@Nonnull W widget) {
		return super.addWidget(widget);
	}

	public static boolean InBox(int xMouse, int yMouse, int x, int w, int y, int h)
	{
		return x <= xMouse && xMouse < x + w
			&& y <= yMouse && yMouse < y + h;
	}

}
