package com.flansmod.common.abilities;

import com.flansmod.common.types.abilities.elements.AbilityStackingDefinition;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;

public class AbilityStack
{
	@Nonnull
	private final AbilityStackingDefinition Def;
	private int StackCount;
	private int TicksSinceStackedOrDecayed;

	public AbilityStack(@Nonnull AbilityStackingDefinition def)
	{
		Def = def;
		StackCount = 0;
		TicksSinceStackedOrDecayed = 0;
	}

	public float GetIntensity() { return Def.GetIntensity(StackCount); }
	public float GetDurationSeconds() { return Def.GetDurationSeconds(StackCount); }
	public int GetDurationTicks() { return Def.GetDurationTicks(StackCount); }
	public boolean IsActive() { return StackCount > 0; }

	public void AddStack()
	{
		TicksSinceStackedOrDecayed = 0;
		StackCount++;
		StackCount = Maths.Clamp(StackCount, 0, Def.maxStacks);
	}

	public void Tick()
	{
		TicksSinceStackedOrDecayed++;
		if(StackCount > 0 && TicksSinceStackedOrDecayed >= Def.GetDecayTimeTicks())
		{
			Decay();
		}
	}

	public void Decay()
	{
		TicksSinceStackedOrDecayed = 0;
		if(Def.decayAllAtOnce)
			StackCount = 0;
		else
			StackCount--;
	}
}
