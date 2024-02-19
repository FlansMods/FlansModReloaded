package com.flansmod.common.actions.stats;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.elements.EAccumulationSource;
import com.flansmod.common.types.abilities.elements.StatAccumulatorDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.formulae.FloatAccumulation;
import com.flansmod.util.formulae.FloatAccumulator;

import javax.annotation.Nonnull;

// A composed set of modifier functions for a single Float parameter
//  BaseAdd and FinalAdd are just additives. They stack additively.
//  StackableMultiplier is stored relative to 1.0, so see it as +10% or -40%. It stacks additvely.
//  IndependentMultiplier is stored as a flat multiplier. It stacks multiplicatively
//  	Formula is: (input + BaseAdd) * (1 + StackableMultiplier) * IndependentMultiplier + FinalAdd
public class StatAccumulator extends FloatAccumulator<EAccumulationSource, StatFormula>
{
	public final static StatAccumulator EmptyAccumulator = new StatAccumulator();
	public final static StatFormula EmptyFormula = new StatFormula();

	public StatAccumulator() { }

	@Nonnull @Override
	protected StatFormula FormulaConstructor() { return new StatFormula(); }
	@Nonnull @Override
	protected StatFormula ConstEmptyFormula() { return EmptyFormula; }
	@Nonnull
	public StatAccumulator CopyFrom(@Nonnull StatAccumulator other) { return (StatAccumulator)super.CopyFrom(other); }
	@Nonnull
	public StatAccumulator Bake(@Nonnull EAccumulationSource inputType, float value) { return (StatAccumulator)super.Bake(inputType, value); }


	// ------------------------------------------------------------------------------------------------------
	// Building the formula from components
	// ------------------------------------------------------------------------------------------------------
	@Nonnull
	public StatAccumulator Stack(@Nonnull ModifierDefinition... mods)
	{
		for(ModifierDefinition mod : mods)
			for(StatAccumulatorDefinition accumulatorDef : mod.accumulators)
			{
				GetFormulaFor(accumulatorDef.operation)
					.AddTerm(accumulatorDef.GetValue(), accumulatorDef.multiplyPer);
			}
		return this;
	}
	@Nonnull
	public StatAccumulator Stack(@Nonnull StatAccumulatorDefinition... accumulators)
	{
		for(StatAccumulatorDefinition accumulatorDef : accumulators)
		{
			GetFormulaFor(accumulatorDef.operation)
				.AddTerm(accumulatorDef.GetValue(), accumulatorDef.multiplyPer);
		}

		return this;
	}
	@Nonnull
	public StatAccumulator Stack(int level, int stackCount, @Nonnull StatAccumulatorDefinition... accumulators)
	{
		for(StatAccumulatorDefinition accumulatorDef : accumulators)
		{
			GetFormulaFor(accumulatorDef.operation)
				.AddTerm(accumulatorDef.GetValue(), accumulatorDef.multiplyPer)
				.BakeInput(EAccumulationSource.PerLevel, level)
				.BakeInput(EAccumulationSource.PerStacks, stackCount);
		}

		return this;
	}
	@Nonnull
	public StatAccumulator Stack(int level, int stackCount, @Nonnull StatAccumulatorDefinition accumulatorDef)
	{
		GetFormulaFor(accumulatorDef.operation)
			.AddTerm(accumulatorDef.GetValue(), accumulatorDef.multiplyPer)
			.BakeInput(EAccumulationSource.PerLevel, level)
			.BakeInput(EAccumulationSource.PerStacks, stackCount);

		return this;
	}
	// ------------------------------------------------------------------------------------------------------
	public StatAccumulator CopyWithLevelAndStacks(int level, int stackCount)
	{
		return new StatAccumulator()
			.CopyFrom(this)
			.Bake(EAccumulationSource.PerLevel, level)
			.Bake(EAccumulationSource.PerStacks, stackCount);
	}

	@Nonnull
	public FloatAccumulation Calculate(@Nonnull IStatCalculatorContext ctx)
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
