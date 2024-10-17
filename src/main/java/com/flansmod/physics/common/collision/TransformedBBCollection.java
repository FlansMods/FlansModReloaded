package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public record TransformedBBCollection(@Nonnull Transform Location, @Nonnull ImmutableList<AABB> Colliders)
{
	public int getCount() { return Colliders.size(); }
	@Nonnull public Vec3 getCenter(int index) { return Location.positionVec3().add(Colliders.get(index).getCenter()); }
	@Nonnull public Vector3f getHalfExtents(int index)
	{
		AABB aabb = Colliders.get(index);
		return new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f);
	}

	@Nonnull
	public TransformedBB getColliderBB(int index)
	{
		return new TransformedBB(Transform.fromPosAndQuat(getCenter(index), Location.Orientation, ()->""), getHalfExtents(index));
	}
	@Nonnull
	public TransformedBB getBoundingBB()
	{
		return TransformedBB.Of(Location, getLocalBounds());
	}
	@Nonnull
	public AABB getLocalBounds()
	{
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for(AABB aabb : Colliders)
		{
			xMax = Maths.max(aabb.maxX, xMax);
			yMax = Maths.max(aabb.maxY, yMax);
			zMax = Maths.max(aabb.maxZ, zMax);
			xMin = Maths.min(aabb.minX, xMin);
			yMin = Maths.min(aabb.minY, yMin);
			zMin = Maths.min(aabb.minZ, zMin);
		}
		return new AABB(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
