package com.flansmod.physics.common.util.shapes;

import com.flansmod.client.render.debug.DebugRenderer;
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
    double GetDistance();

    @Nonnull Optional<Vec3> RayPlaneIntersect(@Nonnull Vec3 origin, @Nonnull Vec3 ray);

    // Some defaults to give more accessors
    default double GetPointHeightAbove(@Nonnull Vec3 point) { return Project(point) - GetDistance(); }
    default boolean IsPointOn(@Nonnull Vec3 point) { return Maths.approx(GetPointHeightAbove(point), 0d); }
    default boolean IsPointOn(@Nonnull Vec3 point, double epsilon) { return Maths.approx(GetPointHeightAbove(point), 0d, epsilon); }
    default boolean IsPointOnOrBelow(@Nonnull Vec3 point) { return GetPointHeightAbove(point) <= 0d; }
    default boolean IsPointOnOrAbove(@Nonnull Vec3 point) { return GetPointHeightAbove(point) >= 0d; }
    default boolean IsPointApproxBelow(@Nonnull Vec3 point) { return GetPointHeightAbove(point) < -Maths.Epsilon; }
    default boolean IsPointApproxAbove(@Nonnull Vec3 point) { return GetPointHeightAbove(point) > Maths.Epsilon; }
    default boolean IsPointApproxBelow(@Nonnull Vec3 point, double epsilon) { return GetPointHeightAbove(point) < -epsilon; }
    default boolean IsPointApproxAbove(@Nonnull Vec3 point, double epsilon) { return GetPointHeightAbove(point) > epsilon; }

    // AABB Defaults
    default boolean IsAABBFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMax(point, halfExtents) <= GetDistance(); }
    default boolean IsAABBFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMin(point, halfExtents) >= GetDistance(); }
    default double GetAABBHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMin(point, halfExtents) - GetDistance(); }
    default double GetAABBHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents) { return ProjectAABBMax(point, halfExtents) - GetDistance(); }
    default boolean IsAABBFullyBelow(@Nonnull AABB aabb) { return ProjectAABBMax(aabb) <= GetDistance(); }
    default boolean IsAABBFullyAbove(@Nonnull AABB aabb) { return ProjectAABBMin(aabb) >= GetDistance(); }
    default double GetAABBHeightAbove(@Nonnull AABB aabb) { return ProjectAABBMin(aabb) - GetDistance(); }
    default double GetAABBHeightBelow(@Nonnull AABB aabb) { return ProjectAABBMax(aabb) - GetDistance(); }

    // OBB Defaults
    default boolean IsOBBFullyBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMin(point, halfExtents, ori) <= GetDistance(); }
    default boolean IsOBBFullyAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMax(point, halfExtents, ori) >= GetDistance(); }
    default double GetOBBHeightAbove(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMin(point, halfExtents, ori) - GetDistance(); }
    default double GetOBBHeightBelow(@Nonnull Vec3 point, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectOBBMax(point, halfExtents, ori) - GetDistance(); }
    default boolean IsOBBFullyBelow(@Nonnull TransformedBB bb) { return IsOBBFullyBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default boolean IsOBBFullyAbove(@Nonnull TransformedBB bb) { return IsOBBFullyAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default double GetOBBHeightAbove(@Nonnull TransformedBB bb) { return GetOBBHeightAbove(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }
    default double GetOBBHeightBelow(@Nonnull TransformedBB bb) { return GetOBBHeightBelow(bb.GetCenter(), bb.HalfExtents(), bb.Loc().oriMatrix()); }

    @Nonnull default Optional<Vec3> LinePlaneIntersect(@Nonnull Vec3 pointA, @Nonnull Vec3 pointB) { return RayPlaneIntersect(pointA, pointB.subtract(pointA)); }

    @Nonnull
    default VertexIndex SelectCornerOBBMax(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectionUtil.SelectCornerOBBMax(GetNormal(), center, halfExtents, ori); }
    @Nonnull
    default VertexIndex SelectCornerOBBMin(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return ProjectionUtil.SelectCornerOBBMin(GetNormal(), center, halfExtents, ori); }
    @Nonnull
    default Direction SelectFaceOBBMax(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori)
    {
        VertexIndex corner = SelectCornerOBBMax(center, halfExtents, ori);
        Direction bestDir = null;
        double mostParallel = -1.0d;
        for(Direction dir : corner.getPossibleFaces())
        {
            Vec3i dirNormal = dir.getNormal();
            Vector3f dirNormalF = new Vector3f(dirNormal.getX(), dirNormal.getY(), dirNormal.getZ());
            dirNormalF.mul(ori);
            Vec3 faceNormal = new Vec3(dirNormalF);
            double parallelity = faceNormal.dot(GetNormal());
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
    default Direction SelectFaceOBBMax(@Nonnull TransformedBB obb) { return SelectFaceOBBMax(obb.GetCenter(), obb.HalfExtents(), obb.Loc().oriMatrix()); }
    @Nonnull
    default Direction SelectFaceOBBMin(@Nonnull Vec3 center, @Nonnull Vector3f halfExtents, @Nonnull Matrix3f ori) { return SelectFaceOBBMax(center, halfExtents, ori).getOpposite(); }
    @Nonnull
    default Direction SelectFaceOBBMin(@Nonnull TransformedBB obb) { return SelectFaceOBBMax(obb).getOpposite(); }

    @Nonnull
    default Direction SelectFaceAABBMin(@Nonnull AABB aabb) { return Direction.getNearest(GetNormal().x, GetNormal().y, GetNormal().z).getOpposite(); }
    @Nonnull
    default Direction SelectFaceAABBMax(@Nonnull AABB aabb) { return Direction.getNearest(GetNormal().x, GetNormal().y, GetNormal().z); }

    // This does not return the true Box-Plane intersection, rather a useful incident face to consider for collision
    @Nonnull
    default IPolygon CollisionClip(@Nonnull IPolygon incident, @Nonnull IPolygon reference)
    {
        //https://research.ncl.ac.uk/game/mastersdegree/gametechnologies/previousinformation/physics5collisionmanifolds/2017%20Tutorial%205%20-%20Collision%20Manifolds.pdf

        for(int i = 0; i < incident.GetNumVertices(); i++)
        {
            Transform vCurrent = Transform.fromPos(incident.GetVertex(i));
            Vec3 vEdge = incident.GetEdgeVector(i);
            DebugRenderer.RenderLine(vCurrent, 2, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), vEdge);
            DebugRenderer.RenderPoint(vCurrent, 2, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        }
        for(int i = 0; i < reference.GetNumVertices(); i++)
        {
            Transform vCurrent = Transform.fromPos(reference.GetVertex(i));
            Vec3 vEdge = reference.GetEdgeVector(i);
            DebugRenderer.RenderLine(vCurrent, 2, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), vEdge);
            DebugRenderer.RenderPoint(vCurrent, 2, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        }

        // Adjacent Face Clipping
        int refVertexCount = reference.GetNumVertices();
        for(int i = 0; i < refVertexCount; i++)
        {
            IPlane clipPlane = reference.GetEdgeClipPlane(GetNormal(), i);
            incident = incident.Clip(clipPlane);
        }

        // Final Clipping - simply remove points that are inside our clip plane
        incident = incident.CullClip(reference.GetFaceClipPlane());

        return incident;
    }

    default boolean AreOBBsFullyBelow(@Nonnull TransformedBBCollection set)
    {
        Matrix3f mat = set.Location().oriMatrix();
        for(int i = 0; i < set.getCount(); i++)
            if(!IsOBBFullyBelow(set.getCenter(i), set.getHalfExtents(i), mat))
                return false;
        return true;
    }
    default boolean AreOBBsFullyAbove(@Nonnull TransformedBBCollection set)
    {
        Matrix3f mat = set.Location().oriMatrix();
        for(int i = 0; i < set.getCount(); i++)
            if(!IsOBBFullyAbove(set.getCenter(i), set.getHalfExtents(i), mat))
                return false;
        return true;
    }
}
