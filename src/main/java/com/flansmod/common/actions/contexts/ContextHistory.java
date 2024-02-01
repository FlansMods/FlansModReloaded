package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ContextHistory<T>
{
	@Nonnull
	protected final UUID ID;
	@Nonnull
	private final List<ContextEntry<T>> History;

	public ContextHistory(@Nonnull UUID id)
	{
		ID = id;
		History = new ArrayList<>();
	}

	@Nullable
	public T GetMostRecentValidContext(@Nonnull Function<T, Boolean> validatorFunc)
	{
		for(int i = History.size() - 1; i >= 0; i--)
		{
			if(validatorFunc.apply(History.get(i).Context))
				return History.get(i).Context;
		}
		return null;
	}

	// This function will do its best to return a valid context. These are the possibilities in order:
	//  1. The most recent context is valid and matches your criteria, you will receive this context
	//  2. Your critera were not matched, but your creatorFunc made a valid context, that has been stored and returned
	//  3. Sorry, but your new context was not valid, so we don't store it and return an invalid context
	@Nonnull
	public T GetOrCreate(@Nonnull Function<T, Boolean> validatorFunc, @Nonnull NonNullSupplier<T> creatorFunc, @Nonnull NonNullSupplier<Long> timeFunc)
	{
		//T match = GetMostRecentValidContext(validatorFunc);
		// If our most recent context is not valid, we should push another context onto the stack
		T match = GetMostRecentContext();
		if(match == null || !BasicValidation(match) || !validatorFunc.apply(match))
		{
			match = creatorFunc.get();
			if(!BasicValidation(match) || !validatorFunc.apply(match))
			{
				// We couldn't find an old context, but our new one is not valid either.
				return GetInvalidContext();
			}
			else
			{
				// We made a new valid context, add it to the history.
				History.add(new ContextEntry<>(match, timeFunc.get()));

				if (History.size() == 1)
				{
					FlansMod.LOGGER.info("Context[" + ID + "] seen for the first time as '" + match + "'");
				} else
				{
					ContextEntry<T> previous = History.get(History.size() - 2);
					MarkContextAsOld(previous.Context);
					FlansMod.LOGGER.info("Context[" + ID + "] updated from '" + previous.Context + "' to '" + match + "'");
					if (History.size() > 100)
					{
						FlansMod.LOGGER.error("ContextHistory of [" + ID + "] is overflowing?");
						History.remove(0);
					}
				}

			}
		}
		return match;
	}

	@Nullable
	public T GetMostRecentContext()
	{
		return History.isEmpty() ? null : History.get(History.size() - 1).Context;
	}

	@Nonnull
	protected abstract T GetInvalidContext();

	protected abstract void MarkContextAsOld(@Nonnull T oldContext);

	protected abstract boolean BasicValidation(@Nonnull T check);

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("History of [").append(ID).append("]:\n");
		if(History.size() == 0)
			sb.append("~ No Records ~");
		for (ContextEntry<T> tContextEntry : History)
		{
			sb.append(tContextEntry.toString()).append("\n");
		}
		return sb.toString();
	}

	private static class ContextEntry<T>
	{
		@Nonnull
		private final T Context;
		private final long CreatedTick;

		public ContextEntry(@Nonnull T context, long tick)
		{
			Context = context;
			CreatedTick = tick;
		}

		@Override
		public String toString()
		{
			return CreatedTick + ": " + Context;
		}
	}
}
