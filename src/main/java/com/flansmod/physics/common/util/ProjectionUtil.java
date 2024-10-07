package com.flansmod.physics.common.util;

import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.util.shapes.CubeCornerSelection;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ProjectionUtil
{

    // Now we give the box a position. so our corners are v = point + (+/-1, +/-1, +/-1) * halfExtents
    //  The same logic applies as last time, but taking the maximum value isn't just abs value
    //  projection = Axis.x * (pos.x + vMax.x), ...
    //  		   = dot(Axis, pos) + dot(Axis, vMax)
    //  max(dot(A,p)+dot(A,v)) = dot(A,p) + sign(dot(A,p))*abs(dot(A,v)) <- not sure if this is worth optimising

    @Nonnull
    public static ProjectedRange ProjectAABBMinMax(@Nonnull Vec3 projAxis, @Nonnull AABB aabb)
    {
        return ProjectAABBMinMax(projAxis, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }
    @Nonnull
    public static ProjectedRange ProjectAABBMinMax(@Nonnull Vec3 projAxis,
                                                          double xMin, double yMin, double zMin,
                                                          double xMax, double yMax, double zMax)
    {
        double x1 = projAxis.x * xMin, x2 = projAxis.x * xMax;
        double y1 = projAxis.y * yMin, y2 = projAxis.y * yMax;
        double z1 = projAxis.z * zMin, z2 = projAxis.z * zMax;
        return ProjectedRange.preSorted(min(x1, x2) + min(y1, y2) + min(z1, z2), max(x1, x2) + max(y1, y2) + max(z1, z2));
    }
    public static double ProjectAABBMax(@Nonnull Vec3 projAxis, @Nonnull AABB aabb)
    {
        return ProjectAABBMax(projAxis, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }
    public static double ProjectAABBMax(@Nonnull Vec3 projAxis,
                                         double xMin, double yMin, double zMin,
                                         double xMax, double yMax, double zMax)
    {
        return max(projAxis.x * xMin, projAxis.x * xMax)
             + max(projAxis.y * yMin, projAxis.y * yMax)
             + max(projAxis.z * zMin, projAxis.z * zMax);
    }
    public static double ProjectAABBMin(@Nonnull Vec3 projAxis, @Nonnull AABB aabb)
    {
        return ProjectAABBMin(projAxis, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }
    public static double ProjectAABBMin(@Nonnull Vec3 projAxis,
                                         double xMin, double yMin, double zMin,
                                         double xMax, double yMax, double zMax)
    {
        return min(projAxis.x * xMin, projAxis.x * xMax)
             + min(projAxis.y * yMin, projAxis.y * yMax)
             + min(projAxis.z * zMin, projAxis.z * zMax);
    }

    // Last step, we give the box an orientation
    //  Now picking vMax is a little different
    //  Our projection will be:
    //   Axis.x * [pos.x + vMax.x * m00 + vMax.y * m01 + vMax.z * m02] + ...
    //   We can rearrange for vMax
    //   dot(pos, Axis) + vMax.x * [Axis.x * m00 + Axis.y * m10 + Axis.z * m20] + ...
    //  Whaddaya know, its the transpose
    public static double ProjectBoxMin(@Nonnull Vec3 projAxis,
                                       @Nonnull Vec3 point,
                                       @Nonnull Vector3f halfExtents,
                                       @Nonnull Matrix3f ori)
    {
        double projPosX = point.x * projAxis.x;
        double orientedHalfX = halfExtents.x * (projAxis.x * ori.m00 + projAxis.y * ori.m10 + projAxis.z * ori.m20); // * vMin.x either -1 or 1
        double minProjX = min(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMin.x

        double projPosY = point.y * projAxis.y;
        double orientedHalfY = halfExtents.y * (projAxis.x * ori.m01 + projAxis.y * ori.m11 + projAxis.z * ori.m21); // * vMin.y either -1 or 1
        double minProjY = min(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMin.y

        double projPosZ = point.z * projAxis.z;
        double orientedHalfZ = halfExtents.z * (projAxis.x * ori.m02 + projAxis.y * ori.m12 + projAxis.z * ori.m22); // * vMin.z either -1 or 1
        double minProjZ = min(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMin.z

        return minProjX + minProjY + minProjZ;
    }

    public static double ProjectBoxMax(@Nonnull Vec3 projAxis,
                                       @Nonnull Vec3 point,
                                       @Nonnull Vector3f halfExtents,
                                       @Nonnull Matrix3f ori)
    {
        double projPosX = point.x * projAxis.x;
        double orientedHalfX = halfExtents.x * (projAxis.x * ori.m00 + projAxis.y * ori.m10 + projAxis.z * ori.m20); // * vMax.x either -1 or 1
        double maxProjX = max(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMax.x

        double projPosY = point.y * projAxis.y;
        double orientedHalfY = halfExtents.y * (projAxis.x * ori.m01 + projAxis.y * ori.m11 + projAxis.z * ori.m21); // * vMax.y either -1 or 1
        double maxProjY = max(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMax.y

        double projPosZ = point.z * projAxis.z;
        double orientedHalfZ = halfExtents.z * (projAxis.x * ori.m02 + projAxis.y * ori.m12 + projAxis.z * ori.m22); // * vMax.z either -1 or 1
        double maxProjZ = max(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMax.z

        return maxProjX + maxProjY + maxProjZ;
    }
    @Nonnull
    public static ProjectedRange ProjectOBBMinMax(@Nonnull Vec3 projAxis,
                                                        @Nonnull TransformedBB obb)
    {
        return ProjectOBBMinMax(projAxis, obb.GetCenter(), obb.HalfExtents(), obb.Loc().OriMatrix());
    }
    @Nonnull
    public static ProjectedRange ProjectOBBMinMax(@Nonnull Vec3 projAxis,
                                                        @Nonnull Vec3 boxCenter,
                                                        @Nonnull Vector3f halfExtents,
                                                        @Nonnull Matrix3f ori)
    {
        double projPosX = boxCenter.x * projAxis.x;
        double orientedHalfX = halfExtents.x * (projAxis.x * ori.m00 + projAxis.y * ori.m10 + projAxis.z * ori.m20); // * v.x either -1 or 1
        double maxProjX = max(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMax.x
        double minProjX = min(projPosX + orientedHalfX, projPosX - orientedHalfX); // <- Pick the best vMin.x

        double projPosY = boxCenter.y * projAxis.y;
        double orientedHalfY = halfExtents.y * (projAxis.x * ori.m01 + projAxis.y * ori.m11 + projAxis.z * ori.m21); // * v.y either -1 or 1
        double maxProjY = max(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMax.y
        double minProjY = min(projPosY + orientedHalfY, projPosY - orientedHalfY); // <- Pick the best vMin.y

        double projPosZ = boxCenter.z * projAxis.z;
        double orientedHalfZ = halfExtents.z * (projAxis.x * ori.m02 + projAxis.y * ori.m12 + projAxis.z * ori.m22); // * v.z either -1 or 1
        double maxProjZ = max(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMax.z
        double minProjZ = min(projPosZ + orientedHalfZ, projPosZ - orientedHalfZ); // <- Pick the best vMin.z

        return ProjectedRange.preSorted(minProjX + minProjY + minProjZ, maxProjX + maxProjY + maxProjZ);
    }
    @Nonnull
    public static CubeCornerSelection SelectCornerOBBMin(@Nonnull Vec3 projAxis,
                                                         @Nonnull Vec3 boxCenter,
                                                         @Nonnull Vector3f halfExtents,
                                                         @Nonnull Matrix3f ori)
    {
        return SelectCornerOBBMax(projAxis, boxCenter, halfExtents, ori).opposite();
    }
    @Nonnull
    public static CubeCornerSelection SelectCornerOBBMax(@Nonnull Vec3 projAxis,
                                                         @Nonnull Vec3 boxCenter,
                                                         @Nonnull Vector3f halfExtents,
                                                         @Nonnull Matrix3f ori)
    {
        double orientedHalfX = halfExtents.x * (projAxis.x * ori.m00 + projAxis.y * ori.m10 + projAxis.z * ori.m20); // * v.x either -1 or 1
        boolean positiveX = orientedHalfX > 0.0f;
        double orientedHalfY = halfExtents.y * (projAxis.x * ori.m01 + projAxis.y * ori.m11 + projAxis.z * ori.m21); // * v.y either -1 or 1
        boolean positiveY = orientedHalfY > 0.0f;
        double orientedHalfZ = halfExtents.z * (projAxis.x * ori.m02 + projAxis.y * ori.m12 + projAxis.z * ori.m22); // * v.z either -1 or 1
        boolean positiveZ = orientedHalfZ > 0.0f;
        return CubeCornerSelection.of(positiveX, positiveY, positiveZ);
    }
    public static double GetSeparationDistance(@Nonnull ProjectedRange a, @Nonnull ProjectedRange b)
    {
        if(a.min() >= b.max())
            return a.min() - b.max();
        if(b.min() >= a.max())
            return b.min() - a.max();

        // Intersection, distance is negative
        return max(a.min() - b.max(), b.min() - a.max());
    }
    public static double GetSeparationDistanceWithMotion(@Nonnull ProjectedRange a, double motionA, @Nonnull ProjectedRange b, double motionB)
    {
        return GetSeparationDistance(
                ProjectedRange.preSorted(a.min() + motionA, a.max() + motionA),
                ProjectedRange.preSorted(b.min() + motionB, b.max() + motionB));
    }
    public enum ProjectionComparison
    {
        Separate_A_Then_B,
        Separate_B_Then_A,

        Colliding_A_Then_B,
        Colliding_B_Then_A,
    }
    // Pair(distance between A and B, false=A<B or true=B<A)
    @Nonnull
    public static Pair<Double, ProjectionComparison> GetSeparationDistanceAndOrder(@Nonnull ProjectedRange a, @Nonnull ProjectedRange b)
    {
        if(a.min() >= b.max())
            return Pair.of(a.min() - b.max(), ProjectionComparison.Separate_B_Then_A);
        if(b.min() >= a.max())
            return Pair.of(b.min() - a.max(), ProjectionComparison.Separate_A_Then_B);

        // Intersection, distance is negative
        double clipDepthAthenB = a.min() - b.max();
        double clipDepthBthenA = b.min() - a.max();
        if(clipDepthAthenB < clipDepthBthenA)
            return Pair.of(clipDepthAthenB, ProjectionComparison.Colliding_A_Then_B);
        else
            return Pair.of(clipDepthBthenA, ProjectionComparison.Colliding_B_Then_A);
    }
    public static boolean Separated(@Nonnull ProjectedRange a, @Nonnull ProjectedRange b)
    {
        if(a.min() >= b.max())
            return true;
        if(b.min() >= a.max())
            return true;
        return false;
    }
    public static boolean SeparatedWithMotion(@Nonnull ProjectedRange a,
                                              double motionA,
                                              @Nonnull ProjectedRange b,
                                              double motionB)
    {
        if(a.min() + motionA >= b.max() + motionB)
            return true;
        if(b.min() + motionB >= a.max() + motionA)
            return true;
        return false;
    }

}
