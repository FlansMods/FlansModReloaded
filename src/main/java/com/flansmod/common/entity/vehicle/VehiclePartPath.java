package com.flansmod.common.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nonnull;

public record VehiclePartPath(@Nonnull Either<Long, ImmutableList<String>> Source)
{
	public static final int MAX_PATH_DEPTH = 8;
	public static final int MAX_CHILDREN_PER_NODE = 256;

	@Nonnull
	public static VehiclePartPath of(@Nonnull ImmutableList<String> names)
	{
		return new VehiclePartPath(Either.right(names));
	}

	public boolean IsNamed() { return Source.right().isPresent(); }
}
