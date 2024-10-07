package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public record Plane(@Nonnull Vec3 Normal, double Distance) implements IPlane
{
    @Nonnull
    public static Plane of(@Nonnull Vec3 normal, double distance) { return new Plane(normal, distance); }
    @Nonnull
    public static Plane ofNormalAndPointOnPlane(@Nonnull Vec3 normal, @Nonnull Vec3 point) { return new Plane(normal, point.dot(normal)); }
    @Nonnull
    public static Plane of(@Nonnull ISeparationAxis axis, double distance) { return new Plane(axis.GetNormal(), distance); }

    @Override
    public double GetDistance() { return Distance; }
    @Override @Nonnull
    public Vec3 GetNormal() { return Normal; }
    @Override
    public double Project(@Nonnull Vec3 point) { return point.dot(Normal); }

    @Override @Nonnull
    public Optional<Vec3> RayPlaneIntersect(@Nonnull Vec3 origin, @Nonnull Vec3 ray)
    {
        // (Origin + t * Ray).V == d
        // Solve for t
        // (R.V)t = d - O.V
        double rDotV = ray.dot(Normal);
        if(Maths.Approx(rDotV, 0d))
            return Optional.empty();

        double oDotV = origin.dot(Normal);
        double t = (Distance - oDotV) / rDotV;
        return Optional.of(origin.add(ray.scale(t)));
    }
}
