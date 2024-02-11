package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import net.minecraft.resources.ResourceLocation;
import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class CraftingTraitProviderDefinition
{
	@JsonField
	public ResourceLocation trait = InvalidLocation;
	@JsonField
	public int level = 1;

	private CraftingTraitDefinition TraitDef = null;
	public CraftingTraitDefinition GetAbility()
	{
		if(TraitDef == null)
			TraitDef = FlansMod.TRAITS.Get(trait);
		return TraitDef;
	}
}
