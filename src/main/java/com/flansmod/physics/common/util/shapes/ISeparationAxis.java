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
    @Nonnull Vec3 GetNormal();
    double Project(@Nonnull Vec3 point);

    // These defaults are given mainly to show the layer by layer construction of our projection logic
    // Take a max over any of the box corners, i.e. v = (+/-1, +/-1, +/-1) * halfExtents
    //  Say we pick vMax, then our projection onto the planar normal is as follows:
    //  Axis.x * vMax.x, Axis.y * vMax.y, Axis.z * vMax.z
    // Since we can pick any corner, vMax.x, .y, and .z are all independent, so we can maximise each component
    // Notably, this cannot be negative, but that makes sense. We are centered on the origin
    default double ProjectOriginBoxMax(@Nonnull Vector3f halfExtents) { return abs(GetNormal().x * halfExtents.x) + abs(GetNormal().y * halfExtents.y) + abs(GetNormal().z * halfExtents.z); }
    default double ProjectOriginBoxMin(@Nonnull Vector3f halfExtents) { return -(abs(GetNormal().x * halfExtents.x) + abs(GetNormal().y * halfExtents.y) + abs(GetNormal().z * halfExtents.z)); }
    @Nonnull default ProjectedRange ProjectOriginBox(@Nonnull Vector3f halfExtents)
    {
        return ProjectionUtil.ProjectAABBMinMax(GetNormal(), -halfExtents.x, -halfExtents.y, -halfExtents.z, halfExtents.x, halfExtents.y, halfExtents.z);
    }


    // Now we give the box a position. so our corners are v = point + (+/-1, +/-1, +/-1) * halfExtents
    //  The same logic applies as last time, but taking the maximum value isn't just abs value
    //  projection = Axis.x * (pos.x + vMax.x), ...
    //  		   = dot(Axis, pos) + dot(Axis, vMax)
    //  max(dot(A,p)+dot(A,v)) = dot(A,p) + sign(dot(A,p))*abs(dot(A,v)) <- not sure if this is worth optimising
    @Nonnull default ProjectedRange ProjectAABBMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents)
    {
        return ProjectionUtil.ProjectAABBMinMax(GetNormal(),
                point.x - halfExtents.x, point.y - halfExtents.y, point.z - halfExtents.z,
                point.x + halfExtents.x, point.y + halfExtents.y, point.z + halfExtents.z);
    }
    default double ProjectAABBMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMinMax(point, halfExtents).min(); }
    default double ProjectAABBMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMinMax(point, halfExtents).max(); }
    @Nonnull default ProjectedRange ProjectAABBMinMax(@Nonnull AABB aabb) { return ProjectionUtil.ProjectAABBMinMax(GetNormal(), aabb); }
    default double ProjectAABBMin(@Nonnull AABB aabb) { return ProjectAABBMinMax(aabb).min(); }
    default double ProjectAABBMax(@Nonnull AABB aabb) { return ProjectAABBMinMax(aabb).max(); }
    @Nonnull
    default Pair<Double, IPlane> GetSeparationPlaneAtoB(@Nonnull TransformedBB a, @Nonnull AABB b)
    {
        ProjectedRange aProjection = ProjectOBBMinMax(a);
        ProjectedRange bProjection = ProjectAABBMinMax(b);
        var pair = ProjectionUtil.GetSeparationDistanceAndOrder(aProjection, bProjection);
        return switch(pair.getSecond())
        {
            case Separate_A_Then_B, Colliding_A_Then_B -> Pair.of(pair.getFirst(), Plane.of(GetNormal(), aProjection.max()));
            case Separate_B_Then_A, Colliding_B_Then_A -> Pair.of(pair.getFirst(), Plane.of(GetNormal().scale(-1d), -aProjection.min()));
        };
    }

    @Nonnull
    default ProjectedRange ProjectOBBMinMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
    {
        return ProjectionUtil.ProjectOBBMinMax(GetNormal(), point, halfExtents, ori);
    }
    default double ProjectOBBMin(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMinMax(point, halfExtents, ori).min(); }
    default double ProjectOBBMax(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMinMax(point, halfExtents, ori).max(); }
    @Nonnull default ProjectedRange ProjectOBBMinMax(@Nonnull TransformedBB obb) { return ProjectionUtil.ProjectOBBMinMax(GetNormal(), obb); }
    default double ProjectOBBMin(@Nonnull TransformedBB obb) { return ProjectOBBMinMax(obb).min(); }
    default double ProjectOBBMax(@Nonnull TransformedBB obb) { return ProjectOBBMinMax(obb).max(); }


    @Nonnull
    default ProjectedRange ProjectOBBsMinMax(@Nonnull TransformedBBCollection set)
    {
        Matrix3f matrix = set.Location().oriMatrix();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int i = 0; i < set.getCount(); i++)
        {
            ProjectedRange projection = ProjectOBBMinMax(set.getCenter(i), set.getHalfExtents(i), matrix);
            min = Maths.Min(projection.min(), min);
            max = Maths.Min(projection.max(), max);
        }
        return ProjectedRange.preSorted(min, max);
    }

    default boolean Separates(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange projectionA = ProjectOBBMinMax(a);
        ProjectedRange projectionB = ProjectOBBMinMax(b);
        return ProjectionUtil.Separated(projectionA, projectionB);
    }
    default boolean Separates(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
    {
        ProjectedRange projectionA = ProjectOBBsMinMax(a);
        ProjectedRange projectionB = ProjectOBBsMinMax(b);
        return ProjectionUtil.Separated(projectionA, projectionB);
    }
    default double GetSeparationDistance(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange aProjection = ProjectOBBMinMax(a);
        ProjectedRange bProjection = ProjectOBBMinMax(b);
        return ProjectionUtil.GetSeparationDistance(aProjection, bProjection);
    }
    @Nonnull
    default Pair<Double, IPlane> GetSeparationPlaneAtoB(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
    {
        ProjectedRange aProjection = ProjectOBBMinMax(a);
        ProjectedRange bProjection = ProjectOBBMinMax(b);
        var pair = ProjectionUtil.GetSeparationDistanceAndOrder(aProjection, bProjection);
        return switch(pair.getSecond())
        {
            case Separate_A_Then_B, Colliding_A_Then_B -> Pair.of(pair.getFirst(), Plane.of(GetNormal(), aProjection.max()));
            case Separate_B_Then_A, Colliding_B_Then_A -> Pair.of(pair.getFirst(), Plane.of(GetNormal().scale(-1d), -aProjection.min()));
        };
    }
    default double GetSeparationDistance(@Nonnull TransformedBBCollection a, @Nonnull TransformedBBCollection b)
    {
        ProjectedRange aProjection = ProjectOBBsMinMax(a);
        ProjectedRange bProjection = ProjectOBBsMinMax(b);
        return ProjectionUtil.GetSeparationDistance(aProjection, bProjection);
    }
    default boolean SeparatesWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
    {
        ProjectedRange projectedRangeA = ProjectOBBMinMax(a);
        double projectedMotionA = Project(motionA);
        ProjectedRange projectedRangeB = ProjectOBBMinMax(b);
        double projectedMotionB = Project(motionB);
        return ProjectionUtil.SeparatedWithMotion(projectedRangeA, projectedMotionA, projectedRangeB, projectedMotionB);
    }
    default boolean SeparatesWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
    {
        ProjectedRange projectedRangeA = ProjectOBBsMinMax(a);
        double projectedMotionA = Project(motionA);
        ProjectedRange projectedRangeB = ProjectOBBsMinMax(b);
        double projectedMotionB = Project(motionB);
        return ProjectionUtil.SeparatedWithMotion(projectedRangeA, projectedMotionA, projectedRangeB, projectedMotionB);
    }
    default double GetSeparationDistanceWithMotion(@Nonnull TransformedBB a, @Nonnull Vec3 motionA, @Nonnull TransformedBB b, @Nonnull Vec3 motionB)
    {
        ProjectedRange aProjection = ProjectOBBMinMax(a);
        double projectedMotionA = Project(motionA);
        ProjectedRange bProjection = ProjectOBBMinMax(b);
        double projectedMotionB = Project(motionB);
        return ProjectionUtil.GetSeparationDistanceWithMotion(aProjection, projectedMotionA, bProjection, projectedMotionB);
    }
    default double GetSeparationDistanceWithMotion(@Nonnull TransformedBBCollection a, @Nonnull Vec3 motionA, @Nonnull TransformedBBCollection b, @Nonnull Vec3 motionB)
    {
        ProjectedRange aProjection = ProjectOBBsMinMax(a);
        double projectedMotionA = Project(motionA);
        ProjectedRange bProjection = ProjectOBBsMinMax(b);
        double projectedMotionB = Project(motionB);
        return ProjectionUtil.GetSeparationDistanceWithMotion(aProjection, projectedMotionA, bProjection, projectedMotionB);
    }
}
