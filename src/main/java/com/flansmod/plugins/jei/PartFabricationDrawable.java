package com.flansmod.plugins.jei;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nonnull;

public class PartFabricationDrawable
{
	public static class Background extends FlansDrawable
	{
		@Override
		public int getWidth() { return 128; }
		@Override
		public int getHeight() { return 128; }
		@Override
		public void draw(@Nonnull PoseStack poseStack, int xOffset, int yOffset)
		{

		}
	}

	public static class Icon extends FlansDrawable
	{
		@Override
		public int getWidth() { return 16; }
		@Override
		public int getHeight() { return 16; }
		@Override
		public void draw(@Nonnull PoseStack poseStack, int xOffset, int yOffset)
		{

		}
	}
}
