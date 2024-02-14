package com.flansmod.common.gunshots;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.types.abilities.elements.StatAccumulatorDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;

public class FloatModifier
{
	public static final FloatModifier NoMod = new FloatModifier(0f, 0f, 0f, 0f);

	private final float Base;
	private final float Added;
	private final float Multiplier;
	private final float Exponent;

	private FloatModifier(float base, float added, float multiplier, float exponent)
	{
		Base = base;
		Added = added;
		Multiplier = multiplier;
		Exponent = exponent;
	}
	@Nonnull
	public static FloatModifier of(@Nonnull FloatModifier a, @Nonnull FloatModifier b)
	{
		return new FloatModifier(a.Base + b.Base, a.Added + b.Added, a.Multiplier + b.Multiplier, a.Exponent + b.Exponent);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull FloatModifier... mods)
	{
		float base = 0f;
		float added = 0f;
		float multiplier = 0f;
		float exponent = 0f;
		for(FloatModifier mod : mods)
		{
			base += mod.Base;
			added += mod.Added;
			multiplier += mod.Multiplier;
			exponent += mod.Exponent;
		}
		return new FloatModifier(base, added, multiplier, exponent);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull StatCalculationContext ctx, @Nonnull ModifierDefinition... mods)
	{
		return of(ctx, NoMod, mods);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull StatCalculationContext ctx, @Nonnull StatAccumulatorDefinition... accumulators)
	{
		return of(ctx, NoMod, accumulators);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull ModifierDefinition modifier, @Nonnull StatCalculationContext ctx)
	{
		return of(modifier, ctx, NoMod);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull ModifierDefinition modifier, @Nonnull StatCalculationContext ctx, @Nonnull FloatModifier appendTo)
	{
		return of(ctx, appendTo, modifier);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull StatCalculationContext ctx, @Nonnull FloatModifier appendTo, @Nonnull ModifierDefinition... mods)
	{
		float base = appendTo.Base;
		float added = appendTo.Added;
		float multiplier = appendTo.Multiplier;
		float exponent = appendTo.Exponent;
		for(ModifierDefinition mod : mods)
		{
			for (StatAccumulatorDefinition accumulator : mod.accumulators)
			{
				float sourceModifier = GetSourceModifier(accumulator, ctx);

				switch (accumulator.operation)
				{
					case Constant -> base = accumulator.value * sourceModifier;
					case Add -> added += accumulator.value * sourceModifier;
					case Multiply -> multiplier += accumulator.value * sourceModifier;
					case Exponent -> exponent += accumulator.value * sourceModifier;
				}
			}
		}
		return new FloatModifier(base, added, multiplier, exponent);
	}
	@Nonnull
	public static FloatModifier of(@Nonnull StatCalculationContext ctx, @Nonnull FloatModifier appendTo, @Nonnull StatAccumulatorDefinition... accumulators)
	{
		float base = appendTo.Base;
		float added = appendTo.Added;
		float multiplier = appendTo.Multiplier;
		float exponent = appendTo.Exponent;
		for (StatAccumulatorDefinition accumulator : accumulators)
		{
			float sourceModifier = GetSourceModifier(accumulator, ctx);

			switch (accumulator.operation)
			{
				case Constant -> base = accumulator.value * sourceModifier;
				case Add -> added += accumulator.value * sourceModifier;
				case Multiply -> multiplier += accumulator.value * sourceModifier;
				case Exponent -> exponent += accumulator.value * sourceModifier;
			}
		}
		return new FloatModifier(base, added, multiplier, exponent);
	}
	public static float GetSourceModifier(@Nonnull StatAccumulatorDefinition accumulator, @Nonnull StatCalculationContext ctx)
	{
		return switch (accumulator.multiplyPer) {
			case One -> 1f;
			case PerStacks -> ctx.StackCount;
			case PerLevel -> ctx.Level;
			case PerAttachment -> ctx.NumAttachments.get();
			case PerMagFullness -> ctx.MagFullnessRatio.get();
			case PerMagEmptiness -> 1.0f - ctx.MagFullnessRatio.get();
		};
	}

	// You can use this as your only source
	public float GetValue() { return ModifyValue(Base); }
	// Or you can provide some value you want to modify
	public float ModifyValue(float baseValue)
	{
		if(Exponent != 0.0f)
			return baseValue + (Added * Maths.PowF((1.0f + Multiplier), (1.0f + Exponent)));

		return baseValue + (Added * (1.0f + Multiplier));
	}
}
