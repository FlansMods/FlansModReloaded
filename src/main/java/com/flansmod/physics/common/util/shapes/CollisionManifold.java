package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.util.Maths;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;


public record CollisionManifold(@Nonnull Vec3 Normal, double Distance, @Nonnull ImmutableList<Vec3> BoundingVertices)
		implements IPlane, IPolygon
{

	private static final CollisionManifold INVALID = new CollisionManifold(Vec3.ZERO, Double.MAX_VALUE, ImmutableList.of());
	@Nonnull
	public static CollisionManifold invalid() { return INVALID; }
	//@Nonnull
	//public static CollisionManifold ofBox(@Nonnull TransformedBB obb, @Nonnull Direction side)
	//{
	//	return obb.GetSide(side);
	//}
	@Nonnull
	public static CollisionManifold ofLoopedVerts(@Nonnull Vec3 normal, double dist, @Nonnull ImmutableList<Vec3> verts)
	{
		if(verts.get(0).equals(verts.get(verts.size() - 1)))
			return new CollisionManifold(normal, dist, ImmutableList.copyOf(verts.subList(0, verts.size() - 1)));
		return new CollisionManifold(normal, dist, verts);
	}
	@Nonnull
	public static CollisionManifold ofNonLoopedVerts(@Nonnull Vec3 normal, double dist, @Nonnull ImmutableList<Vec3> verts)
	{
		return new CollisionManifold(normal, dist, verts);
	}


	// =================================================================
	// IPlane
	@Override @Nonnull
	public Vec3 getNormal() { return Normal; }
	@Override
	public double GetDistance() { return Distance; }
	@Override
	public double project(@Nonnull Vec3 point) { return Normal.dot(point); }
	// The chosen definition of a plane is shown most clearly here
	@Override
	public double GetPointHeightAbove(@Nonnull Vec3 point) { return (project(point) - Distance); }
	@Override @Nonnull
	public Optional<Vec3> RayPlaneIntersect(@Nonnull Vec3 origin, @Nonnull Vec3 ray)
	{
		// (Origin + t * Ray).V == d
		// Solve for t
		// (R.V)t = d - O.V
		double rDotV = ray.dot(Normal);
		if(Maths.approx(rDotV, 0d))
			return Optional.empty();

		double oDotV = origin.dot(Normal);
		double t = (Distance - oDotV) / rDotV;
		return Optional.of(origin.add(ray.scale(t)));
	}

	// =================================================================
	// IPolygon
	@Override @Nonnull
	public IPlane GetFaceClipPlane() { return this; }
	@Override @Nonnull
	public List<Vec3> GetVertices() { return BoundingVertices; }
	@Override @Nonnull
	public IPolygon Clip(@Nonnull IPlane clipPlane)
	{
		// https://alamot.github.io/sutherland_hodgman_algorithm/
		ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
		for(int i = 0; i < BoundingVertices.size(); i++)
		{
			Vec3 vCurr = BoundingVertices.get(i);
			Vec3 vNext = GetVertexLooped(i + 1);

			double dCurr = clipPlane.project(vCurr);
			double dNext = clipPlane.project(vNext);

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
				Optional<Vec3> clipPos = clipPlane.RayPlaneIntersect(vCurr, vNext.subtract(vCurr));
				if(clipPos.isPresent())
					builder.add(clipPos.get());
				else {
					FlansPhysicsMod.LOGGER.error("Clipped edge positive somehow didn't intersect");
					builder.add(vNext);
				}
			}
			else if(dCurr > 0d && dNext <= 0d)
			{
				Optional<Vec3> clipPos = clipPlane.RayPlaneIntersect(vCurr, vNext.subtract(vCurr));
				if(clipPos.isPresent())
					builder.add(clipPos.get());
				else {
					FlansPhysicsMod.LOGGER.error("Clipped edge negative somehow didn't intersect");
					builder.add(vNext);
				}
				builder.add(vNext);
			}
		}
		return ofLoopedVerts(Normal, Distance, builder.build());
	}

	@Override @Nonnull
	public IPolygon CullClip(@Nonnull IPlane clipPlane)
	{
		ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
		for(int i = 0; i < BoundingVertices.size(); i++)
		{
			Vec3 vCurr = BoundingVertices.get(i);
			double dCurr = clipPlane.project(vCurr);
			if(dCurr >= 0d)
				builder.add(vCurr);
		}
		return ofNonLoopedVerts(Normal, Distance, builder.build());
	}
}

