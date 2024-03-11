package com.flansmod.common.types.armour;

import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nonnull;

public enum EArmourType
{
	Feet,
	Legs,
	Chest,
	Head;

	@Nonnull
	public EquipmentSlot ToSlot()
	{
		return switch(this) {
			case Feet -> EquipmentSlot.FEET;
			case Legs -> EquipmentSlot.LEGS;
			case Chest -> EquipmentSlot.CHEST;
			case Head -> EquipmentSlot.HEAD;
		};
	}

}
