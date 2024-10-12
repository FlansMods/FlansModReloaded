package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

public interface IPolygon
{
    @Nonnull List<Vec3> GetVertices();
    @Nonnull IPolygon Clip(@Nonnull IPlane clipPlane);
    @Nonnull IPolygon CullClip(@Nonnull IPlane clipPlane);

    default int GetNumVertices() { return GetVertices().size(); }
    @Nonnull
    default Vec3 GetVertex(int index)
    {
        if(0 <= index && index < GetVertices().size())
            return GetVertices().get(index);
        return Vec3.ZERO;
    }
    @Nonnull
    default Vec3 GetVertexLooped(int index)
    {
        if(GetVertices().isEmpty())
            return Vec3.ZERO;

        index = Maths.Modulo(index, GetVertices().size());
        return GetVertices().get(index);
    }
    @Nonnull
    default Vec3 GetEdgeVector(int edgeIndex)
    {
        Vec3 v1 = GetVertexLooped(edgeIndex);
        Vec3 v2 = GetVertexLooped(edgeIndex + 1);
        return v2.subtract(v1);
    }
    @Nonnull
    default Vec3 GetAveragePos()
    {
        int numVerts = GetVertices().size();
        if(numVerts == 0)
            return Vec3.ZERO;

        Vec3 pos = Vec3.ZERO;

        for(int i = 0; i < numVerts; i++)
            pos = pos.add(GetVertices().get(i));
        return pos.scale(1d / numVerts);
    }

    @Nonnull
    default IPolygon Clip(@Nonnull List<IPlane> clipPlanes)
    {
        IPolygon clipResult = this;
        for(IPlane clipPlane : clipPlanes)
            clipResult = clipResult.Clip(clipPlane);
        return clipResult;
    }
    @Nonnull
    default IPlane GetEdgeClipPlane(@Nonnull Vec3 refNormal, int edgeIndex)
    {
        Vec3 edge = GetEdgeVector(edgeIndex);
        Vec3 clipNormal = refNormal.cross(edge.normalize());
        double dist = GetVertexLooped(edgeIndex).dot(clipNormal);
        return Plane.of(clipNormal, dist);
    }
    @Nonnull
    default IPlane GetFaceClipPlane()
    {
        if(GetNumVertices() < 3)
        {
            FlansPhysicsMod.LOGGER.warn("Tried to get clip plane of a polygon with fewer than 3 verts");
            return Plane.of(Vec3.ZERO, 0d);
        }
        Vec3 v1 = GetVertexLooped(0);
        Vec3 v2 = GetVertexLooped(1);
        Vec3 v3 = GetVertexLooped(2);
        Vec3 normal = v3.subtract(v1).cross(v2.subtract(v1)).normalize();
        double dist = v1.dot(normal);
        return Plane.of(normal, dist);
    }

}
