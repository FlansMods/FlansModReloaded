package com.flansmod.common.crafting.ingredients;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TieredPartIngredient extends AbstractIngredient
{
	@Nonnull
	public final EMaterialType MaterialType;
	public final int MaterialTierMin;
	public final int MaterialTierMax;
	@Nonnull
	public final String MatchTag;

	// Matching stack cache
	@Nullable
	private ItemStack[] CachedMatchingStacks = null;
	@Nullable
	private List<MaterialDefinition> CachedMaterialMatches = null;
	@Nullable
	private List<PartDefinition> CachedPartMatches = null;

	public TieredPartIngredient(@Nonnull ResourceLocation matLoc, @Nonnull String matchTag)
	{
		super();
		MaterialDefinition matDef = FlansMod.MATERIALS.Get(matLoc);
		MaterialType = matDef.materialType;
		MaterialTierMin = matDef.craftingTier;
		MaterialTierMax = Integer.MAX_VALUE;
		MatchTag = matchTag;
	}
	public TieredPartIngredient(@Nonnull EMaterialType materialType,
								int tierMin,
								int tierMax,
								@Nonnull String matchTag)
	{
		MaterialType = materialType;
		MaterialTierMin = tierMin;
		MaterialTierMax = tierMax;
		MatchTag = matchTag;
	}

	@Nonnull
	public List<Component> GenerateTooltip(boolean advanced)
	{
		List<Component> lines = new ArrayList<>();

		// --- Match Tag ---
		if(!MatchTag.isEmpty())
			lines.add(Component.translatable("crafting.with_tag", MatchTag));

		// --- Match Materials ---
		//if(MaterialType.length == 0)
		//	lines.add(Component.translatable("crafting.match_any_material"));
		//else if(allowedMaterials.length == 1)
			lines.add(Component.translatable("crafting.match_single", MaterialType.ToComponent()));
		//else
		//{
		//	Object[] varargs = new Object[allowedMaterials.length];
		//	for(int n = 0; n < allowedMaterials.length; n++)
		//		varargs[n] = allowedMaterials[n].ToComponent();
		//	lines.add(Component.translatable("crafting.match_multiple." + allowedMaterials.length, varargs));
		//}

		// --- Match Tiers ---
		if(MaterialTierMin == MaterialTierMax)
			lines.add(Component.translatable("crafting.match_single_tier", MaterialTierMin));
		else if(MaterialTierMin == 1)
			lines.add(Component.translatable("crafting.match_tiers_below", MaterialTierMax));
		else
			lines.add(Component.translatable("crafting.match_tiers_between", MaterialTierMin, MaterialTierMax));

		return lines;
	}

	@Nonnull
	public List<MaterialDefinition> GetMaterialMatches()
	{
		if(CachedMaterialMatches == null)
		{
			CachedMaterialMatches = FlansMod.MATERIALS.Find((mat) ->
				mat.IsValid()
					&& mat.craftingTier >= MaterialTierMin
					&& mat.craftingTier <= MaterialTierMax
					&& mat.materialType == MaterialType);
		}
		return CachedMaterialMatches;
	}
	@Nonnull
	public List<PartDefinition> GetPartMatches()
	{
		if(CachedPartMatches == null)
		{
			CachedPartMatches = FlansMod.PARTS.Find((part) ->
				part.IsValid()
				&& GetMaterialMatches().contains(part.GetMaterial())
				&& part.itemSettings.Matches(MatchTag));
		}
		return CachedPartMatches;
	}
	@Override
	@Nonnull
	public ItemStack[] getItems()
	{
		if (CachedMatchingStacks == null)
		{
			List<ItemStack> matching = new ArrayList<>();
			TagKey<Item> matchTagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MatchTag));
			for(ResourceLocation partID : FlansMod.PARTS.getIds())
			{
				Item partItem = ForgeRegistries.ITEMS.getValue(partID);
				if(partItem != null && partItem.builtInRegistryHolder().is(matchTagKey))
				{
					PartDefinition part = FlansMod.PARTS.Get(partID);
					if(part.IsValid())
					{
						MaterialDefinition matDef = FlansMod.MATERIALS.Get(part.material);
						if (matDef.IsValid())
						{
							if (matDef.materialType == MaterialType
								&& matDef.craftingTier >= MaterialTierMin
								&& matDef.craftingTier <= MaterialTierMax)
							{
								matching.add(new ItemStack(partItem));
							}
						}
					}
				}
			}
			CachedMatchingStacks = matching.toArray(CachedMatchingStacks);
		}
		return CachedMatchingStacks;
	}

	@Override
	public boolean isSimple() { return true; }

	@Override
	@Nonnull
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("material", MaterialType.toString());
		json.addProperty("min", MaterialTierMin);
		json.addProperty("max", MaterialTierMax);
		json.addProperty("tag", MatchTag);
		return json;
	}

	@Override
	@Nonnull
	public IIngredientSerializer<? extends Ingredient> getSerializer()
	{
		return TieredPartIngredient.Serializer.INSTANCE;
	}

	public static class Serializer implements IIngredientSerializer<TieredPartIngredient>
	{
		public static final TieredPartIngredient.Serializer INSTANCE = new TieredPartIngredient.Serializer();

		@Override
		@Nonnull
		public TieredPartIngredient parse(@Nonnull JsonObject json)
		{
			EMaterialType materialType = EMaterialType.parse( GsonHelper.getAsString(json, "material", ""));
			String tag = GsonHelper.getAsString(json, "tag", "flansmod:generic");
			int min = GsonHelper.getAsInt(json, "min", 1);
			int max = GsonHelper.getAsInt(json, "max", Integer.MAX_VALUE);
			return new TieredPartIngredient(materialType, min, max, tag);
		}

		@Override
		public void write(FriendlyByteBuf buffer, TieredPartIngredient ingredient)
		{
			buffer.writeInt(ingredient.MaterialType.ordinal());
			buffer.writeInt(ingredient.MaterialTierMin);
			buffer.writeInt(ingredient.MaterialTierMax);
			buffer.writeUtf(ingredient.MatchTag);
		}

		@Override
		@Nonnull
		public TieredPartIngredient parse(FriendlyByteBuf buffer)
		{
			EMaterialType materialType = EMaterialType.values()[buffer.readInt()];
			int min = buffer.readInt();
			int max = buffer.readInt();
			String tag = buffer.readUtf();
			return new TieredPartIngredient(materialType, min, max, tag);
		}
	}
}
