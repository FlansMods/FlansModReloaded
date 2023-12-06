package com.flansmod.common.abilities;

import com.flansmod.common.types.abilities.AbilityDefinition;

import javax.annotation.Nonnull;

public class Abilities
{
	public static AbilityInstance InstanceAbility(@Nonnull AbilityDefinition def, int level)
	{
		switch(def.effectType)
		{
			case ApplyAttributeModifier -> { return new ApplyAttributeAbility(def, level); }
			case ApplyMobEffect -> { return new ApplyEffectAbility(def, level); }
			case ApplyDamage -> { return new ApplyDamageAbility(def, level); }
			case DamageArmour -> { return new DamageArmourAbility(def, level); }
			case PerformAction -> { return new PerformActionAbility(def, level); }
		}
		return null;
	}
}
