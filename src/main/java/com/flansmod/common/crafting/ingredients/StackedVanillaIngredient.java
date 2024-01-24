package com.flansmod.common.crafting.ingredients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class StackedVanillaIngredient extends StackedIngredient
{
	public final ResourceLocation[] Items;
	public final List<TagKey<Item>> Tags;
	private ItemStack[] MatchingStacks = null;

	public StackedVanillaIngredient(ResourceLocation[] items, List<TagKey<Item>> tags, int count)
	{
		super(count);
		Items = items;
		Tags = tags;
	}
	@Override
	public boolean isSimple()
	{
		return true;
	}
	@Override
	public boolean isEmpty() { return getItems().length == 0; }
	@Override
	@Nonnull
	public ItemStack[] getItems()
	{
		if(MatchingStacks == null)
		{
			List<ItemStack> matches = new ArrayList<>();
			for(Item item : ForgeRegistries.ITEMS.getValues())
			{
				boolean match = false;
				for(ResourceLocation itemLoc : Items)
					if(item.builtInRegistryHolder().is(itemLoc))
						match = true;
				for (TagKey<Item> tag : Tags)
					if (item.builtInRegistryHolder().is(tag))
						match = true;

				if(match)
					matches.add(new ItemStack(item, Count));
			}
			MatchingStacks = new ItemStack[matches.size()];
			matches.toArray(MatchingStacks);
		}

		return MatchingStacks;
	}
	@Override
	public int Count(@Nullable ItemStack target)
	{
		if (target == null)
			return 0;

		int value = 0;
		for(ResourceLocation itemLoc : Items)
			if(target.getItem().builtInRegistryHolder().is(itemLoc))
				return target.getCount();
		for (TagKey<Item> tag : Tags)
			if (target.is(tag))
				return target.getCount();

		return 0;
	}

	@Override
	@Nonnull
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		JsonArray itemArray = new JsonArray();
		for (ResourceLocation item : Items)
			itemArray.add(item.toString());
		json.add("items", itemArray);
		JsonArray tagArray = new JsonArray();
		for (TagKey<Item> tag : Tags)
			tagArray.add(tag.toString());
		json.add("tags", tagArray);
		json.addProperty("count", Count);
		return json;
	}

	@Override
	@Nonnull
	public IIngredientSerializer<? extends Ingredient> getSerializer() { return StackedVanillaIngredient.Serializer.INSTANCE; }
	public static class Serializer implements IIngredientSerializer<StackedVanillaIngredient>
	{
		public static final StackedVanillaIngredient.Serializer INSTANCE = new StackedVanillaIngredient.Serializer();

		@Override
		@Nonnull
		public StackedVanillaIngredient parse(@Nonnull JsonObject json)
		{
			ResourceLocation[] items = new ResourceLocation[0];
			if(json.has("items"))
			{
				JsonArray itemArray = GsonHelper.getAsJsonArray(json, "items");
				items = new ResourceLocation[itemArray.size()];
				for (int i = 0; i < itemArray.size(); i++)
					items[i] = new ResourceLocation(itemArray.get(i).getAsString());
			}

			List<TagKey<Item>> tags = new ArrayList<>();
			if(json.has("tags"))
			{
				JsonArray tagArray = GsonHelper.getAsJsonArray(json, "tags");
				for (int i = 0; i < tagArray.size(); i++)
					tags.add(TagKey.create(Registries.ITEM, new ResourceLocation(tagArray.get(i).getAsString())));
			}
			int count = GsonHelper.getAsInt(json, "count", 1);
			return new StackedVanillaIngredient(items, tags, count);
		}

		@Override
		public void write(FriendlyByteBuf buffer, StackedVanillaIngredient ingredient)
		{
			buffer.writeInt(ingredient.Items.length);
			for(int i = 0; i < ingredient.Items.length; i++)
				buffer.writeUtf(ingredient.Items[i].toString());
			buffer.writeInt(ingredient.Tags.size());
			for(int i = 0; i < ingredient.Tags.size(); i++)
				buffer.writeUtf(ingredient.Tags.get(i).location().toString());
			buffer.writeInt(ingredient.Count);
		}

		@Override
		@Nonnull
		public StackedVanillaIngredient parse(FriendlyByteBuf buffer)
		{
			int itemCount = buffer.readInt();
			ResourceLocation[] items = new ResourceLocation[itemCount];
			for(int i = 0; i < itemCount; i++)
				items[i] = new ResourceLocation(buffer.readUtf());

			int tagCount = buffer.readInt();
			List<TagKey<Item>> tags = new ArrayList<>(tagCount);
			for(int i = 0; i < tagCount; i++)
				tags.add(TagKey.create(Registries.ITEM, new ResourceLocation(buffer.readUtf())));

			int count = buffer.readInt();

			return new StackedVanillaIngredient(items, tags, count);
		}
	}
}
