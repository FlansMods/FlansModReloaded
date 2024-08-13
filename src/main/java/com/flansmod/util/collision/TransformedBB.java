package com.flansmod.util.collision;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public record TransformedBB(@Nonnull Transform Loc, @Nonnull Vector3f HalfExtents)
{
	@Nonnull
	public static TransformedBB EntityInSpace(@Nonnull AABB entityBounds,
											  @Nonnull Vec3 entityPos,
											  @Nonnull Transform space)
	{
		return new TransformedBB(space.GlobalToLocalTransform(Transform.FromPos(entityPos)),
			new Vector3f((float)entityBounds.getXsize()/2f, (float)entityBounds.getYsize()/2f, (float)entityBounds.getZsize()/2f));
	}
	@Nonnull
	public static TransformedBB Of(@Nonnull Transform location, @Nonnull AABB aabb)
	{
		Transform centerLoc = Transform.Compose(
			location,
			Transform.FromPos(aabb.getCenter()));
		return new TransformedBB(centerLoc,
			new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f));
	}
	@Nonnull
	public static TransformedBB Of(@Nonnull AABB aabb)
	{
		return new TransformedBB(Transform.FromPos(aabb.getCenter()), new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f));
	}

	@Nonnull
	public Vec3 GetCenter() { return Loc.PositionVec3(); }

	@Nullable
	public ContinuousSeparationManifold Intersect(@Nonnull AABB bb, @Nonnull Vec3 motion) {
		return ContinuousCollisionUtility.SeparateContinuous(this, motion, bb);
	}
	@Nullable
	public Vec3 Intersect(@Nonnull AABB bb) { return CollisionUtility.Separate(this, bb); }
	@Nullable
	public Vec3 Intersect(@Nonnull TransformedBB other) { return CollisionUtility.Separate(this, other); }

	public boolean Contains(@Nonnull Vec3 point)
	{
		Vec3 local = Loc.GlobalToLocalPosition(point);
		return Maths.Abs(local.x) <= HalfExtents.x
			&& Maths.Abs(local.y) <= HalfExtents.y
			&& Maths.Abs(local.z) <= HalfExtents.z;
	}

	public double XSize() { return HalfExtents.x * 2d; }
	public double YSize() { return HalfExtents.y * 2d; }
	public double ZSize() { return HalfExtents.z * 2d; }

	public double GetMaxRadiusBound() {
		return Maths.Max(HalfExtents.x, HalfExtents.y, HalfExtents.z) * Maths.Root2;
	}

	@Nonnull
	public TransformedBB Move(@Nonnull Function<Transform, Transform> func) { return new TransformedBB(func.apply(Loc), HalfExtents); }
	@Nonnull
	public SeparationManifold GetSide(@Nonnull Direction dir)
	{
		Vec3 center = Loc.PositionVec3();
		Vec3 dirVec = Loc.DirectionVec3(dir);
		return switch(dir)
		{
			case UP, DOWN -> new SeparationManifold(dirVec, dirVec.dot(center) + HalfExtents.y);
			case NORTH, SOUTH -> new SeparationManifold(dirVec, dirVec.dot(center) + HalfExtents.z);
			case EAST, WEST -> new SeparationManifold(dirVec, dirVec.dot(center) + HalfExtents.x);
		};
	}

}
