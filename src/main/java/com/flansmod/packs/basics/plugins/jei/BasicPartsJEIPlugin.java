package com.flansmod.packs.basics.plugins.jei;

import com.flansmod.packs.basics.BasicPartsMod;
import com.flansmod.packs.basics.common.DistillationRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class BasicPartsJEIPlugin implements IModPlugin
{
	private static final ResourceLocation ID = new ResourceLocation(BasicPartsMod.MODID, "jei_plugin");
	@Override
	@Nonnull
	public ResourceLocation getPluginUid() { return ID; }

	private static final RecipeType<DistillationRecipe> DISTILLATION_RECIPE_TYPE = RecipeType.create(BasicPartsMod.MODID, "distillation", DistillationRecipe.class);
	private static final DistillationCraftingCategory DISTILLATION_CRAFTING_CATEGORY = new DistillationCraftingCategory(DISTILLATION_RECIPE_TYPE);

	@Override
	public void registerCategories(@Nonnull IRecipeCategoryRegistration registration)
	{
		registration.addRecipeCategories(DISTILLATION_CRAFTING_CATEGORY);
	}
	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registration)
	{
		ClientLevel world = Minecraft.getInstance().level;
		if (world != null)
		{
			List<DistillationRecipe> recipes = world.getRecipeManager().getAllRecipesFor(BasicPartsMod.DISTILLATION_RECIPE_TYPE.get());
			registration.addRecipes(DISTILLATION_RECIPE_TYPE, recipes);
		}

		//registration.getJeiHelpers().
	}
	@Override
	public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(BasicPartsMod.DISTILLATION_TOWER_TOP_ITEM.get()), DISTILLATION_RECIPE_TYPE);
		registration.addRecipeCatalyst(new ItemStack(BasicPartsMod.DISTILLATION_TOWER_ITEM.get()), DISTILLATION_RECIPE_TYPE);
	}


}
