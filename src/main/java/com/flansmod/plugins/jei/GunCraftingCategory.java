package com.flansmod.plugins.jei;

import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GunCraftingCategory extends FlansCraftingCategory<GunFabricationRecipe>
{
	private final GunFabricationDrawable.Background BackgroundDrawable;
	private final GunFabricationDrawable.Icon IconDrawable;

	public GunCraftingCategory(RecipeType<GunFabricationRecipe> recipeType)
	{
		super(recipeType);

		BackgroundDrawable = new GunFabricationDrawable.Background();
		IconDrawable = new GunFabricationDrawable.Icon();
	}

	@Override
	@Nonnull
	public Component getTitle()
	{
		return Component.translatable("jei.gun_fabrication.title");
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
						  @Nonnull GunFabricationRecipe recipe,
						  @Nonnull IFocusGroup focuses)
	{
		for(int i = 0; i < recipe.InputIngredients.size(); i++)
		{
			final int index = i;
			builder.addSlot(RecipeIngredientRole.INPUT, 11+22*index, 9)
				.addIngredients(recipe.InputIngredients.get(index))
				.addTooltipCallback((((recipeSlotView, tooltip) -> {
					tooltip.addAll(recipe.GenerateTooltip(index));
				})))
				.setSlotName("input_" + index);
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 141, 9)
			.addItemStack(recipe.Result)
			.setSlotName("output");
	}
	@Override
	public void draw(@Nonnull GunFabricationRecipe recipe,
					 @Nonnull IRecipeSlotsView recipeSlotsView,
					 @Nonnull GuiGraphics guiGraphics,
					 double mouseX, double mouseY)
	{
		BackgroundDrawable.DrawExtras(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
	}
}