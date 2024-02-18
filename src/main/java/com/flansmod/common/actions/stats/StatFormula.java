package com.flansmod.common.actions.stats;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.elements.EAccumulationSource;
import com.flansmod.util.formulae.FloatFormula;

import javax.annotation.Nonnull;

public class StatFormula extends FloatFormula<EAccumulationSource>
{
	public StatFormula AtLevel(int level)
	{
		BakeInput(EAccumulationSource.PerLevel, level);
		return this;
	}
	public StatFormula WithStacks(int stackCount)
	{
		BakeInput(EAccumulationSource.PerStacks, stackCount);
		return this;
	}
	public float Evaluate(@Nonnull IStatCalculatorContext ctx)
	{
		return Evaluate((statIndex) -> {
			switch(EAccumulationSource.values()[statIndex]) {
				case PerLevel, PerStacks:
					FlansMod.LOGGER.error("Too late to bake in trait level or stack count");
					return 1f;
				case PerAttachment: return (float)ctx.GetNumAttachments();
				case PerMagFullness: return ctx.GetMagFullness();
				case PerMagEmptiness: return 1.0f - ctx.GetMagFullness();
				default:
					FlansMod.LOGGER.error("I don't know what this input is");
					return 0f;
			}
		});
	}
}
