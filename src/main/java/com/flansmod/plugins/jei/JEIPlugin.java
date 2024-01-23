package com.flansmod.plugins.jei;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin
{
	private static final ResourceLocation ID = new ResourceLocation(FlansMod.MODID, "jei_plugin");
	@Override
	@Nonnull
	public ResourceLocation getPluginUid() { return ID; }

	private static final RecipeType<PartFabricationRecipe> PART_FABRICATION_RECIPE_TYPE = RecipeType.create(FlansMod.MODID, "part_fabrication", PartFabricationRecipe.class);
	private static final PartCraftingCategory PART_CRAFTING_CATEGORY = new PartCraftingCategory(PART_FABRICATION_RECIPE_TYPE);

	@Override
	public void registerCategories(@Nonnull IRecipeCategoryRegistration registration)
	{
		registration.addRecipeCategories(PART_CRAFTING_CATEGORY);
	}
	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registration)
	{
		ClientLevel world = Minecraft.getInstance().level;
		if (world != null)
 			registration.addRecipes(
				PART_FABRICATION_RECIPE_TYPE,
				world.getRecipeManager().getAllRecipesFor(FlansMod.PART_FABRICATION_RECIPE_TYPE.get()));
	}
	@Override
	public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration)
	{
		for(ResourceLocation workbenchID : FlansMod.WORKBENCHES.getIds())
		{
			WorkbenchDefinition workbenchDefinition = FlansMod.WORKBENCHES.Get(workbenchID);
			Item workbenchItem = ForgeRegistries.ITEMS.getValue(workbenchID);
			if(workbenchItem != null && workbenchDefinition.IsValid())
			{
				if(workbenchDefinition.partCrafting.isActive)
					registration.addRecipeCatalyst(new ItemStack(workbenchItem), PART_FABRICATION_RECIPE_TYPE);
			}
		}
	}
}