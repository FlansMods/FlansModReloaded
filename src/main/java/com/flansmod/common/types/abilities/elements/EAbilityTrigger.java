package com.flansmod.common.types.abilities.elements;

public enum EAbilityTrigger
{
	AlwaysOn,
	Instant,

	ShotEntity,
	ShotBlock,
	ShotAndBrokeBlock,
	ShotHeadshot,

	ReloadStart,
	ReloadEject,
	ReloadLoadOne,
	ReloadEnd,

	StartActionGroup,
	TriggerActionGroup,
	EndActionGroup,
	SwitchMode,

	AddToInventory,
	RemoveFromInventory,
	Equip,
	Unequip,


}
