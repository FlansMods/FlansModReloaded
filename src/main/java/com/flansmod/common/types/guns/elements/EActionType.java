package com.flansmod.common.types.guns.elements;

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
	Raycast,			// > Use this to pick a target for arbitrary abilities <
	Heal,				// OLD - Moved to Abilities
	Feed,				// OLD - Moved to Abilities
	ApplyPotionEffect,	// OLD - Moved to Abilities
	Interact,			// OLD - Moved to Abilities
	BreakBlock,			// OLD - Moved to Abilities
	PlaceBlock,			// OLD - Moved to Abilities
	CollectFluid,		// OLD - Moved to Abilities
	PlaceFluid,			// OLD - Moved to Abilities
	Repair,				// OLD - Moved to Abilities
	DeleteEntity,		// OLD - Moved to Abilities
	SummonNpc,			// OLD - Moved to Abilities

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

	AttachEffect,		// TODO:
	SpawnParticle,		// TODO:
	Laser,				// Implemented. You need this on your gun to render a laser effect
	SwitchMode,			// Implemented. Changes a mode on your gun

	AbilityOnSelf, EjectCasing,
}
