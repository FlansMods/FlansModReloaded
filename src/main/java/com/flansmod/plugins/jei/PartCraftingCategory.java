package com.flansmod.plugins.jei;

import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class PartCraftingCategory extends FlansCraftingCategory<PartFabricationRecipe>
{
	private final PartFabricationDrawable.Background BackgroundDrawable;
	private final PartFabricationDrawable.Icon IconDrawable;

	public PartCraftingCategory(RecipeType<PartFabricationRecipe> recipeType)
	{
		super(recipeType);

		BackgroundDrawable = new PartFabricationDrawable.Background();
		IconDrawable = new PartFabricationDrawable.Icon();
	}

	@Override
	@Nonnull
	public Component getTitle()
	{
		return Component.translatable("jei.part_fabrication.title");
	}
	@Override
	@Nonnull
	public IDrawable getBackground() { return BackgroundDrawable; }
	@Override
	@Nonnull
	public IDrawable getIcon()
	{
		return IconDrawable;
	}
	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder,
						  @Nonnull PartFabricationRecipe recipe,
						  @Nonnull IFocusGroup focuses)
	{
		for(int i = 0; i < recipe.InputIngredients.size(); i++)
		{
			final int index = i;
			builder.addSlot(RecipeIngredientRole.INPUT, 6 + 26*i, 6)
				.addIngredients(recipe.InputIngredients.get(i))
				.addTooltipCallback(((recipeSlotView, tooltip) -> {
					int[] numRequired = recipe.GetRequiredOfEachIngredient();
					tooltip.addAll(recipe.GenerateTooltip(index, numRequired[index], numRequired[index]));
				}))
				.setSlotName("input_" + i);
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 141, 6)
			.addItemStack(recipe.getResultItem(RegistryAccess.EMPTY))
			.setSlotName("output");
	}
	@Override
	public void draw(@Nonnull PartFabricationRecipe recipe,
					 @Nonnull IRecipeSlotsView recipeSlotsView,
					 @Nonnull GuiGraphics guiGraphics,
					 double mouseX, double mouseY)
	{
		BackgroundDrawable.DrawExtras(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
	}
}