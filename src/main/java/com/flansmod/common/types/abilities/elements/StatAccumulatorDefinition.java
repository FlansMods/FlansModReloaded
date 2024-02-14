package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.types.JsonField;

public class StatAccumulatorDefinition
{
	@JsonField
	public EAccumulationOperation operation = EAccumulationOperation.Add;
	@JsonField
	public float value = 0.0f;
	@JsonField
	public EAccumulationSource multiplyPer = EAccumulationSource.One;


}
