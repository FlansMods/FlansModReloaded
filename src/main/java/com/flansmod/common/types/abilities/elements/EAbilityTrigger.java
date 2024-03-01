package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.types.guns.elements.EReloadStage;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;

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

	AddToInventory,		// Not in yet
	RemoveFromInventory,// Not in yet
	Equip,
	Unequip,

	BulletConsumed,
	FirstBulletConsumed,
	LastBulletConsumed,

	RaycastAction,



	;

	public static EAbilityTrigger FromReloadStage(@Nonnull EReloadStage stage)
	{
		return switch(stage)
		{
			case Start -> ReloadStart;
			case Eject -> ReloadEject;
			case LoadOne -> ReloadLoadOne;
			case End ->ReloadEnd;
		};
	}
}
