package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.FloatStatDefinition;
import com.flansmod.util.Maths;

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
	public FloatStatDefinition decayTime = new FloatStatDefinition();
	@JsonField
	public FloatStatDefinition intensity = new FloatStatDefinition();

	public int GetDecayTimeTicks(@Nonnull StatCalculationContext ctx) { return Maths.Ceil(GetDecayTimeSeconds(ctx) * 20.0f); }
	public float GetDecayTimeSeconds(@Nonnull StatCalculationContext ctx) { return decayTime.Calculate(ctx); }
	public float GetIntensity(@Nonnull StatCalculationContext ctx) { return intensity.Calculate(ctx); }
}
