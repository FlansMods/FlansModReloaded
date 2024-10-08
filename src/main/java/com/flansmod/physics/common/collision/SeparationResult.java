package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.shapes.IPlane;
import com.flansmod.physics.common.util.shapes.ISeparationAxis;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record SeparationResult(boolean success, @Nonnull IPlane separator, double depth)
{
	@Nonnull
	public static SeparationResult successful(@Nonnull IPlane separator) { return new SeparationResult(true, separator, 0d); }
	@Nonnull
	public static SeparationResult failure(@Nonnull IPlane separator, double depth) { return new SeparationResult(false, separator, depth); }

	@Nonnull
	public static SeparationResult bestFailResult(@Nullable SeparationResult previousBestResult, @Nonnull IPlane newSeparator, double newDepth)
	{
		return bestFailResult(previousBestResult, failure(newSeparator, newDepth));
	}
	@Nonnull
	public static SeparationResult bestFailResult(@Nullable SeparationResult previousBestResult, @Nonnull SeparationResult newResult)
	{
		if(previousBestResult == null)
			return newResult;

		double previousDist = previousBestResult.depth();
		double newDist = newResult.depth();
		if(newDist > previousDist)
			return newResult;
		else
			return previousBestResult;
	}
}