package com.flansmod.packs.basics.plugins.jei;

import com.flansmod.common.crafting.ingredients.TieredPartIngredient;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.packs.basics.common.DistillationRecipe;
import com.flansmod.plugins.jei.FlansDrawable;
import com.flansmod.util.Maths;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class DistillationDrawable
{
	public static class Background extends FlansDrawable
	{
		private static final int WIDTH = 87;
		private static final int HEIGHT = 79;
		public Background()
		{
			super();
		}

		@Override
		public int getWidth() { return WIDTH; }
		@Override
		public int getHeight() { return HEIGHT; }
		@Override
		public void draw(@Nonnull GuiGraphics graphics, int xOffset, int yOffset)
		{
			graphics.blit(TEXTURE, xOffset, yOffset, 162, 27, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}

		public void DrawExtras(@Nonnull DistillationRecipe recipe,
							   @Nonnull IRecipeSlotsView recipeSlotsView,
							   @Nonnull GuiGraphics graphics,
							   double mouseX, double mouseY)
		{
			int maxDepth = 1;
			for(IRecipeSlotView slotView : recipeSlotsView.getSlotViews())
			{
				String slotName = slotView.getSlotName().orElse("");
				if(slotName.startsWith("output_"))
				{
					int depth = slotName.charAt(slotName.length() - 1) - '0';
					if (depth > maxDepth)
						maxDepth = depth;
				}
			}
			graphics.blit(TEXTURE, 34, 20, 219, 140, 16, 16 + (20 * (maxDepth - 1)), TEXTURE_W, TEXTURE_H);
		}
	}

	public static class Icon extends FlansDrawable
	{
		private static final int WIDTH = 16;
		private static final int HEIGHT = 16;

		@Override
		public int getWidth() { return WIDTH; }
		@Override
		public int getHeight() { return HEIGHT; }
		@Override
		public void draw(@Nonnull GuiGraphics graphics, int xOffset, int yOffset)
		{
			graphics.blit(TEXTURE, xOffset, yOffset, 16, 200, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}
	}

}