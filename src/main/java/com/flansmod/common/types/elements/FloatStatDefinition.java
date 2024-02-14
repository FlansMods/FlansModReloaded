package com.flansmod.common.types.elements;

import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.StatAccumulatorDefinition;

import javax.annotation.Nonnull;

// A low footprint container for a float stat with optional modifiers.
// Note: There is no stat key! This is for use cases where you know the stat name from context
// 		 Generally the stat key is the name of the field
public class FloatStatDefinition
{
	@JsonField
	public float baseValue = 0.0f;
	@JsonField
	public StatAccumulatorDefinition[] additional = new StatAccumulatorDefinition[0];

	public float Calculate(@Nonnull StatCalculationContext ctx)
	{
		if(additional.length == 0)
			return baseValue;

		return FloatModifier.of(ctx, additional).ModifyValue(baseValue);
	}
}
