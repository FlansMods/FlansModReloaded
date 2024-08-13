package com.flansmod.util.collision;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public record TransformedBBCollection(@Nonnull Transform Location, @Nonnull ImmutableList<AABB> Colliders)
{
	public int GetCount() { return Colliders.size(); }
	@Nonnull public Vec3 GetCenter(int index) { return Location.PositionVec3().add(Colliders.get(index).getCenter()); }
	@Nonnull public Vector3f GetHalfExtents(int index)
	{
		AABB aabb = Colliders.get(index);
		return new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f);
	}

	@Nonnull
	public TransformedBB GetColliderBB(int index)
	{
		return new TransformedBB(Transform.FromPos(GetCenter(index)), GetHalfExtents(index));
	}
	@Nonnull
	public TransformedBB GetBoundingBB()
	{
		return TransformedBB.Of(Location, GetLocalBounds());
	}
	@Nonnull
	public AABB GetLocalBounds()
	{
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for(AABB aabb : Colliders)
		{
			xMax = Maths.Max(aabb.maxX, xMax);
			yMax = Maths.Max(aabb.maxY, yMax);
			zMax = Maths.Max(aabb.maxZ, zMax);
			xMin = Maths.Min(aabb.minX, xMin);
			yMin = Maths.Min(aabb.minY, yMin);
			zMin = Maths.Min(aabb.minZ, zMin);
		}
		return new AABB(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
