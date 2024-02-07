package com.flansmod.common.crafting.ingredients;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.google.gson.JsonArray;
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

public class TieredPartIngredient extends AbstractIngredient implements IExtraIngredientTooltip
{
	@Nonnull
	public final EMaterialType[] MaterialTypes;
	public final int MaterialTierMin;
	public final int MaterialTierMax;
	@Nonnull
	public final ResourceLocation MatchTag;

	// Matching stack cache
	@Nullable
	private ItemStack[] CachedMatchingStacks = null;
	@Nullable
	private List<MaterialDefinition> CachedMaterialMatches = null;
	@Nullable
	private List<PartDefinition> CachedPartMatches = null;

	public boolean IsMatchingMaterialType(EMaterialType matType)
	{
		for (EMaterialType materialType : MaterialTypes)
			if (materialType == matType)
				return true;
		return false;
	}

	public static int MaterialsToFlags(EMaterialType[] materialTypes) {
		int flags = 0;
		for (EMaterialType materialType : materialTypes)
			flags |= (1 << materialType.ordinal());
		return flags;
	}
	public static EMaterialType[] MaterialsFromFlags(int flags) {
		List<EMaterialType> materialTypes = new ArrayList<>();
		for (EMaterialType materialType : EMaterialType.values())
			if((flags & (1 << materialType.ordinal())) != 0)
				materialTypes.add(materialType);
		return materialTypes.toArray(new EMaterialType[0]);
	}

	public TieredPartIngredient(@Nonnull EMaterialType[] materialTypes,
								int tierMin,
								int tierMax,
								@Nonnull ResourceLocation matchTag)
	{
		MaterialTypes = materialTypes;
		MaterialTierMin = tierMin;
		MaterialTierMax = tierMax;
		MatchTag = matchTag;
	}

	@Override
	public void GenerateTooltip(@Nonnull List<Component> lines, boolean advanced)
	{
		// --- Match Tag ---
		if(JsonDefinition.IsValidLocation(MatchTag))
			lines.add(Component.translatable("crafting.with_tag", MatchTag));

		// --- Match Materials ---
		if(MaterialTypes.length == 0)
			lines.add(Component.translatable("crafting.match_any_material"));
		else if(MaterialTypes.length == 1)
			lines.add(Component.translatable("crafting.match_single", MaterialTypes[0].ToComponent()));
		else
		{
			Object[] varargs = new Object[MaterialTypes.length];
			for(int n = 0; n < MaterialTypes.length; n++)
				varargs[n] = MaterialTypes[n] != null ? MaterialTypes[n].ToComponent() : Component.empty();
			lines.add(Component.translatable("crafting.match_multiple." + MaterialTypes.length, varargs));
		}

		// --- Match Tiers ---
		if(MaterialTierMin == MaterialTierMax)
			lines.add(Component.translatable("crafting.match_single_tier", MaterialTierMin));
		else if(MaterialTierMax >= 99)
			lines.add(Component.translatable("crafting.match_tiers_above", MaterialTierMin));
		else if(MaterialTierMin == 1)
			lines.add(Component.translatable("crafting.match_tiers_below", MaterialTierMax));
		else
			lines.add(Component.translatable("crafting.match_tiers_between", MaterialTierMin, MaterialTierMax));
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
					&& IsMatchingMaterialType(mat.materialType));
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
	public boolean isEmpty() { return getItems().length == 0; }
	@Override
	@Nonnull
	public ItemStack[] getItems()
	{
		if (CachedMatchingStacks == null)
		{
			List<ItemStack> matching = new ArrayList<>();
			TagKey<Item> matchTagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), MatchTag);
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
							if (IsMatchingMaterialType(matDef.materialType)
								&& matDef.craftingTier >= MaterialTierMin
								&& matDef.craftingTier <= MaterialTierMax)
							{
								matching.add(new ItemStack(partItem));
							}
						}
					}
				}
			}
			CachedMatchingStacks = matching.toArray(new ItemStack[0]);
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
		JsonArray jMatArray = new JsonArray();
		for(EMaterialType materialType : MaterialTypes)
			jMatArray.add(materialType.toString());
		json.add("materials", jMatArray);
		json.addProperty("min", MaterialTierMin);
		json.addProperty("max", MaterialTierMax);
		json.addProperty("tag", MatchTag.toString());
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
			JsonArray array = GsonHelper.getAsJsonArray(json, "materials");

			EMaterialType[] materialTypes = new EMaterialType[array.size()];
			for(int i = 0; i < array.size(); i++)
				materialTypes[i] = EMaterialType.parse(array.get(i).getAsString());

			String tag = GsonHelper.getAsString(json, "tag", "flansmod:generic");
			int min = GsonHelper.getAsInt(json, "min", 1);
			int max = GsonHelper.getAsInt(json, "max", Integer.MAX_VALUE);
			return new TieredPartIngredient(materialTypes, min, max, new ResourceLocation(tag));
		}

		@Override
		public void write(FriendlyByteBuf buffer, TieredPartIngredient ingredient)
		{
			buffer.writeInt(MaterialsToFlags(ingredient.MaterialTypes));
			buffer.writeInt(ingredient.MaterialTierMin);
			buffer.writeInt(ingredient.MaterialTierMax);
			buffer.writeResourceLocation(ingredient.MatchTag);
		}

		@Override
		@Nonnull
		public TieredPartIngredient parse(FriendlyByteBuf buffer)
		{
			int materialFlags = buffer.readInt();
			EMaterialType[] materialTypes = MaterialsFromFlags(materialFlags);
			int min = buffer.readInt();
			int max = buffer.readInt();
			ResourceLocation tag = buffer.readResourceLocation();
			return new TieredPartIngredient(materialTypes, min, max, tag);
		}
	}
}
