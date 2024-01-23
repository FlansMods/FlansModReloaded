package com.flansmod.common.crafting.recipes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.crafting.ERecipePart;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.player.Input;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GunFabricationRecipe implements Recipe<WorkbenchBlockEntity>
{
	@Nonnull
	public final RecipeType<?> Type;
	@Nonnull
	public final ERecipePart Part;
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

	public GunFabricationRecipe(@Nonnull RecipeType<?> type,
								@Nonnull ResourceLocation loc,
								@Nonnull String group,
								@Nonnull Ingredient[] inputIngredients,
								@Nonnull ItemStack result,
								@Nonnull ERecipePart part,
								int craftTime)
	{
		Type = type;
		Part = part;
		Loc = loc;
		Group = group;
		InputIngredients = NonNullList.of(Ingredient.EMPTY, inputIngredients);
		Result = result;

		CraftTime = craftTime;
	}

	@Override
	public boolean matches(@Nonnull WorkbenchBlockEntity workbench, @Nullable Level level)
	{
		// Is this recipe available in this particular workbench?
		if (!workbench.GunRecipeCanBeCraftedInThisWorkbench(Result))
			return false;

		// Are there enough slots to complete this recipe? (There should be)
		if(workbench.GunCraftingInputContainer.getContainerSize() < InputIngredients.size())
			return false;

		// Slots are ordered and must match the ingredient for that slot
		for(int slot = 0; slot < InputIngredients.size(); slot++)
		{
			boolean match = InputIngredients.get(slot).test(workbench.GunCraftingInputContainer.getItem(slot));
			if(!match)
				return false;
		}

		return true;
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull WorkbenchBlockEntity workbench, @Nonnull RegistryAccess registryAccess)
	{
		ItemStack output = Result.copy();
		List<ItemStack> craftingInputs = new ArrayList<>();
		for(int slot = 0; slot < workbench.GunCraftingInputContainer.getContainerSize(); slot++)
		{
			ItemStack inputStack = workbench.GunCraftingInputContainer.getItem(slot);
			if(inputStack.getItem() instanceof PartItem part)
				craftingInputs.add(inputStack);
		}
		FlanItem.SetCraftingInputs(output, craftingInputs);
		return output;
	}

	@Override
	@Nonnull
	public RecipeSerializer<?> getSerializer() { return FlansMod.GUN_FABRICATION_RECIPE_SERIALIZER.get(); }
	public static class Serializer implements RecipeSerializer<GunFabricationRecipe>
	{
		@Nonnull
		public GunFabricationRecipe fromJson(@Nonnull ResourceLocation loc, @Nonnull JsonObject json)
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
			ERecipePart recipePart = ERecipePart.valueOf(GsonHelper.getAsString(json, "part", "generic"));

			return new GunFabricationRecipe(
				FlansMod.GUN_FABRICATION_RECIPE_TYPE.get(),
				loc,
				group,
				ingredients,
				output,
				recipePart,
				craftTime);
		}

		@Override
		@Nullable
		public GunFabricationRecipe fromNetwork(@Nonnull ResourceLocation loc, @Nonnull FriendlyByteBuf buf)
		{
			String group = buf.readUtf();
			int count = buf.readInt();
			Ingredient[] inputs = new Ingredient[count];
			for(int i = 0 ; i < count; i++)
				inputs[i] = Ingredient.fromNetwork(buf);
			ItemStack output = buf.readItem();
			int time = buf.readInt();
			ERecipePart recipePart = ERecipePart.values()[buf.readInt()];
			return new GunFabricationRecipe(
				FlansMod.GUN_FABRICATION_RECIPE_TYPE.get(),
				loc,
				group,
				inputs,
				output,
				recipePart,
				time);
		}

		@Override
		public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull GunFabricationRecipe recipe)
		{
			buf.writeUtf(recipe.Group);
			buf.writeInt(recipe.InputIngredients.size());
			for(int i = 0; i < recipe.InputIngredients.size(); i++)
				recipe.InputIngredients.get(i).toNetwork(buf);
			buf.writeItem(recipe.Result);
			buf.writeInt(recipe.CraftTime);
			buf.writeInt(recipe.Part.ordinal());
		}
	}
}
