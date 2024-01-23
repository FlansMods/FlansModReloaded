package com.flansmod.common.crafting.recipes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.ingredients.StackedIngredient;
import com.flansmod.common.crafting.ingredients.TieredMaterialIngredient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PartFabricationRecipe implements Recipe<WorkbenchBlockEntity>
{
	@Nonnull
	public final RecipeType<?> Type;
	@Nonnull
	public final ResourceLocation Loc;
	@Nonnull
	public final String Group;
	@Nonnull
	public final NonNullList<Ingredient> InputIngredients;
	@Nonnull
	public final ItemStack Result;
	public final int CraftTime;

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
	public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) { return Result; }
	@Override
	public boolean canCraftInDimensions(int x, int y) { return true; }
	@Nonnull
	@Override
	public ResourceLocation getId() { return Loc; }
	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients() { return InputIngredients; }


	public PartFabricationRecipe(@Nonnull RecipeType<?> type,
								 @Nonnull ResourceLocation loc,
								 @Nonnull String group,
								 @Nonnull Ingredient[] inputIngredients,
								 @Nonnull ItemStack result,
								 int craftTime)
	{
		Type = type;
		Loc = loc;
		Group = group;
		InputIngredients = NonNullList.of(Ingredient.EMPTY, inputIngredients);
		Result = result;
		CraftTime = craftTime;
	}

	public static int CountInputMatching(@Nonnull Ingredient ingredient, @Nonnull Container container)
	{
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); i++)
		{
			ItemStack stack = container.getItem(i);
			if(ingredient instanceof StackedIngredient stackedIngredient)
			{
				count += stackedIngredient.Count(stack);
			}
			else if(ingredient.test(stack))
				count += stack.getCount();

		}
		return count;
	}
	public int[] GetMatchingOfEachIngredient(@Nonnull Container container)
	{
		int[] matching = new int[InputIngredients.size()];
		for (int i = 0; i < InputIngredients.size(); i++)
		{
			Ingredient ingredient = InputIngredients.get(i);
			matching[i] = CountInputMatching(ingredient, container);
		}
		return matching;
	}
	public int[] GetRequiredOfEachIngredient()
	{
		int[] required = new int[InputIngredients.size()];
		for (int i = 0; i < InputIngredients.size(); i++)
		{
			Ingredient ingredient = InputIngredients.get(i);
			if(ingredient instanceof StackedIngredient stacked)
				required[i] = stacked.Count;
			else required[i] = 1;
		}
		return required;
	}
	@Nonnull
	public List<Component> GenerateTooltip(int ingredientIndex, int numRequired, int numMatching)
	{
		List<Component> lines = new ArrayList<>();
		int maxProduce = numMatching / numRequired;

		Ingredient ingredient = InputIngredients.get(ingredientIndex);
		if (ingredient instanceof TieredMaterialIngredient tiered)
		{
			String materialName = "material." + tiered.MaterialType().Location.getNamespace() + "." + tiered.MaterialType().Location.getPath();
			lines.add(Component.translatable("crafting.match_single", Component.translatable(materialName)));

			String matchingString = tiered.MaterialType().GenerateString(numMatching);
			String requiredString = tiered.MaterialType().GenerateString(numRequired);

			String resetColorCode = "\u00A7f";
			String colorCode = numMatching < numRequired ? "\u00A74" : resetColorCode;

			lines.add(Component.literal(colorCode + matchingString + resetColorCode + " / " + requiredString  + " (Max: " + maxProduce + ")"));
		}
		else
		{
			if(ingredient.getItems().length == 1)
				lines.add(ingredient.getItems()[0].getHoverName());
			else
			{
				lines.add(Component.translatable("crafting.one_of"));
				for (ItemStack possible : ingredient.getItems())
				{
					lines.add(possible.getHoverName());
				}
			}

			lines.add(Component.literal(numMatching + "/" + numRequired + " (Max: " + maxProduce + ")"));
		}

		return lines;
	}

	@Override
	public boolean matches(WorkbenchBlockEntity workbench, @Nonnull Level level)
	{
		// Is this recipe available in this particular workbench?
		if(!workbench.PartRecipeCanBeCraftedInThisWorkbench(Result))
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
	public ItemStack assemble(@Nonnull WorkbenchBlockEntity distiller, @Nonnull RegistryAccess registryAccess) { return Result.copy(); }
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


			ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
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


