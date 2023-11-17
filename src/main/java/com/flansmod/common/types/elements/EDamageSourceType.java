package com.flansmod.common.types.elements;

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
		if(source.isFire())
			return Fire;
		if(source.isExplosion())
			return Explosion;
		if(source.isFall())
			return Fall;
		if(source.isMagic())
			return Magic;
		if(source.isProjectile())
			return Ranged;

		return Misc;
	}

}
