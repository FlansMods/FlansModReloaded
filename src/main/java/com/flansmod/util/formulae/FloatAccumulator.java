package com.flansmod.util.formulae;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class FloatAccumulator<TInputEnum extends Enum<TInputEnum>, TFormulaType extends FloatFormula<? extends TInputEnum>>
{
	private final TFormulaType BaseAdd;
	private final TFormulaType StackableMultiplier;
	private final List<TFormulaType> IndependentMultipliers;
	private final TFormulaType FinalAdd;

	@Nonnull
	protected abstract TFormulaType FormulaConstructor();

	@Nonnull
	protected abstract TFormulaType ConstEmptyFormula();

	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> CopyFrom(@Nonnull FloatAccumulator<TInputEnum, TFormulaType> other)
	{
		BaseAdd.AddTerms(other.BaseAdd.Terms);
		StackableMultiplier.AddTerms(other.StackableMultiplier.Terms);
		for(int i = 0; i < other.IndependentMultipliers.size(); i++)
		{
			TFormulaType copyFormula = FormulaConstructor();
			copyFormula.AddTerms(other.IndependentMultipliers.get(i).Terms);
			IndependentMultipliers.add(copyFormula);
		}
		FinalAdd.AddTerms(other.FinalAdd.Terms);
		return this;
	}

	public FloatAccumulator()
	{
		BaseAdd = FormulaConstructor();
		StackableMultiplier = FormulaConstructor();
		IndependentMultipliers = new ArrayList<>();
		FinalAdd = FormulaConstructor();
	}

	// ------------------------------------------------------------------------------------------------------
	// Building the formula from components
	// ------------------------------------------------------------------------------------------------------
	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> BaseAdd(float constant)
	{
		BaseAdd.AddTerm(constant);
		return this;
	}

	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> StackableMultiplier(float constant)
	{
		StackableMultiplier.AddTerm(constant);
		return this;
	}

	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> IndependentMultiplier(float constant)
	{
		GetFormulaFor(EAccumulationOperation.IndependentPercentage).AddTerm(constant);
		return this;
	}

	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> FinalAdd(float constant)
	{
		FinalAdd.AddTerm(constant);
		return this;
	}

	@Nonnull
	public FloatAccumulator<TInputEnum, TFormulaType> Bake(@Nonnull TInputEnum inputType, float value)
	{
		int inputTypeIndex = inputType.ordinal();
		BaseAdd.BakeInput(inputTypeIndex, value);
		StackableMultiplier.BakeInput(inputTypeIndex, value);
		for(TFormulaType formula : IndependentMultipliers)
			formula.BakeInput(inputTypeIndex, value);
		FinalAdd.BakeInput(inputTypeIndex, value);
		return this;
	}

	@Nonnull
	protected TFormulaType GetFormulaFor(@Nonnull EAccumulationOperation operation)
	{
		switch (operation)
		{
			case BaseAdd -> {
				return BaseAdd;
			}
			case StackablePercentage -> {
				return StackableMultiplier;
			}
			case FinalAdd -> {
				return FinalAdd;
			}
			// Special case, we need to multiply all these later, so don't stack them additively
			case IndependentPercentage -> {
				TFormulaType newIndependentMul = FormulaConstructor();
				IndependentMultipliers.add(newIndependentMul);
				return newIndependentMul;
			}
			default -> {
				return ConstEmptyFormula();
			}
		}
	}

	// Evaluate all inputs as 0.
	@Nonnull
	public FloatAccumulation GetDefaultValue() { return Evaluate(enumIndex -> 0f); }
	@Nonnull
	public FloatAccumulation Evaluate(@Nonnull Function<Integer, Float> sources)
	{
		float baseAdd = BaseAdd.Evaluate(sources);
		float stackMul = StackableMultiplier.Evaluate(sources);
		float indepMul = 1.0f;
		for(TFormulaType formula : IndependentMultipliers)
			indepMul *= formula.Evaluate(sources);
		float finalAdd = FinalAdd.Evaluate(sources);
		return FloatAccumulation.of(baseAdd, stackMul, indepMul, finalAdd);
	}


}
