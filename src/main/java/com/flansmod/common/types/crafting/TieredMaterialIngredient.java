package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;


public class TieredMaterialIngredient extends AbstractIngredient
{
	public final EMaterialType MaterialType;
	public final int Tier;
	public final boolean AllowHigherTiers;

	protected TieredMaterialIngredient(EMaterialType materialType, int tier, boolean allowHigherTiers)
	{
		MaterialType = materialType;
		Tier = tier;
		AllowHigherTiers = allowHigherTiers;
	}

	public static Ingredient of(EMaterialType materialType, int tier, boolean allowHigherTiers)
	{
		return new TieredMaterialIngredient(materialType, tier, allowHigherTiers);
	}

	@Override
	public boolean test(@Nullable ItemStack target)
	{
		if (target == null)
			return false;

		MaterialMatcher matcher = FlansMod.MATERIALS.GetMatcherForThisTier(MaterialType, Tier, AllowHigherTiers);
		return matcher.Matches(target);
	}

	@Override
	public boolean isSimple() { return true; }
	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() { return Serializer.INSTANCE; }
	@Override
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("materialType", MaterialType.toString());
		json.addProperty("tier", Tier);
		json.addProperty("allowHigherTiers", AllowHigherTiers);
		return json;
	}

	public static class Serializer implements IIngredientSerializer<TieredMaterialIngredient>
	{
		public static final TieredMaterialIngredient.Serializer INSTANCE = new TieredMaterialIngredient.Serializer();

		@Override
		public TieredMaterialIngredient parse(FriendlyByteBuf buffer)
		{
			return new TieredMaterialIngredient(EMaterialType.values()[buffer.readByte()], buffer.readByte(), buffer.readBoolean());
		}

		@Override
		public TieredMaterialIngredient parse(JsonObject json)
		{
			EMaterialType materialType = EMaterialType.Misc;
			int tier = 1;
			boolean allowHigherTiers = false;
			if(json.has("materialType"))
				materialType = EMaterialType.valueOf(json.get("materialType").getAsString());
			if(json.has("tier"))
				tier = json.get("tier").getAsInt();
			if(json.has("allowHigherTiers"))
				allowHigherTiers = json.get("allowHigherTiers").getAsBoolean();

			return new TieredMaterialIngredient(materialType, tier, allowHigherTiers);
		}

		@Override
		public void write(FriendlyByteBuf buffer, TieredMaterialIngredient ingredient)
		{
			buffer.writeByte(ingredient.MaterialType.ordinal());
			buffer.writeByte(ingredient.Tier);
			buffer.writeBoolean(ingredient.AllowHigherTiers);
		}

	}
}
