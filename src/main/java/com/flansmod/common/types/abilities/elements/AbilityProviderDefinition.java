package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.resources.ResourceLocation;

public class AbilityProviderDefinition
{
	@JsonField
	public String ability = "";
	@JsonField
	public int level = 1;


	private AbilityDefinition AbilityDef = null;
	public AbilityDefinition GetAbility()
	{
		if(AbilityDef == null)
			AbilityDef = FlansMod.ABILITIES.Get(new ResourceLocation(ability));
		return AbilityDef;
	}
}
