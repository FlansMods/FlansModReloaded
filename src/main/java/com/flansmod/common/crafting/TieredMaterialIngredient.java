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

public class TieredMaterialIngredient extends AbstractIngredient
{
	public final MaterialDefinition MaterialType;
	public final int Count;

	public TieredMaterialIngredient(MaterialDefinition materialType, int count)
	{
		MaterialType = materialType;
		Count = count;
	}

	@Override
	public boolean isSimple()
	{
		return true;
	}

	@Override
	public boolean test(@Nullable ItemStack target)
	{
		if (target == null)
			return false;

		int value = 0;
		for (MaterialSourceDefinition source : MaterialType.sources)
		{
			value += source.AnalyzeStack(target);
		}

		return value >= Count;
	}

	@Override
	@Nonnull
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("material", MaterialType.Location.toString());
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
			MaterialDefinition material = FlansMod.MATERIALS.Get(materialLoc);
			int count = GsonHelper.getAsInt(json, "count", 1);
			return new TieredMaterialIngredient(material, count);
		}

		@Override
		public void write(FriendlyByteBuf buffer, TieredMaterialIngredient ingredient)
		{
			buffer.writeInt(ingredient.MaterialType.hashCode());
			buffer.writeInt(ingredient.Count);
		}

		@Override
		@Nonnull
		public TieredMaterialIngredient parse(FriendlyByteBuf buffer)
		{
			int materialHash = buffer.readInt();
			int count = buffer.readInt();

			return new TieredMaterialIngredient(
				FlansMod.MATERIALS.ByHash(materialHash),
				count);
		}
	}
}