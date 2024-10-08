package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ProjectedRange(double min, double max)
{
    @Nonnull
    public static ProjectedRange of(double a, double b) { return new ProjectedRange(Math.min(a, b), Math.max(a, b)); }
    @Nonnull
    public static ProjectedRange preSorted(double min, double max) { return new ProjectedRange(min, max); }

    @Nonnull
    public static ProjectedRange add(@Nullable ProjectedRange existing, double value)
    {
        if(existing == null)
            return ProjectedRange.preSorted(value, value);

        return new ProjectedRange(Math.min(value, existing.min), Math.max(value, existing.max));
    }
    public static double width(@Nullable ProjectedRange range)
    {
        if(range == null)
            return Double.MAX_VALUE;
        return range.max() - range.min();
    }
    public static double clamp(@Nullable ProjectedRange range, double value)
    {
        if(range == null)
            return value;
        if(value <= range.min)
            return range.min;
        if(value >= range.max)
            return range.max;
        return value;
    }

    public double clamp(double value)
    {
        if(value <= min)
            return min;
        if(value >= max)
            return max;
        return value;
    }
}