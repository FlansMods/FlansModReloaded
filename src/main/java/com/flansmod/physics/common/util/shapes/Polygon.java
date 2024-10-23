package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.util.Maths;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public record Polygon(@Nonnull ImmutableList<Vec3> vertices) implements IPolygon
{
    private static final Polygon EMPTY_POLYGON = new Polygon(ImmutableList.of());
    @Nonnull
    public static Polygon empty() { return EMPTY_POLYGON; }
    @Nonnull
    public static Polygon point(@Nonnull final Vec3 point) { return new Polygon(ImmutableList.of(point)); }
    @Nonnull
    public static Polygon line(@Nonnull final Vec3 a, @Nonnull final Vec3 b) { return new Polygon(ImmutableList.of(a, b)); }
    @Nonnull
    public static Polygon triangle(@Nonnull final Vec3 a, @Nonnull final Vec3 b, @Nonnull final Vec3 c) { return new Polygon(ImmutableList.of(a, b, c)); }
    @Nonnull
    public static Polygon square(@Nonnull final Vec3 a, @Nonnull final Vec3 b, @Nonnull final Vec3 c, @Nonnull final Vec3 d) { return new Polygon(ImmutableList.of(a, b, c, d)); }
    @Nonnull
    public static Polygon ofLoopedVerts(@Nonnull ImmutableList<Vec3> vertices)
    {
        if(!vertices.isEmpty()) {
            if (vertices.get(0).equals(vertices.get(vertices.size() - 1)))
                return new Polygon(ImmutableList.copyOf(vertices.subList(0, vertices.size() - 1)));
        }
        return new Polygon(vertices);
    }
    @Nonnull
    public static Polygon ofNonLoopedVerts(@Nonnull ImmutableList<Vec3> vertices) { return new Polygon(vertices); }

    @Nonnull
    public static IPolygon of(@Nonnull AABB aabb, @Nonnull Direction dir) { return new Polygon(VertexIndex.getAABBFace(aabb, dir)); }
    @Nonnull
    public static IPolygon of(@Nonnull TransformedBB obb, @Nonnull Direction dir) { return obb.GetFace(dir); }

    @Override @Nonnull
    public List<Vec3> GetVertices() { return vertices; }

    @Override @Nonnull
    public IPolygon Clip(@Nonnull IPlane clipPlane)
    {
        // https://alamot.github.io/sutherland_hodgman_algorithm/
        ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
        for(int i = 0; i < vertices.size(); i++)
        {
            Vec3 vCurr = vertices.get(i);
            Vec3 vNext = GetVertexLooped(i + 1);

            double dCurr = clipPlane.getPointHeightAbove(vCurr);
            double dNext = clipPlane.getPointHeightAbove(vNext);

            if(dCurr <= 0d && dNext <= 0d)
            {
                builder.add(vNext);
            }
            else if(dCurr > 0d && Maths.approx(dNext, 0d))
            {
                builder.add(vNext);
            }
            else if(Maths.approx(dCurr, 0d) && dNext > 0d)
            {
                continue;
            }
            else if(dCurr <= 0d && dNext > 0d)
            {
                Optional<Vec3> clipPos = clipPlane.rayPlaneIntersect(vCurr, vNext.subtract(vCurr));
                if(clipPos.isPresent())
                    builder.add(clipPos.get());
                else {
                    FlansPhysicsMod.LOGGER.error("Clipped edge positive somehow didn't intersect");
                    builder.add(vNext);
                }
            }
            else if(dCurr > 0d && dNext <= 0d)
            {
                Optional<Vec3> clipPos = clipPlane.rayPlaneIntersect(vCurr, vNext.subtract(vCurr));
                if(clipPos.isPresent())
                    builder.add(clipPos.get());
                else {
                    FlansPhysicsMod.LOGGER.error("Clipped edge negative somehow didn't intersect");
                    builder.add(vNext);
                }
                builder.add(vNext);
            }
        }
        return ofLoopedVerts(builder.build());
    }

    @Override @Nonnull
    public IPolygon CullClip(@Nonnull IPlane clipPlane)
    {
        ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
        for(int i = 0; i < vertices.size(); i++)
        {
            Vec3 vCurr = vertices.get(i);
            double dCurr = clipPlane.getPointHeightAbove(vCurr);
            if(dCurr >= 0d)
                builder.add(vCurr);
        }
        return Polygon.ofNonLoopedVerts(builder.build());
    }

}
