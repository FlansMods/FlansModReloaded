package com.flansmod.common.types.abilities.elements;

public enum EAbilityEffect
{
	Nothing,

	ApplyAttribute,
	ApplyMobEffect,
	ApplyDamage,
	DamageArmour,

	StartActionGroup,
	ApplyModifier,

	TotemOfUndying,

	DeleteEntity,			// Deletes an entity - DANGEROUS
	SpawnEntity,			// Spawns an entity
	HealEntity,				// Heals the target entity
	FeedEntity,				// Feeds the target entity
	SummonNpc,				// Summons a Flan's Mod NPC / vendor

	Interact, 				// A vanilla interact - TODO
	RepairVehicle,			// Waiting on vehicles - TODO

	BreakBlock,
	PlaceBlock,
	CollectFluid,
	PlaceFluid,

	EndActionGroup,
}
