package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityStackingDefinition;
import com.flansmod.physics.common.util.Maths;

import javax.annotation.Nonnull;

public class AbilityStack
{
	@Nonnull
	public final AbilityStackingDefinition Def;
	public final int Level;
	private int StackCount;
	private int TicksSinceStackedOrDecayed;

	public AbilityStack(@Nonnull AbilityStackingDefinition def, int level)
	{
		Def = def;
		Level = level;
		StackCount = 0;
		TicksSinceStackedOrDecayed = 0;
	}

	public boolean IsActive() { return StackCount > 0; }
	public int GetStackCount() { return StackCount; }

	public float GetDecayTimeSeconds(@Nonnull GunContext gunContext)
	{
		return Def.GetDecayTimeSeconds(StatCalculationContext.of(Level, StackCount, gunContext));
	}
	public int GetDecayTimeTicks(@Nonnull GunContext gunContext)
	{
		return Def.GetDecayTimeTicks(StatCalculationContext.of(Level, StackCount, gunContext));
	}

	public void AddStack()
	{
		TicksSinceStackedOrDecayed = 0;
		StackCount++;
		StackCount = Maths.Clamp(StackCount, 0, Def.maxStacks);
	}

	public void Tick(@Nonnull GunContext gunContext)
	{
		gunContext.ModifyFloat(Constants.STAT_DECAY_TIME);
		TicksSinceStackedOrDecayed++;
		if(StackCount > 0 && TicksSinceStackedOrDecayed >= GetDecayTimeTicks(gunContext))
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

	public void DecayAll()
	{
		TicksSinceStackedOrDecayed = 0;
		StackCount = 0;
	}



}
