package com.flansmod.common.types.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FabricatingRecipeSerializer implements RecipeSerializer<FabricatingRecipe>
{
	@Override
	public FabricatingRecipe fromJson(ResourceLocation location, JsonObject json)
	{
		List<ItemStack> itemStacks = new ArrayList<>();
		JsonArray array = GsonHelper.getAsJsonArray(json, "item_parts");
		for(JsonElement element : array.asList())
		{
			Item item = GsonHelper.convertToItem(element, "");
			itemStacks.add(new ItemStack(item));
		}


		JsonArray mat_array = GsonHelper.getAsJsonArray(json, "material_parts");
		for(JsonElement element : mat_array.asList())
		{

		}


		return new FabricatingRecipe();
	}

	@Override
	public @Nullable FabricatingRecipe fromNetwork(ResourceLocation location, FriendlyByteBuf buf)
	{
		return null;
	}

	@Override
	public void toNetwork(FriendlyByteBuf buf, FabricatingRecipe recipe)
	{

	}
}
