package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModifierCache
{
	private static final int INVALID_CACHE = -1;
	private int CachedHash = INVALID_CACHE;
	private final Map<String, FloatModifier> FloatModifiers = new HashMap<>();
	private final Map<String, String> StringModications = new HashMap<>();


	@Nonnull
	public Function<String, FloatModifier> GetFloatModifierLookup()
	{
		return (stat) -> FloatModifiers.getOrDefault(stat, FloatModifier.NoMod);
	}
	@Nonnull
	public Function<String, String> GetStringModificationLookup()
	{
		return (stat) -> StringModications.getOrDefault(stat, null);
	}

	public int ModifierHash()
	{
		return CachedHash;
	}

	public void Clear()
	{
		FloatModifiers.clear();
		StringModications.clear();
		CachedHash = INVALID_CACHE;
	}

	public void ApplyModifier(@Nonnull ModifierDefinition mod, @Nonnull StatCalculationContext ctx)
	{
		if(CachedHash == INVALID_CACHE)
		{
			// Is this a float modifier?
			if (mod.accumulators.length > 0)
			{
				FloatModifier existing = FloatModifiers.getOrDefault(mod.stat, FloatModifier.NoMod);
				FloatModifiers.put(mod.stat, FloatModifier.of(mod, ctx, existing));
			}
			if (!mod.setValue.isEmpty())
			{
				StringModications.put(mod.stat, mod.setValue);
			}
		}
		else FlansMod.LOGGER.error("Can't modify a finished ModifierCache");
	}

	public void CompleteCaching(int hash)
	{
		CachedHash = hash;
	}
}
