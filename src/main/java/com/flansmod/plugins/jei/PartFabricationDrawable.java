package com.flansmod.plugins.jei;

import com.flansmod.client.gui.crafting.WorkbenchScreenTabPartCrafting;
import com.flansmod.common.crafting.PartFabricationRecipe;
import com.flansmod.common.crafting.TieredMaterialIngredient;
import com.flansmod.util.Maths;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PartFabricationDrawable
{
	public static class Background extends FlansDrawable
	{
		private static final int WIDTH = 160;
		private static final int HEIGHT = 25;

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
			graphics.blit(TEXTURE, xOffset, yOffset, 1, 1, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}
		public void DrawExtras(@Nonnull PartFabricationRecipe recipe,
							   @Nonnull IRecipeSlotsView recipeSlotsView,
							   @Nonnull GuiGraphics graphics,
							   double mouseX, double mouseY)
		{

			int numInputs = recipe.InputIngredients.size();
			int[] numRequired = recipe.GetRequiredOfEachIngredient();

			for(int i = 0; i < numInputs; i++)
			{
				graphics.blit(TEXTURE, 4 + 26*i, 4, 162, 5, (i == numInputs - 1) ? 20 : 26, 20, TEXTURE_W, TEXTURE_H);

				Optional<ItemStack> previewStack = recipeSlotsView.getSlotViews().get(i).getDisplayedItemStack();
				Ingredient ingredient = recipe.InputIngredients.get(i);
				if(previewStack.isPresent() && ingredient instanceof TieredMaterialIngredient tiered)
				{
					int countPerItem = tiered.Count(previewStack.get());
					if(countPerItem > 0)
					{
						int numNeeded = numRequired[i] / countPerItem;
						int numRemainder = numRequired[i] % countPerItem;

						graphics.pose().pushPose();
						graphics.pose().translate(0f, 0f, 300f);
						graphics.drawString(Minecraft.getInstance().font,
							Component.literal(numRemainder > 0 ? "(" + (numNeeded + 1) + ")" : "" + numNeeded),
							6 + 26 * i, 15,
							0xffffff);
						graphics.pose().popPose();
					}
				}
			}
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
			graphics.blit(TEXTURE, xOffset, yOffset, 0, 200, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}
	}
}