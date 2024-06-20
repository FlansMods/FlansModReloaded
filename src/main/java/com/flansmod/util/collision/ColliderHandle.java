package com.flansmod.util.collision;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nonnull;

public record ColliderHandle(long Handle)
{
	@Nonnull
	public static Pair<ColliderHandle, ColliderHandle> uniquePairOf(@Nonnull ColliderHandle a, @Nonnull ColliderHandle b)
	{
		if(a.Handle < b.Handle)
			return Pair.of(a, b);
		return Pair.of(b, a);
	}
}
