package com.flansmod.common.abilities;

import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;

import javax.annotation.Nonnull;

public class Abilities
{
	public static final IAbilityEffect None = new AbilityEffectNone();

	@Nonnull
	public static IAbilityEffect CreateEffectProcessor(@Nonnull AbilityEffectDefinition def)
	{
		switch(def.effectType)
		{
			case ApplyDamage -> { return new AbilityEffectApplyDamage(def); }
			case ApplyMobEffect -> { return new AbilityEffectApplyMobEffect(def); }
			case ApplyAttribute -> { return new AbilityEffectApplyAttribute(def); }
			case ApplyModifier -> { return new AbilityInstanceApplyModifier(def); }

			case BreakBlock -> { return new AbilityEffectBreakBlock(def); }
			case PlaceBlock -> { return new AbilityEffectPlaceBlock(def); }

			case SummonNpc -> { return new AbilityEffectSummonNpc(def); }
			case SpawnEntity -> { return new AbilityEffectSpawnEntity(def); }
			case DeleteEntity -> { return new AbilityEffectDeleteEntity(def); }
			case HealEntity -> { return new AbilityEffectHealEntity(def); }
			case FeedEntity -> { return new AbilityEffectFeedEntity(def); }

			case StartActionGroup -> { return new AbilityEffectStartActionGroup(def); }


			// Not working yet
			case DamageArmour -> { return new AbilityEffectDamageArmour(def); }
		}
		return None;
	}
}
