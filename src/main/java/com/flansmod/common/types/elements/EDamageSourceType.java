package com.flansmod.common.types.elements;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

// Approximate, for use in editor
public enum EDamageSourceType
{
	Misc,
	Melee,
	Ranged,
	Magic,
	Bullet,
	Fire,
	Explosion,
	Poison,
	Wither,
	Fall,
	Harming;

	public static EDamageSourceType MatchType(DamageSource source)
	{
		if(source.is(DamageTypeTags.IS_FIRE))
			return Fire;
		if(source.is(DamageTypeTags.IS_EXPLOSION))
			return Explosion;
		if(source.is(DamageTypeTags.IS_FALL))
			return Fall;
		if(source.is(DamageTypeTags.WITCH_RESISTANT_TO))
			return Magic;
		if(source.is(DamageTypeTags.IS_PROJECTILE))
			return Ranged;

		return Misc;
	}

}
