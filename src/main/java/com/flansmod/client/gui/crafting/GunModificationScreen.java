package com.flansmod.client.gui.crafting;

import com.flansmod.common.crafting.GunModificationMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GunModificationScreen extends AbstractContainerScreen<GunModificationMenu>
{
	public GunModificationScreen(GunModificationMenu menu, Inventory inventory, Component component)
	{
		super(menu, inventory, component);
	}


	@Override
	protected void renderBg(PoseStack p_97787_, float p_97788_, int p_97789_, int p_97790_)
	{

	}
}
