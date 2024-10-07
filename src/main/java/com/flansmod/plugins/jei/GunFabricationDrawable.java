package com.flansmod.plugins.jei;

import com.flansmod.common.crafting.ingredients.TieredPartIngredient;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.physics.common.util.Maths;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class GunFabricationDrawable
{
	public static class Background extends FlansDrawable
	{
		private static final int WIDTH = 160;
		private static final int HEIGHT = 48;

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
			graphics.blit(TEXTURE, xOffset, yOffset, 1, 27, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}

		public void DrawExtras(@Nonnull GunFabricationRecipe recipe,
							   @Nonnull IRecipeSlotsView recipeSlotsView,
							   @Nonnull GuiGraphics graphics,
							   double mouseX, double mouseY)
		{
			int numInputs = recipe.InputIngredients.size();
			for(int i = 0; i < numInputs; i++)
			{
				Ingredient input = recipe.InputIngredients.get(i);
				if(input instanceof TieredPartIngredient tiered)
				{
					// Add a tiered background box
					graphics.blit(TEXTURE, 8+22*i, 1, 1+(tiered.MaterialTierMin*22), 95, 22, 47, TEXTURE_W, TEXTURE_H);
					if(InBox(mouseX, mouseY, 8+22*i, 22, 1, 6))
						graphics.renderTooltip(
							Minecraft.getInstance().font,
							Component.translatable("crafting.match_tiers_above", tiered.MaterialTierMin),
							Maths.Floor(mouseX),
							Maths.Floor(mouseY));

					DrawMaterialTypeBox(tiered, EMaterialType.Wood, graphics, 		8+22*i+3, 	27, 3, 145, mouseX, mouseY);
					DrawMaterialTypeBox(tiered, EMaterialType.Glass, graphics, 		8+22*i+9, 	27, 9, 145, mouseX, mouseY);
					DrawMaterialTypeBox(tiered, EMaterialType.Metal, graphics, 		8+22*i+15, 	27, 15, 145, mouseX, mouseY);
					DrawMaterialTypeBox(tiered, EMaterialType.Composite, graphics, 	8+22*i+3, 	32, 3, 150, mouseX, mouseY);
					DrawMaterialTypeBox(tiered, EMaterialType.Electronic, graphics, 8+22*i+9, 	32, 9, 150, mouseX, mouseY);
					DrawMaterialTypeBox(tiered, EMaterialType.Fabric, graphics, 	8+22*i+15, 	32, 15, 150, mouseX, mouseY);
				}
				else
				{
					// Add the default background box
					graphics.blit(TEXTURE, 8+22*i, 1, 1, 95, 22, 47, TEXTURE_W, TEXTURE_H);
				}
			}
		}

		private void DrawMaterialTypeBox(@Nonnull TieredPartIngredient tiered,
										 @Nonnull EMaterialType materialType,
										 @Nonnull GuiGraphics graphics,
										 int x, int y,
										 int iconX, int iconY,
										 double mouseX, double mouseY)
		{

			if(tiered.IsMatchingMaterialType(materialType))
			{
				graphics.blit(TEXTURE, x, y, iconX, iconY, 5, 4, TEXTURE_W, TEXTURE_H);
				if (InBox(mouseX, mouseY, x, 5, y, 4))
					graphics.renderTooltip(
						Minecraft.getInstance().font,
						materialType.ToComponent(),
						Maths.Floor(mouseX),
						Maths.Floor(mouseY));
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
			graphics.blit(TEXTURE, xOffset, yOffset, 16, 200, WIDTH, HEIGHT, TEXTURE_W, TEXTURE_H);
		}
	}
}
