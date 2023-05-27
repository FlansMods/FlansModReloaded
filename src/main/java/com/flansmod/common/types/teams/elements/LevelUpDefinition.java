package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;

public class LevelUpDefinition
{
	@JsonField
	public PaintjobUnlockDefinition[] paintjobs = new PaintjobUnlockDefinition[0];
	@JsonField
	public ItemUnlockDefinition[] items = new ItemUnlockDefinition[0];

	@JsonField(Docs = "Leave at -1 for no slot unlock")
	public int unlockSlot = -1;
	@JsonField(Docs = "This is the total from the previous level to this one, not cumulative from level 1")
	public int xpToLevel = 100;
}
