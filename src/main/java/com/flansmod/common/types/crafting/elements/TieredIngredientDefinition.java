package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TieredIngredientDefinition
{
	@JsonField(Docs = "Tag will be of the format 'flansmod:items/wing'")
	public String tag;
	@JsonField
	public int minMaterialTier = 1;
	@JsonField
	public int maxMaterialTier = 5;
	@JsonField
	public EMaterialType[] allowedMaterials = new EMaterialType[] { EMaterialType.Misc };




	public boolean is(EMaterialType matType)
	{
		for(EMaterialType allowed : allowedMaterials)
			if(allowed == matType)
				return true;
		return false;
	}

	@Nullable
	private List<MaterialDefinition> CachedMaterialMatches = null;
	@Nonnull
	public List<MaterialDefinition> GetMaterialMatches()
	{
		if(CachedMaterialMatches == null)
		{
			CachedMaterialMatches = FlansMod.MATERIALS.Find((mat) ->
				mat.IsValid()
				&& mat.craftingTier >= minMaterialTier
				&& mat.craftingTier <= maxMaterialTier
				&& this.is(mat.materialType));
		}
		return CachedMaterialMatches;
	}

	@Nullable
	private List<PartDefinition> CachedPartMatches = null;
	@Nonnull
	public List<PartDefinition> GetPartMatches()
	{
		if(CachedPartMatches == null)
		{
			CachedPartMatches = FlansMod.PARTS.Find((part) -> part.IsValid() && Matches(part));
		}
		return CachedPartMatches;
	}


	@Nonnull
	public List<Component> GenerateTooltip(boolean advanced)
	{
		List<Component> lines = new ArrayList<>();

		// --- Match Tag ---
		if(!tag.isEmpty())
			lines.add(Component.translatable("crafting.with_tag", tag));

		// --- Match Materials ---
		if(allowedMaterials.length == 0)
			lines.add(Component.translatable("crafting.match_any_material"));
		else if(allowedMaterials.length == 1)
			lines.add(Component.translatable("crafting.match_single", allowedMaterials[0].ToComponent()));
		else
		{
			Object[] varargs = new Object[allowedMaterials.length];
			for(int n = 0; n < allowedMaterials.length; n++)
				varargs[n] = allowedMaterials[n].ToComponent();
			lines.add(Component.translatable("crafting.match_multiple." + allowedMaterials.length, varargs));
		}

		// --- Match Tiers ---
		if(maxMaterialTier == minMaterialTier)
			lines.add(Component.translatable("crafting.match_single_tier", minMaterialTier));
		else if(minMaterialTier == 1)
			lines.add(Component.translatable("crafting.match_tiers_below", maxMaterialTier));
		else
			lines.add(Component.translatable("crafting.match_tiers_between", minMaterialTier, maxMaterialTier));

		return lines;
	}

	public void GenerateMatches(@Nonnull List<ItemStack> potentialMatches)
	{
		for (PartDefinition part : GetPartMatches())
		{
			Item item = ForgeRegistries.ITEMS.getValue(part.Location);
			if(item != null)
				potentialMatches.add(new ItemStack(item));
		}
	}
	public boolean Matches(@Nonnull MaterialDefinition material)
	{
		return material.IsValid()
			   && material.craftingTier >= minMaterialTier
			   && material.craftingTier <= maxMaterialTier
			   && this.is(material.materialType);
	}
	public boolean Matches(@Nonnull PartDefinition part)
	{
		return Matches(part.GetMaterial())
			&& part.itemSettings.Matches(tag);
	}
	public boolean Matches(@Nonnull ItemStack stack)
	{
		return stack.getItem() instanceof PartItem part && Matches(part.Def());
	}
}
