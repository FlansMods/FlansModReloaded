package com.flansmod.util.formulae;

import com.flansmod.common.FlansMod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FloatFormula<TInputEnum extends Enum<TInputEnum>>
{
	protected static class Term
	{
		public float Coefficient;
		public int Flags;

		public Term(float coeff, int flags)
		{
			Coefficient = coeff;
			Flags = flags;
		}
	}

	protected final List<Term> Terms;

	public FloatFormula()
	{
		Terms = new ArrayList<>();
	}

	public void AddTerms(@Nonnull Collection<Term> terms)
	{
		for(Term term : terms)
		{
			Terms.add(new Term(term.Coefficient, term.Flags));
		}
	}
	@Nonnull
	public FloatFormula<TInputEnum> BakeInput(int inputTypeIndex, float value)
	{
		int inputFlag = 1 << inputTypeIndex;
		for(Term term : Terms)
		{
			if((term.Flags & inputTypeIndex) != 0)
			{
				term.Coefficient *= value;
				term.Flags ^= inputTypeIndex;
			}
		}
		return this;
	}
	@Nonnull
	public FloatFormula<TInputEnum> BakeInput(@Nonnull TInputEnum inputType, float value)
	{
		return BakeInput(inputType.ordinal(), value);
	}
	public float Evaluate(@Nonnull Function<Integer, Float> resolver)
	{
		float value = 0.0f;
		for(Term term : Terms)
		{
			float termValue = term.Coefficient;
			int remainingFlags = term.Flags;
			int i = 0;
			while(remainingFlags != 0)
			{
				boolean hasFlagI = (remainingFlags & 0x1) != 0;
				if(hasFlagI)
					termValue *= resolver.apply(i);
				i++;
				remainingFlags >>= 1;
			}
			value += termValue;
		}
		return value;
	}
	public float Evaluate()
	{
		float value = 0.0f;
		for(Term term : Terms)
		{
			if(term.Flags != 0)
			{
				FlansMod.LOGGER.error("Called Evaluate on a FloatFormula with coeffecients outstanding and no resolver, these will be assumed to be 0.");
			}
			value += term.Coefficient;
		}
		return value;
	}

	@SafeVarargs
	public final FloatFormula<TInputEnum> AddTerm(float coeff, @Nonnull TInputEnum... inputs)
	{
		int flags = 0;
		for(TInputEnum input : inputs)
		{
			flags |= (1 << input.ordinal());
			if(input.ordinal() > 32)
				FlansMod.LOGGER.error("Trying to add a "+input+" term to a formula, over 32 max.");
		}
		for(Term term : Terms)
		{
			if (term.Flags == flags)
			{
				term.Coefficient += coeff;
				return this;
			}
		}
		Terms.add(new Term(coeff, flags));
		return this;
	}

	public float Calculate(float... inputs)
	{
		float sum = 0.0f;
		for(Term term : Terms)
		{
			float add = term.Coefficient;
			for(int i = 0; i < inputs.length; i++)
				if(((term.Flags << i) & 0x1) != 0)
					add *= inputs[i];
			sum += add;
		}
		return sum;
	}

	@SafeVarargs
	public final float Calculate(@Nonnull Supplier<Float>... inputs)
	{
		float sum = 0.0f;
		for(Term term : Terms)
		{
			float add = term.Coefficient;
			for(int i = 0; i < inputs.length; i++)
				if(((term.Flags << i) & 0x1) != 0)
					add *= inputs[i].get();
			sum += add;
		}
		return sum;
	}
}
