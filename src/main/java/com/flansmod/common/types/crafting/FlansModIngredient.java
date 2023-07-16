package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.util.MinecraftHelpers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
public class FlansModIngredient extends AbstractIngredient
{
	public final IngredientDefinition Def;
	private List<Item> MatchingItems;

	protected FlansModIngredient(IngredientDefinition def)
	{
		Def = def;
		MatchingItems = null;
		CacheItemMatches();
	}

	public static Ingredient of(IngredientDefinition def)
	{
		return new FlansModIngredient(def);
	}

	private void CacheItemMatches()
	{
		MatchingItems = new ArrayList<>();
		List<TagKey<Item>> tagKeys = new ArrayList<>();
		if (Def.compareItemTags)
		{
			for (String tag : Def.requiredTags)
			{
				ResourceLocation tagResLoc = ResourceLocation.tryParse(tag);
				if (tagResLoc != null)
				{
					tagKeys.add(ItemTags.create(tagResLoc));
				}
				else FlansMod.LOGGER.warn("Failed to parse tag ResourceLocation " + tag + " for CacheItemMatches");
			}
		}

		for(var kvp : ForgeRegistries.ITEMS.getEntries())
		{
			if (Def.compareItemName)
			{
				if(!kvp.getKey().equals(Def.itemName))
					continue;
			}
			if (Def.compareItemTags)
			{
				boolean matchedATag = false;
				for(TagKey<Item> tagKey : tagKeys)
				{
					if(kvp.getValue().builtInRegistryHolder().is(tagKey))
						matchedATag = true;
				}
				if(!matchedATag)
					continue;
			}

			MatchingItems.add(kvp.getValue());
		}
	}

	@Override
	public boolean test(@Nullable ItemStack target)
	{
		if (target == null)
			return false;

		if(target.getCount() < Def.count)
			return false;

		if(Def.compareItemName || Def.compareItemTags)
		{
			if(MatchingItems == null)
				CacheItemMatches();

			boolean itemMatches = false;
			for(Item item : MatchingItems)
				if(target.is(item))
					itemMatches = true;

			if(!itemMatches)
				return false;
		}

		if(Def.compareDamage)
		{
			if(target.getDamageValue() > Def.maxAllowedDamage)
				return false;
			if(target.getDamageValue() < Def.minAllowedDamage)
				return false;
		}

		if(Def.compareNBT)
		{
			if(Def.requiredNBT.length > 0 && target.getTag() == null)
				return false;
			else
			{
				for (String required : Def.requiredNBT)
				{
					String[] split = required.split(":");
					if (split.length > 0)
					{
						if (!target.getTag().contains(split[0]))
							return false;
						if (split.length > 1)
						{
							Tag tag = target.getTag().get(split[0]);
							if(!MinecraftHelpers.TagEqual(tag, split[1]))
								return false;
						}
					}
				}
			}

			if(target.getTag() != null)
			{
				for (String disallowed : Def.disallowedNBT)
				{
					String[] split = disallowed.split(":");
					if(split.length > 1)
					{
						if (MinecraftHelpers.TagEqual(target.getTag().get(split[0]), split[1]))
							return false;
					}
					else if(split.length == 1)
					{
						if (target.getTag().contains(split[0]))
							return false;
					}
				}
			}
		}

		return true;
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

	public static class Serializer implements IIngredientSerializer<FlansModIngredient>
	{
		public static final FlansModIngredient.Serializer INSTANCE = new FlansModIngredient.Serializer();

		@Override
		public FlansModIngredient parse(FriendlyByteBuf buffer)
		{

			return new FlansModIngredient(EMaterialType.values()[buffer.readByte()], buffer.readByte(), buffer.readBoolean());
		}

		@Override
		public FlansModIngredient parse(JsonObject json)
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

			return new FlansModIngredient(materialType, tier, allowHigherTiers);
		}

		@Override
		public void write(FriendlyByteBuf buffer, FlansModIngredient ingredient)
		{
			buffer.writeByte(ingredient.MaterialType.ordinal());
			buffer.writeByte(ingredient.Tier);
			buffer.writeBoolean(ingredient.AllowHigherTiers);
		}

	}
}

 */
