package com.flansmod.util.formulae;

import javax.annotation.Nonnull;

// Baked accumulated modifiers.
//
public class FloatAccumulation
{
	public final float BaseAdd;
	public final float StackableMultiplier;
	public final float IndependentMultiplier;
	public final float FinalAdd;

	public float get()
	{
		return BaseAdd * (1.0f + StackableMultiplier) * (IndependentMultiplier) + FinalAdd;
	}
	public float apply(float baseValue)
	{
		return (baseValue + BaseAdd) * (1.0f + StackableMultiplier) * (IndependentMultiplier) + FinalAdd;
	}

	private FloatAccumulation(float baseAdd, float stackableMul, float independentMul, float finalAdd)
	{
		BaseAdd = baseAdd;
		StackableMultiplier = stackableMul;
		IndependentMultiplier = independentMul;
		FinalAdd = finalAdd;
	}
	// ---
	// N.B. StackableMul should be relative to 1. So +10% = 0.1, -25% = -0.25
	// 		IndependentMul should be relative to 0. So x110% = 1.1, x75% = 0.75
	@Nonnull
	public static FloatAccumulation of(float baseAdd, float stackableMul, float independentMul, float finalAdd)
	{
		return new FloatAccumulation(baseAdd, stackableMul, independentMul, finalAdd);
	}
	@Nonnull
	public static FloatAccumulation compose(@Nonnull FloatAccumulation... mods)
	{
		float baseAdd = 0f;
		float stackableMul = 0f;
		float independentMul = 1f;
		float finalAdd = 0f;
		for(FloatAccumulation mod : mods)
		{
			baseAdd += mod.BaseAdd;
			stackableMul += mod.StackableMultiplier;
			independentMul *= mod.IndependentMultiplier;
			finalAdd += mod.FinalAdd;
		}
		return new FloatAccumulation(baseAdd, stackableMul, independentMul, finalAdd);
	}

}
