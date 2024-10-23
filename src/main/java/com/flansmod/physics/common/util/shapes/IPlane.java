package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.collision.TransformedBBCollection;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.ProjectionUtil;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IPlane extends ISeparationAxis
{
    // Normal from the parent
    double getDistance();

    @Nonnull Optional<Vec3> rayPlaneIntersect(@Nonnull Vec3 origin, @Nonnull Vec3 ray);

    // Some defaults to give more accessors
    default double getPointHeightAbove(@Nonnull Vec3 point) { return project(point) - getDistance(); }
    default boolean isPointOn(@Nonnull Vec3 point) { return Maths.approx(getPointHeightAbove(point), 0d); }
    default boolean isPointOn(@Nonnull Vec3 point, double epsilon) { return Maths.approx(getPointHeightAbove(point), 0d, epsilon); }
    default boolean isPointOnOrBelow(@Nonnull Vec3 point) { return getPointHeightAbove(point) <= 0d; }
    default boolean isPointOnOrAbove(@Nonnull Vec3 point) { return getPointHeightAbove(point) >= 0d; }
    default boolean isPointApproxBelow(@Nonnull Vec3 point) { return getPointHeightAbove(point) < -Maths.Epsilon; }
    default boolean isPointApproxAbove(@Nonnull Vec3 point) { return getPointHeightAbove(point) > Maths.Epsilon; }
    default boolean isPointApproxBelow(@Nonnull Vec3 point, double epsilon) { return getPointHeightAbove(point) < -epsilon; }
    default boolean isPointApproxAbove(@Nonnull Vec3 point, double epsilon) { return getPointHeightAbove(point) > epsilon; }

    // AABB Defaults
    default boolean isAABBFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMax(point, halfExtents) <= getDistance(); }
    default boolean isAABBFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMin(point, halfExtents) >= getDistance(); }
    default double getAABBHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMin(point, halfExtents) - getDistance(); }
    default double getAABBHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return projectAABBMax(point, halfExtents) - getDistance(); }
    default boolean isAABBFullyBelow(@Nonnull AABB aabb) { return projectAABBMax(aabb) <= getDistance(); }
    default boolean isAABBFullyAbove(@Nonnull AABB aabb) { return projectAABBMin(aabb) >= getDistance(); }
    default double getAABBHeightAbove(@Nonnull AABB aabb) { return projectAABBMin(aabb) - getDistance(); }
    default double getAABBHeightBelow(@Nonnull AABB aabb) { return projectAABBMax(aabb) - getDistance(); }

    // OBB Defaults
    default boolean isOBBFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMin(point, halfExtents, ori) <= getDistance(); }
    default boolean isOBBFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMax(point, halfExtents, ori) >= getDistance(); }
    default double getOBBHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMin(point, halfExtents, ori) - getDistance(); }
    default double getOBBHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return projectOBBMax(point, halfExtents, ori) - getDistance(); }
    default boolean isOBBFullyBelow(@Nonnull TransformedBB bb) { return isOBBFullyBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default boolean isOBBFullyAbove(@Nonnull TransformedBB bb) { return isOBBFullyAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default double getOBBHeightAbove(@Nonnull TransformedBB bb) { return getOBBHeightAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default double getOBBHeightBelow(@Nonnull TransformedBB bb) { return getOBBHeightBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default boolean isOBBsFullyBelow(@Nonnull TransformedBBCollection bbs) { return projectOBBsMax(bbs) <= getDistance(); }
    default boolean isOBBsFullyAbove(@Nonnull TransformedBBCollection bbs) { return projectOBBsMin(bbs) >= getDistance(); }
    default double getOBBsHeightAbove(@Nonnull TransformedBBCollection bbs) { return projectOBBsMin(bbs) - getDistance(); }
    default double getOBBsHeightBelow(@Nonnull TransformedBBCollection bbs) { return projectOBBsMax(bbs) - getDistance(); }


    @Nonnull default Optional<Vec3> linePlaneIntersect(@Nonnull Vec3 pointA, @Nonnull Vec3 pointB) { return rayPlaneIntersect(pointA, pointB.subtract(pointA)); }

    @Nonnull
    default VertexIndex selectCornerOBBMax(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectionUtil.SelectCornerOBBMax(getNormal(), center, halfExtents, ori); }
    @Nonnull
    default VertexIndex selectCornerOBBMin(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectionUtil.SelectCornerOBBMin(getNormal(), center, halfExtents, ori); }
    @Nonnull
    default Direction selectFaceOBBMax(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
    {
        VertexIndex corner = selectCornerOBBMax(center, halfExtents, ori);
        Direction bestDir = null;
        double mostParallel = -1.0d;
        for(Direction dir : corner.getPossibleFaces())
        {
            Vec3i dirNormal = dir.getNormal();
            Vector3f dirNormalF = new Vector3f(dirNormal.getX(), dirNormal.getY(), dirNormal.getZ());
            dirNormalF.mul(ori);
            Vec3 faceNormal = new Vec3(dirNormalF);
            double parallelity = faceNormal.dot(getNormal());
            if(parallelity > mostParallel)
            {
                bestDir = dir;
                mostParallel = parallelity;
            }
        }
        if(bestDir == null)
        {
            FlansPhysicsMod.LOGGER.error("No faces parallel??");
            return Direction.UP;
        }
        return bestDir;
    }
    @Nonnull
    default Direction selectFaceOBBMax(@Nonnull TransformedBB obb) { return selectFaceOBBMax(obb.GetCenter(), obb.HalfExtents(), obb.Loc().oriMatrix()); }
    @Nonnull
    default Direction selectFaceOBBMin(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return selectFaceOBBMax(center, halfExtents, ori).getOpposite(); }
    @Nonnull
    default Direction selectFaceOBBMin(@Nonnull TransformedBB obb) { return selectFaceOBBMax(obb).getOpposite(); }

    @Nonnull
    default Direction selectFaceAABBMin(@Nonnull AABB aabb) { return Direction.getNearest(getNormal().x, getNormal().y, getNormal().z).getOpposite(); }
    @Nonnull
    default Direction selectFaceAABBMax(@Nonnull AABB aabb) { return Direction.getNearest(getNormal().x, getNormal().y, getNormal().z); }

    // This does not return the true Box-Plane intersection, rather a useful incident face to consider for collision
    @Nonnull
    default IPolygon collisionClip(@Nonnull IPolygon incident, @Nonnull IPolygon reference)
    {
        //https://research.ncl.ac.uk/game/mastersdegree/gametechnologies/previousinformation/physics5collisionmanifolds/2017%20Tutorial%205%20-%20Collision%20Manifolds.pdf

        for(int i = 0; i < incident.GetNumVertices(); i++)
        {
            Transform vCurrent = Transform.fromPos(incident.GetVertex(i));
            Vec3 vEdge = incident.GetEdgeVector(i);
            DebugRenderer.renderLine(vCurrent, 4, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), vEdge);
            DebugRenderer.renderPoint(vCurrent, 4, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        }
        for(int i = 0; i < reference.GetNumVertices(); i++)
        {
            Transform vCurrent = Transform.fromPos(reference.GetVertex(i));
            Vec3 vEdge = reference.GetEdgeVector(i);
            DebugRenderer.renderLine(vCurrent, 4, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), vEdge);
            DebugRenderer.renderPoint(vCurrent, 4, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        }

        // Adjacent Face Clipping
        int refVertexCount = reference.GetNumVertices();
        for(int i = 0; i < refVertexCount; i++)
        {
            IPlane clipPlane = reference.GetEdgeClipPlane(getNormal(), i);

            incident = incident.Clip(clipPlane);
        }

        // Final Clipping - simply remove points that are inside our clip plane
        incident = incident.CullClip(reference.GetFaceClipPlane());

        return incident;
    }

    default boolean areOBBsFullyBelow(@Nonnull TransformedBBCollection set)
    {
        Matrix3f mat = set.Location().oriMatrix();
        for(int i = 0; i < set.getCount(); i++)
            if(!isOBBFullyBelow(set.getCenter(i), set.getHalfExtents(i), mat))
                return false;
        return true;
    }
    default boolean areOBBsFullyAbove(@Nonnull TransformedBBCollection set)
    {
        Matrix3f mat = set.Location().oriMatrix();
        for(int i = 0; i < set.getCount(); i++)
            if(!isOBBFullyAbove(set.getCenter(i), set.getHalfExtents(i), mat))
                return false;
        return true;
    }
}
