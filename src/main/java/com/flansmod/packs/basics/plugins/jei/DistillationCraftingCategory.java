package com.flansmod.packs.basics.plugins.jei;

import com.flansmod.packs.basics.BasicPartsMod;
import com.flansmod.packs.basics.common.DistillationRecipe;
import com.flansmod.plugins.jei.FlansCraftingCategory;
import com.flansmod.physics.common.util.MinecraftHelpers;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DistillationCraftingCategory extends FlansCraftingCategory<DistillationRecipe>
{
	private final DistillationDrawable.Background BackgroundDrawable;
	private final DistillationDrawable.Icon IconDrawable;

	public DistillationCraftingCategory(RecipeType<DistillationRecipe> recipeType)
	{
		super(recipeType);

		BackgroundDrawable = new DistillationDrawable.Background();
		IconDrawable = new DistillationDrawable.Icon();
	}

	@Override
	@Nonnull
	public Component getTitle()
	{
		return Component.translatable("jei.distillation.title");
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
						  @Nonnull DistillationRecipe recipe,
						  @Nonnull IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 4)
			.addIngredients(recipe.GetInputIngredient())
			.setSlotName("input");

		List<DistillationRecipe> recipeGroup = new ArrayList<>();
		Level world = MinecraftHelpers.clientGetCurrentLevel();
		if(world != null)
		{
			for(DistillationRecipe otherRecipe : world.getRecipeManager().getAllRecipesFor(BasicPartsMod.DISTILLATION_RECIPE_TYPE.get()))
			{
				if(IngredientsEqual(recipe.GetInputIngredient(), otherRecipe.GetInputIngredient()))
				{
					recipeGroup.add(otherRecipe);
				}
			}
		}
		else
		{
			recipeGroup.add(recipe);
		}

		for(DistillationRecipe match : recipeGroup)
		{
			int depth = match.GetFractionDepth();
			builder.addSlot(RecipeIngredientRole.OUTPUT, 69, 21 + (20 * (depth - 1)))
				.addItemStack(match.GetResultItem())
				.setSlotName("output_" + depth);
		}
	}

	private boolean IngredientsEqual(@Nonnull Ingredient a, @Nonnull Ingredient b)
	{
		for(ItemStack aStack : a.getItems())
			if(!b.test(aStack))
				return false;
		for(ItemStack bStack : b.getItems())
			if(!a.test(bStack))
				return false;
		return true;
	}

	@Override
	public void draw(@Nonnull DistillationRecipe recipe,
					 @Nonnull IRecipeSlotsView recipeSlotsView,
					 @Nonnull GuiGraphics guiGraphics,
					 double mouseX, double mouseY)
	{
		BackgroundDrawable.DrawExtras(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
	}
}
