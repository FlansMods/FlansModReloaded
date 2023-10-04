package com.flansmod.common.types.crafting;

public enum EMaterialIconType
{
	nugget,
	ingot,
	small_sheet,
	sheet,
	block,
	planks,
	thread;

	public String GetIcon()
	{
		return switch (this)
		{
			case nugget -> "\uE790";
			case ingot -> "\uE791";
			case small_sheet -> "\uE792";
			case sheet -> "\uE793";
			case block -> "\uE794";
			case planks -> "\uE795";
			case thread -> "\uE796";
		};
	}
}
