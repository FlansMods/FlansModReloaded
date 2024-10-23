package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.collision.TransformedBBCollection;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.ProjectedRange;
import com.flansmod.physics.common.util.ProjectionUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

import static java.lang.Math.abs;

public interface ISeparationAxis
{
    @Nonnull Vec3 getNormal();
    double project(@Nonnull Vec3 point);

    // These defaults are given mainly to show the layer by layer construction of our projection logic
    // Take a max over any of the box corners, i.e. v = (+/-1, +/-1, +/-1) * halfExtents
    //  Say we pick vMax, then our projection onto the planar normal is as follows:
    //  Axis.x * vMax.x, Axis.y * vMax.y, Axis.z * vMax.z
    // Since we can pick any corner, vMax.x, .y, and .z are all independent, so we can maximise each component
    // Notably, this cannot be negative, but that makes sense. We are centered on the origin
    default double projectOriginBoxMax(@Nonnull Vector3f halfExtents) { return abs(getNormal().x * halfExtents.x) + abs(getNormal().y * halfExtents.y) + abs(getNormal().z * halfExtents.z); }
    default double projectOriginBoxMin(@Nonnull Vector3f halfExtents) { return -(abs(getNormal().x * halfExtents.x) + abs(getNormal().y * halfExtents.y) + abs(getNormal().z * halfExtents.z)); }
    @Nonnull default ProjectedRange projectOriginBox(@Nonnull Vector3f halfExtents)
    {
        return ProjectionUtil.ProjectAABBMinMax(getNormal(), -halfExtents.x, -halfExtents.y, -halfExtents.z, halfExtents.x, halfExtents.y, halfExtents.z);
    }


    // Now we give the box a position. so our corners are v = point + (+/-1, +/-1, +/-1) * halfExtents
    //  The same logic applies as last time, but taking the maximum value isn't just abs value
    //  projection = Axis.x * (pos.x + vMax.x), ...
    //  		   = dot(Axis, pos) + dot(Axis, vMax)
    //  max(dot(A,p)+dot(A,v)) = dot(A,p) + sign(dot(A,p))*abs(dot(A,v)) <- not sure if this is worth optimising
    @Nonnull default ProjectedRange projectAABBMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents)
    {
        return ProjectionUtil.ProjectAABBMinMax(getNormal(),
                point.x - halfExtents.x, point.y - halfExtents.y, point.z - halfExtents.z,
                point.x + halfExtents.x, point.y + halfExtents.y, point.z + halfExtents.z);
    }
    default double projectAABBMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMinMax(point, halfExtents).min(); }
    default double projectAABBMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMinMax(point, halfExtents).max(); }
    @Nonnull default ProjectedRange projectAABBMinMax(@Nonnull AABB aabb) { return ProjectionUtil.ProjectAABBMinMax(getNormal(), aabb); }
    default double projectAABBMin(@Nonnull AABB aabb) { return projectAABBMinMax(aabb).min(); }
    default double projectAABBMax(@Nonnull AABB aabb) { return projectAABBMinMax(aabb).max(); }
    @Nonnull
    default Pair<Double, IPlane> getSeparationPlaneAtoB(@Nonnull TransformedBB a, @Nonnull AABB b)
    {
        ProjectedRange aProjection = projectOBBMinMax(a);
        ProjectedRange bProjection = projectAABBMinMax(b);
        var pair = ProjectionUtil.GetSeparationDistanceAndOrder(aProjection, bProjection);
        return switch(pair.getSecond())
        {
            case Separate_A_Then_B, Colliding_A_Then_B -> Pair.of(pair.getFirst(), Plane.of(getNormal(), aProjection.max()));
            case Separate_B_Then_A, Colliding_B_Then_A -> Pair.of(pair.getFirst(), Plane.of(getNormal().scale(-1d), -aProjection.min()));
        };
    }

    @Nonnull
    default ProjectedRange projectOBBMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
    {
        return ProjectionUtil.ProjectOBBMinMax(getNormal(), point, halfExtents, ori);
    }
    default double projectOBBMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMinMax(point, halfExtents, ori).min(); }
    default double projectOBBMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMinMax(point, halfExtents, ori).max(); }
    @Nonnull default ProjectedRange projectOBBMinMax(@Nonnull TransformedBB obb) { return ProjectionUtil.ProjectOBBMinMax(getNormal(), obb); }
    default double projectOBBMin(@Nonnull TransformedBB obb) { return projectOBBMinMax(obb).min(); }
    default double projectOBBMax(@Nonnull TransformedBB obb) { return projectOBBMinMax(obb).max(); }


    @Nonnull
    default ProjectedRange projectOBBsMinMax(@Nonnull TransformedBBCollection set)
    {
        Matrix3f matrix = set.Location().oriMatrix();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int i = 0; i < set.getCount(); i++)
        {
            ProjectedRange projection = projectOBBMinMax(set.getCenter(i), set.getHalfExtents(i), matrix);
            min = Maths.min(projection.min(), min);
            max = Maths.min(projection.max(), max);
        }
        return ProjectedRange.preSorted(min, max);
    }
    default double projectOBBsMin(@Nonnull TransformedBBCollection set) { return projectOBBsMinMax(set).min(); }
    default double projectOBBsMax(@Nonnull TransformedBBCollection set) { return projectOBBsMinMax(set).max(); }

    default boolean separates(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange projectionA = projectOBBMinMax(a);
        ProjectedRange projectionB = projectOBBMinMax(b);
        return ProjectionUtil.Separated(projectionA, projectionB);
    }
    default boolean separates(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
    {
        ProjectedRange projectionA = projectOBBsMinMax(a);
        ProjectedRange projectionB = projectOBBsMinMax(b);
        return ProjectionUtil.Separated(projectionA, projectionB);
    }
    default double getSeparationDistance(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange aProjection = projectOBBMinMax(a);
        ProjectedRange bProjection = projectOBBMinMax(b);
        return ProjectionUtil.GetSeparationDistance(aProjection, bProjection);
    }
    @Nonnull
    default Pair<Double, IPlane> getSeparationPlaneAtoB(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange aProjection = projectOBBMinMax(a);
        ProjectedRange bProjection = projectOBBMinMax(b);
        var pair = ProjectionUtil.GetSeparationDistanceAndOrder(aProjection, bProjection);
        return switch(pair.getSecond())
        {
            case Separate_A_Then_B, Colliding_A_Then_B -> Pair.of(pair.getFirst(), Plane.of(getNormal(), aProjection.max()));
            case Separate_B_Then_A, Colliding_B_Then_A -> Pair.of(pair.getFirst(), Plane.of(getNormal().scale(-1d), -aProjection.min()));
        };
    }
    default double getSeparationDistance(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
    {
        ProjectedRange aProjection = projectOBBsMinMax(a);
        ProjectedRange bProjection = projectOBBsMinMax(b);
        return ProjectionUtil.GetSeparationDistance(aProjection, bProjection);
    }
    default boolean separatesWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
    {
        ProjectedRange projectedRangeA = projectOBBMinMax(a);
        double projectedMotionA = project(motionA);
        ProjectedRange projectedRangeB = projectOBBMinMax(b);
        double projectedMotionB = project(motionB);
        return ProjectionUtil.SeparatedWithMotion(projectedRangeA, projectedMotionA, projectedRangeB, projectedMotionB);
    }
    default boolean separatesWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
    {
        ProjectedRange projectedRangeA = projectOBBsMinMax(a);
        double projectedMotionA = project(motionA);
        ProjectedRange projectedRangeB = projectOBBsMinMax(b);
        double projectedMotionB = project(motionB);
        return ProjectionUtil.SeparatedWithMotion(projectedRangeA, projectedMotionA, projectedRangeB, projectedMotionB);
    }
    default double getSeparationDistanceWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
    {
        ProjectedRange aProjection = projectOBBMinMax(a);
        double projectedMotionA = project(motionA);
        ProjectedRange bProjection = projectOBBMinMax(b);
        double projectedMotionB = project(motionB);
        return ProjectionUtil.GetSeparationDistanceWithMotion(aProjection, projectedMotionA, bProjection, projectedMotionB);
    }
    default double getSeparationDistanceWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
    {
        ProjectedRange aProjection = projectOBBsMinMax(a);
        double projectedMotionA = project(motionA);
        ProjectedRange bProjection = projectOBBsMinMax(b);
        double projectedMotionB = project(motionB);
        return ProjectionUtil.GetSeparationDistanceWithMotion(aProjection, projectedMotionA, bProjection, projectedMotionB);
    }
}
