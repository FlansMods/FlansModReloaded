package com.flansmod.util.collision;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nonnull;

public record ColliderHandle(long Handle)
{
	public static final ColliderHandle invalid = new ColliderHandle(0L);

	public boolean IsValid() { return Handle != 0L; }

	@Nonnull
	public static Pair<ColliderHandle, ColliderHandle> uniquePairOf(@Nonnull ColliderHandle a, @Nonnull ColliderHandle b)
	{
		if(a.Handle < b.Handle)
			return Pair.of(a, b);
		return Pair.of(b, a);
	}
}
