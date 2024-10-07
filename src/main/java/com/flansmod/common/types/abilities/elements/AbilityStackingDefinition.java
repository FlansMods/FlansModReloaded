package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.types.JsonField;
import com.flansmod.physics.common.util.Maths;

import javax.annotation.Nonnull;

public class AbilityStackingDefinition
{
	public boolean IsStackable()
	{
		return !stackingKey.isEmpty();
	}

	@JsonField
	public String stackingKey = "";
	@JsonField
	public int maxStacks = 4;
	@JsonField
	public boolean decayAllAtOnce = false;
	@JsonField
	public float decayTime = 0.0f;

	public int GetDecayTimeTicks(@Nonnull StatCalculationContext ctx) { return Maths.Ceil(GetDecayTimeSeconds(ctx) * 20.0f); }
	public float GetDecayTimeSeconds(@Nonnull StatCalculationContext ctx) { return decayTime; }
}
