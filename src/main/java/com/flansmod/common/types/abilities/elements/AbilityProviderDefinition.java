package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.resources.ResourceLocation;
import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class AbilityProviderDefinition
{
	@JsonField
	public ResourceLocation ability = InvalidLocation;
	@JsonField
	public int level = 1;

	private AbilityDefinition AbilityDef = null;
	public AbilityDefinition GetAbility()
	{
		if(AbilityDef == null)
			AbilityDef = FlansMod.ABILITIES.Get(ability);
		return AbilityDef;
	}
}
