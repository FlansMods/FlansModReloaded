package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.elements.MaterialSourceDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TieredMaterialIngredient extends StackedIngredient
{
	public MaterialDefinition MaterialType() { return FlansMod.MATERIALS.Get(MaterialLocation); }

	public final ResourceLocation MaterialLocation;
	private ItemStack[] MatchingStacks = null;

	public TieredMaterialIngredient(ResourceLocation matLoc, int count)
	{
		super(count);
		MaterialLocation = matLoc;
	}

	@Override
	public boolean isSimple()
	{
		return true;
	}

	@Override
	@Nonnull
	public ItemStack[] getItems()
	{
		if(MatchingStacks == null)
		{
			MaterialDefinition material = FlansMod.MATERIALS.Get(MaterialLocation);
			if (material.IsValid())
			{
				List<ItemStack> matches = new ArrayList<>();
				for (MaterialSourceDefinition source : material.sources)
				{
					matches.addAll(source.GetMatches());
				}
				MatchingStacks = new ItemStack[matches.size()];
				matches.toArray(MatchingStacks);
			}
			else
			{
				MatchingStacks = new ItemStack[0];
			}
		}

		return MatchingStacks;
	}

	@Override
	public int Count(@Nullable ItemStack target)
	{
		if (target == null)
			return 0;

		int value = 0;
		for (MaterialSourceDefinition source : MaterialType().sources)
		{
			value += source.AnalyzeStack(target);
		}

		return value;
	}

	@Override
	@Nonnull
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("material", MaterialLocation.toString());
		json.addProperty("count", Count);
		return json;
	}

	@Override
	@Nonnull
	public IIngredientSerializer<? extends Ingredient> getSerializer() { return Serializer.INSTANCE; }
	public static class Serializer implements IIngredientSerializer<TieredMaterialIngredient>
	{
		public static final TieredMaterialIngredient.Serializer INSTANCE = new TieredMaterialIngredient.Serializer();

		@Override
		@Nonnull
		public TieredMaterialIngredient parse(@Nonnull JsonObject json)
		{
			String materialName = GsonHelper.getAsString(json, "material", "");
			ResourceLocation materialLoc = new ResourceLocation(materialName);
			int count = GsonHelper.getAsInt(json, "count", 1);
			return new TieredMaterialIngredient(materialLoc, count);
		}

		@Override
		public void write(FriendlyByteBuf buffer, TieredMaterialIngredient ingredient)
		{
			buffer.writeInt(ingredient.MaterialLocation.hashCode());
			buffer.writeInt(ingredient.Count);
		}

		@Override
		@Nonnull
		public TieredMaterialIngredient parse(FriendlyByteBuf buffer)
		{
			int materialHash = buffer.readInt();
			int count = buffer.readInt();

			return new TieredMaterialIngredient(
				FlansMod.MATERIALS.ByHash(materialHash).Location,
				count);
		}
	}
}