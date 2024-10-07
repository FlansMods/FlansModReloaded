package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class OBB extends AABB
{
	public final Transform Loc;

	public OBB(@Nonnull Transform transform, @Nonnull AABB aabb)
	{
		super(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		Loc = transform;
	}
	public OBB(@Nonnull Transform transform, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
	{
		super(xMin, yMin, zMin, xMax, yMax, zMax);
		Loc = transform;
	}
	public OBB(@Nonnull Transform transform, @Nonnull BlockPos pos)
	{
		this(transform, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}
	public OBB(@Nonnull Transform transform, @Nonnull BlockPos from, @Nonnull BlockPos to) {
		this(transform, from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
	}

	@Override @Nonnull
	public OBB setMinX(double d) { return new OBB(Loc, d, minY, minZ, maxX, maxY, maxZ); }
	@Override @Nonnull
	public OBB setMinY(double d) { return new OBB(Loc, minX, d, minZ, maxX, maxY, maxZ); }
	@Override @Nonnull
	public OBB setMinZ(double d) { return new OBB(Loc, minX, minY, d, maxX, maxY, maxZ); }
	@Override @Nonnull
	public OBB setMaxX(double d) { return new OBB(Loc, minX, minY, minZ, d, maxY, maxZ); }
	@Override @Nonnull
	public OBB setMaxY(double d) { return new OBB(Loc, minX, minY, minZ, maxX, d, maxZ); }
	@Override @Nonnull
	public OBB setMaxZ(double d) { return new OBB(Loc, minX, minY, minZ, maxX, maxY, d); }
	//@Override
	//public double min(@Nonnull Direction.Axis axis) { return axis.choose(minX, minY, minZ); }
	//@Override
	//public double max(@Nonnull Direction.Axis axis) { return axis.choose(maxX, maxY, maxZ); }

	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
			return true;
		else if(other instanceof OBB otherOBB)
		{
			return Loc.equals(otherOBB.Loc) && super.equals(other);
		}
		else if(Loc.IsIdentity() && other instanceof AABB)
		{
			return super.equals(other);
		}
		return false;
	}
	@Override
	public int hashCode()
	{
		int i = Loc.hashCode();
		int j = super.hashCode();
		return 31 * j + (int)(i ^ i >>> 16);
	}

	@Override
	@Nonnull
	public OBB contract(double contractX, double contractY, double contractZ) {
		double d0 = this.minX;
		double d1 = this.minY;
		double d2 = this.minZ;
		double d3 = this.maxX;
		double d4 = this.maxY;
		double d5 = this.maxZ;
		if (contractX < 0.0D) {
			d0 -= contractX;
		} else if (contractX > 0.0D) {
			d3 -= contractX;
		}

		if (contractY < 0.0D) {
			d1 -= contractY;
		} else if (contractY > 0.0D) {
			d4 -= contractY;
		}

		if (contractZ < 0.0D) {
			d2 -= contractZ;
		} else if (contractZ > 0.0D) {
			d5 -= contractZ;
		}

		return new OBB(Loc, d0, d1, d2, d3, d4, d5);
	}
	@Override
	@Nonnull
	public OBB expandTowards(@Nonnull Vec3 v) {
		return this.expandTowards(v.x, v.y, v.z);
	}
	@Override
	@Nonnull
	public OBB expandTowards(double expandX, double expandY, double expandZ) {
		double d0 = this.minX;
		double d1 = this.minY;
		double d2 = this.minZ;
		double d3 = this.maxX;
		double d4 = this.maxY;
		double d5 = this.maxZ;
		if (expandX < 0.0D) {
			d0 += expandX;
		} else if (expandX > 0.0D) {
			d3 += expandX;
		}

		if (expandY < 0.0D) {
			d1 += expandY;
		} else if (expandY > 0.0D) {
			d4 += expandY;
		}

		if (expandZ < 0.0D) {
			d2 += expandZ;
		} else if (expandZ > 0.0D) {
			d5 += expandZ;
		}

		return new OBB(Loc, d0, d1, d2, d3, d4, d5);
	}
	@Override @Nonnull
	public OBB inflate(double scale) { return this.inflate(scale, scale, scale); }
	@Override @Nonnull
	public OBB inflate(double x, double y, double z) {
		double d0 = this.minX - x;
		double d1 = this.minY - y;
		double d2 = this.minZ - z;
		double d3 = this.maxX + x;
		double d4 = this.maxY + y;
		double d5 = this.maxZ + z;
		return new OBB(Loc, d0, d1, d2, d3, d4, d5);
	}
	@Override @Nonnull
	public OBB intersect(@Nonnull AABB other) {
		throw new NotImplementedException();
	}
	@Override @Nonnull
	public OBB minmax(@Nonnull AABB other) {
		throw new NotImplementedException();
	}
	@Override @Nonnull
	public OBB move(double x, double y, double z) { return move(new Vec3(x, y, z)); }
	@Override @Nonnull
	public OBB move(BlockPos pos) { return move(pos.getX(), pos.getY(), pos.getZ()); }
	@Override @Nonnull
	public OBB move(@Nonnull Vec3 v) {
		return new OBB(Loc.Translated(v), minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean intersects(@Nonnull OBB other)
	{
		return CollisionUtility.Separate(
			new Vector3f((float)getXsize(), (float)getYsize(), (float)getZsize()),
			new Vector3f((float)other.getXsize(), (float)other.getYsize(), (float)other.getZsize()),
			Transform.Compose(Loc, Transform.FromPos(getCenter())),
			Transform.Compose(other.Loc, Transform.FromPos(other.getCenter()))) != null;
	}
	@Override
	public boolean intersects(@Nonnull AABB other)
	{
		return CollisionUtility.Separate(
			new Vector3f((float)getXsize(), (float)getYsize(), (float)getZsize()),
			new Vector3f((float)other.getXsize(), (float)other.getYsize(), (float)other.getZsize()),
			Transform.Compose(Loc, Transform.FromPos(getCenter())),
			Transform.FromPos(other.getCenter())) != null;
	}
	@Override
	public boolean intersects(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
	{
		return CollisionUtility.Separate(
			new Vector3f((float)getXsize(), (float)getYsize(), (float)getZsize()),
			new Vector3f((float)(xMax - xMin), (float)(yMax - yMin), (float)(zMax - zMin)),
			Transform.Compose(Loc, Transform.FromPos(getCenter())),
			Transform.FromPos((xMin + xMax)/2, (yMin+yMax)/2, (zMin+zMax)/2)) != null;
	}
	//public boolean intersects(Vec3 p_82336_, Vec3 p_82337_) {
	//	return this.intersects(Math.min(p_82336_.x, p_82337_.x), Math.min(p_82336_.y, p_82337_.y), Math.min(p_82336_.z, p_82337_.z), Math.max(p_82336_.x, p_82337_.x), Math.max(p_82336_.y, p_82337_.y), Math.max(p_82336_.z, p_82337_.z));
	//}



	@Override
	public boolean contains(@Nonnull Vec3 v)
	{
		return containsLocal(Loc.GlobalToLocalPosition(v));
	}
	@Override
	public boolean contains(double x, double y, double z)
	{
		return containsLocal(Loc.GlobalToLocalPosition(new Vec3(x, y, z)));
	}
	public boolean containsLocal(@Nonnull Vec3 v)
	{
		return super.contains(v);
	}
	public boolean containsLocal(double x, double y, double z)
	{
		return super.contains(x, y, z);
	}

	@Override @Nonnull
	public OBB deflate(double x, double y, double z) { return inflate(-x, -y, -z); }
	@Override @Nonnull
	public OBB deflate(double scale) { return this.inflate(-scale); }

	@Override @Nonnull
	public Optional<Vec3> clip(@Nonnull Vec3 from, @Nonnull Vec3 to)
	{
		Vec3 localFrom = Loc.GlobalToLocalPosition(from);
		Vec3 localTo = Loc.GlobalToLocalPosition(to);
		Optional<Vec3> clipped = clipLocal(localFrom, localTo);
		return clipped.map(Loc::LocalToGlobalPosition);
	}
	@Nonnull
	public Optional<Vec3> clipLocal(@Nonnull Vec3 from, @Nonnull Vec3 to)
	{
		return super.clip(from, to);
	}
	@Override
	public double distanceToSqr(@Nonnull Vec3 v)
	{
		return super.distanceToSqr(Loc.GlobalToLocalPosition(v));
	}
	@Override @Nonnull
	public String toString()
	{
		return "OBB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "] @ " + Loc;
	}
	@Override
	public boolean hasNaN() {
		return super.hasNaN() || Loc.HasNaN();
	}
	@Override @Nonnull
	public Vec3 getCenter() {
		return Loc.LocalToGlobalPosition(super.getCenter());
	}
}
