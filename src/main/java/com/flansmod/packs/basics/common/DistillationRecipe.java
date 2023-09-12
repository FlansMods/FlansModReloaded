package com.flansmod.packs.basics.common;

import com.flansmod.common.crafting.RestrictedContainer;
import com.flansmod.packs.basics.BasicPartsMod;
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

public class DistillationRecipe implements Recipe<DistillationTowerBlockEntity>
{
	protected final RecipeType<?> Type;
	protected final ResourceLocation Loc;
	protected final String Group;
	protected final ItemStack Result;
	protected final int DistillationFractionDepth;
	protected final Ingredient InputIngredient;
	protected final int DistillationTime;

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
	public ItemStack assemble(@Nonnull DistillationTowerBlockEntity distiller) { return Result.copy(); }
	@Nonnull
	@Override
	public ResourceLocation getId() { return Loc; }
	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients() { return NonNullList.of(InputIngredient); }
	public int GetDistillationTime() {
		return DistillationTime;
	}

	public DistillationRecipe(RecipeType<?> type,
							  ResourceLocation loc,
							  String group,
							  Ingredient inputIngredient,
							  ItemStack result,
							  int distillationFractionDepth,
							  int distillationTime)
	{
		Type = type;
		Loc = loc;
		Group = group;
		InputIngredient = inputIngredient;
		Result = result;
		DistillationFractionDepth = distillationFractionDepth;
		DistillationTime = distillationTime;
	}

	@Override
	public boolean matches(DistillationTowerBlockEntity distiller, Level level)
	{
		// This block will output the matching recipe if the top of its stack contains the source ingredient
		// AND it is at the right depth to be drained off
		int fractionDepth = distiller.GetFractionDepth();
		if(fractionDepth == DistillationFractionDepth)
		{
			DistillationTowerBlockEntity topDistiller = distiller.GetTopDistillationTileEntity();
			if(topDistiller != null && topDistiller.IsTop)
			{
				return InputIngredient.test(topDistiller.getItem(DistillationTowerBlockEntity.INPUT_SLOT));
			}
		}
		return false;
	}

	@Override
	@Nonnull
	public RecipeSerializer<?> getSerializer() { return BasicPartsMod.DISTILLATION_RECIPE_SERIALIZER.get(); }
	public static class Serializer implements RecipeSerializer<DistillationRecipe>
	{
		@Nonnull
		public DistillationRecipe fromJson(@Nonnull ResourceLocation loc, @Nonnull JsonObject json)
		{
			String group = GsonHelper.getAsString(json, "group", "");
			Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
			ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
			int distillationFractionDepth = GsonHelper.getAsInt(json, "fraction_depth", 1);
			int distillationTime = GsonHelper.getAsInt(json, "distillation_time", 20);
			return new DistillationRecipe(
				BasicPartsMod.DISTILLATION_RECIPE_TYPE.get(),
				loc,
				group,
				input,
				output,
				distillationFractionDepth,
				distillationTime);
		}

		@Override
		public @Nullable DistillationRecipe fromNetwork(@Nonnull ResourceLocation loc, @Nonnull FriendlyByteBuf buf)
		{
			String group = buf.readUtf();
			Ingredient input = Ingredient.fromNetwork(buf);
			ItemStack output = buf.readItem();
			int depth = buf.readInt();
			int time = buf.readInt();
			return new DistillationRecipe(
				BasicPartsMod.DISTILLATION_RECIPE_TYPE.get(),
				loc,
				group,
				input,
				output,
				depth,
				time);
		}

		@Override
		public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull DistillationRecipe recipe)
		{
			buf.writeUtf(recipe.Group);
			recipe.InputIngredient.toNetwork(buf);
			buf.writeItem(recipe.Result);
			buf.writeInt(recipe.DistillationFractionDepth);
			buf.writeInt(recipe.DistillationTime);
		}
	}
}
