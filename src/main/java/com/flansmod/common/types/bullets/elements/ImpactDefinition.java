package com.flansmod.common.types.bullets.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;

public class ImpactDefinition
{
	@JsonField
	public EAbilityTarget targetType = EAbilityTarget.ShotEntity;
	@JsonField
	public AbilityEffectDefinition[] impactEffects = new AbilityEffectDefinition[0];
}
