package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;

public record ProjectedRange(double min, double max)
{
    @Nonnull
    public static ProjectedRange of(double a, double b) { return new ProjectedRange(Math.min(a, b), Math.max(a, b)); }
    @Nonnull
    public static ProjectedRange preSorted(double min, double max) { return new ProjectedRange(min, max); }

}