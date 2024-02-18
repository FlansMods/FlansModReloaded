package com.flansmod.util.formulae;

public enum EAccumulationOperation
{
	// BaseAdd = +X damage
	BaseAdd,
	// StackablePercentage = +XX% damage, enter an actual % value, and numbers will be divided by 100
	//  Two instances of StackablePercentage will combine (e.g. [(+100%] + [+100%] = [+200%])
	StackablePercentage,
	// IndependentPercentage = -XX% damage. These are applied one after the other. So if you want to cut damage in half, use these
	//  (e.g. Stackable[+100%] + Stackable[+50%] + Independent[-25%] + Independent[-50%] = (1 + 100% + 50%) * (1 - 25%) * (1 - 50%) = (2.5) * (0.75) * (0.5)
	IndependentPercentage,
	// FinalAdd after all other boosts, another flat +X damage
	FinalAdd,
}