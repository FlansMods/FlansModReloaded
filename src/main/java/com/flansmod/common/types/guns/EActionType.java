package com.flansmod.common.types.guns;

public enum EActionType
{
	Invalid,

	PlaySound,			// Implemented
	Animation,			// Implemented
	Shoot,				// Implemented
	AimDownSights,		// Implemented
	Scope,				// Implemented
	CookGrenade,		// Implemented, untested
	Drop,				// Implemented, untested
	Repair,				// TODO: Waiting on vehicles existing
	Heal,				// Implemented, untested
	Feed,				// Implemented, untested
	ApplyPotionEffect,	// TODO:
	Interact,			// TODO:
	BreakBlock,			// Implemented, untested
	PlaceBlock,			// Implemented, untested
	CollectFluid,		// TODO:
	PlaceFluid,			// TODO:
	SpawnEntity,		// Implemented, untested
	DeleteEntity,		// Implemented, untested
	SummonNpc,			// Implemented, untested

	Melee,				// Vanilla, untested
	Shield,				// Vanilla, untested
	Pickaxe,			// Vanilla, untested
	Shovel,				// Vanilla, untested
	Axe,				// Vanilla, untested
	Hoe,				// Vanilla, untested
	Till,				// Vanilla, untested
	Strip,				// Vanilla, untested
	Shear,				// Vanilla, untested
	Flatten,			// Vanilla, untested

}
