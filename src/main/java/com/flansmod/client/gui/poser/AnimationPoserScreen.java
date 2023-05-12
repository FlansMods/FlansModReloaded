package com.flansmod.client.gui.poser;

import com.flansmod.common.FlansMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AnimationPoserScreen extends Screen
{
	protected static final ResourceLocation ANIM_POSER_TEXTURE = new ResourceLocation(FlansMod.MODID, "textures/gui/anim_poser.png");
	public enum Tab
	{
		KEYFRAME_POSER,
		ANIMATION_BUILDER,
		PREVIEW,
		EXPORT,
	}

	private Tab CurrentTab = Tab.KEYFRAME_POSER;
	private Button[] TabSelectorButtons = null;
	private static final Component[] TAB_TITLES = new Component[]{
		Component.translatable("gui.animPoser.tab_keyframe_poser"),
		Component.translatable("gui.animPoser.tab_animation_builder"),
		Component.translatable("gui.animPoser.tab_preview"),
		Component.translatable("gui.animPoser.tab_export")
	};
	private static final Component TAB_HIDDEN = Component.translatable("gui.socialInteractions.tab_hidden");
	private static final Component TAB_BLOCKED = Component.translatable("gui.socialInteractions.tab_blocked");

	protected AnimationPoserScreen()
	{
		super(Component.translatable("gui.animPoser.title"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick()
	{
		super.tick();
	}

	@Override
	protected void init()
	{
		TabSelectorButtons = new Button[Tab.values().length];
		int xWidth = width / Tab.values().length;
		for(int i = 0; i < Tab.values().length; i++)
		{
			final int index = i;
			TabSelectorButtons[i] = addRenderableWidget(
				Button.builder(TAB_TITLES[i], (t) -> { SelectTab(Tab.values()[index]); })
					.bounds(width * i, width, 0, 20)
					.build());
		}

	}

	private void SelectTab(Tab tab)
	{
		CurrentTab = tab;
	}


	// ---- Keyframe Poser Tab ----
	private Button AddNewButton;



}
