package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import javax.annotation.Nonnull;

public class PartFabricationRecipe implements Recipe<WorkbenchBlockEntity>
{
	protected final RecipeType<?> Type;
	protected final ResourceLocation Loc;
	protected final String Group;
	protected final NonNullList<Ingredient> InputIngredients;
	protected final ItemStack Result;
	protected final int CraftTime;

	@Nonnull
	@Override
	public RecipeType<?> getType() { return Type; }
	@Override
	@Nonnull
	public String getGroup() {
		return Group;
	}
	@Nonnull
	@Override
	public ItemStack getResultItem() { return Result; }
	@Override
	public boolean canCraftInDimensions(int x, int y) { return true; }
	@Nonnull
	@Override
	public ResourceLocation getId() { return Loc; }
	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients() { return InputIngredients; }


	public PartFabricationRecipe(RecipeType<?> type,
								 ResourceLocation loc,
								 String group,
								 Ingredient[] inputIngredients,
								 ItemStack result,
								 int craftTime)
	{
		Type = type;
		Loc = loc;
		Group = group;
		InputIngredients = NonNullList.of(Ingredient.EMPTY, inputIngredients);
		Result = result;
		CraftTime = craftTime;
	}

	@Override
	public boolean matches(WorkbenchBlockEntity workbench, @Nonnull Level level)
	{
		// Is this recipe available in this particular workbench?
		if(!workbench.RecipeCanBeCraftedInThisWorkbench(Result))
			return false;

		int[] amountsMatched = new int[InputIngredients.size()];

		// Then, can we output one from the input ingredients
		for(int slot = 0; slot < workbench.PartCraftingInputContainer.getContainerSize(); slot++)
		{
			ItemStack inputStack = workbench.PartCraftingInputContainer.getItem(slot);
			int countRemaining = inputStack.getCount();
			for(int i = 0; i < InputIngredients.size(); i++)
			{
				Ingredient ingredientToTest = InputIngredients.get(i);
				if(ingredientToTest.test(inputStack))
				{
					// TODO: Wait how do we know how many are needed?
					amountsMatched[i]++;
					countRemaining--;
				}

				// If we used up this whole stack, our other inputs can't check this same stack
				if(countRemaining <= 0)
					break;
			}
		}

		// TODO: Count better, lol?
		for(int i = 0; i < InputIngredients.size(); i++)
		{
			Ingredient ingredientToTest = InputIngredients.get(i);
			if(amountsMatched[i] <= 0)
				return false;
		}

		return true;
	}
	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull WorkbenchBlockEntity distiller) { return Result.copy(); }
	@Override
	@Nonnull
	public RecipeSerializer<?> getSerializer() { return FlansMod.PART_FABRICATION_RECIPE_SERIALIZER.get(); }
	public static class Serializer implements RecipeSerializer<PartFabricationRecipe>
	{
		@Nonnull
		public PartFabricationRecipe fromJson(@Nonnull ResourceLocation loc, @Nonnull JsonObject json)
		{
			String group = GsonHelper.getAsString(json, "group", "");
			JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients");
			Ingredient[] ingredients = new Ingredient[ingredientArray.size()];
			for(int i = 0; i < ingredientArray.size(); i++)
			{
				ingredients[i] = Ingredient.fromJson(ingredientArray.get(i));
			}


			ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
			int craftTime = GsonHelper.getAsInt(json, "craft_time", 20);
			return new PartFabricationRecipe(
				FlansMod.PART_FABRICATION_RECIPE_TYPE.get(),
				loc,
				group,
				ingredients,
				output,
				craftTime);
		}

		@Override
		public @Nullable PartFabricationRecipe fromNetwork(@Nonnull ResourceLocation loc, @Nonnull FriendlyByteBuf buf)
		{
			String group = buf.readUtf();
			int count = buf.readInt();
			Ingredient[] inputs = new Ingredient[count];
			for(int i = 0 ; i < count; i++)
				inputs[i] = Ingredient.fromNetwork(buf);
			ItemStack output = buf.readItem();
			int time = buf.readInt();
			return new PartFabricationRecipe(
				FlansMod.PART_FABRICATION_RECIPE_TYPE.get(),
				loc,
				group,
				inputs,
				output,
				time);
		}

		@Override
		public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull PartFabricationRecipe recipe)
		{
			buf.writeUtf(recipe.Group);
			buf.writeInt(recipe.InputIngredients.size());
			for(int i = 0; i < recipe.InputIngredients.size(); i++)
				recipe.InputIngredients.get(i).toNetwork(buf);
			buf.writeItem(recipe.Result);
			buf.writeInt(recipe.CraftTime);
		}
	}
}


