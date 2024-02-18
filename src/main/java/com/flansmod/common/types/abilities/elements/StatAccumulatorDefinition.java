package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.util.formulae.EAccumulationOperation;

public class StatAccumulatorDefinition
{
	@JsonField
	public EAccumulationOperation operation = EAccumulationOperation.BaseAdd;
	@JsonField
	public float value = 0.0f;
	@JsonField
	public EAccumulationSource[] multiplyPer = new EAccumulationSource[0];
}
