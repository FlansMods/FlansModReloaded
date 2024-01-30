package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.util.Maths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class Gunshot
{
	private static final HitResult[] NO_HITS = new HitResult[0];

	public BulletDefinition bulletDef = BulletDefinition.INVALID;
	public Vec3 origin = Vec3.ZERO;
	public Vec3 trajectory = Vec3.ZERO;
	public HitResult[] hits = NO_HITS;

	public int fromShotIndex = 0;
	// The index of bullet that was consumed from the mag
	public int fromBulletIndex = 0;
	@Nonnull
	public Vec3 Endpoint() { return new Vec3(origin.x + trajectory.x, origin.y + trajectory.y, origin.z + trajectory.z); }
	@Nonnull
	public Gunshot FromShot(int index)
	{
		fromShotIndex = index;
		return this;
	}
	@Nonnull
	public Gunshot FromBulletIndex(int index)
	{
		fromBulletIndex = index;
		return this;
	}
	@Nonnull
	public Gunshot WithOrigin(double x, double y, double z)
	{
		origin = new Vec3(x, y, z);
		return this;
	}
	@Nonnull
	public Gunshot WithOrigin(Vec3 o)
	{
		origin = o;
		return this;
	}
	@Nonnull
	public Gunshot WithTrajectory(double x, double y, double z)
	{
		trajectory = new Vec3(x, y, z);
		return this;
	}
	@Nonnull
	public Gunshot WithTrajectory(Vec3 t)
	{
		trajectory = t;
		return this;
	}
	@Nonnull
	public Gunshot WithHits(HitResult[] results)
	{
		hits = results;
		return this;
	}
	@Nonnull
	public Gunshot WithBullet(BulletDefinition bullet)
	{
		bulletDef = bullet;
		return this;
	}

	public static void Encode(@Nonnull Gunshot gunshot, @Nonnull FriendlyByteBuf buf)
	{
		buf.writeInt(gunshot.bulletDef.hashCode());
		buf.writeInt(gunshot.fromShotIndex);
		buf.writeInt(gunshot.fromBulletIndex);

		buf.writeDouble(gunshot.origin.x);
		buf.writeDouble(gunshot.origin.y);
		buf.writeDouble(gunshot.origin.z);

		buf.writeFloat((float)gunshot.trajectory.x);
		buf.writeFloat((float)gunshot.trajectory.y);
		buf.writeFloat((float)gunshot.trajectory.z);

		Vec3 end = gunshot.Endpoint();
		buf.writeInt(gunshot.hits.length);
		for(int j = 0; j < gunshot.hits.length; j++)
		{
			float t = (float)Maths.CalculateParameter(gunshot.origin, end, gunshot.hits[j].getLocation());
			buf.writeFloat(t);

			// Always write two ints.
			switch(gunshot.hits[j].getType())
			{
				case BLOCK ->
				{
					buf.writeInt(-1); // block
					BlockHitResult blockHit = (BlockHitResult) gunshot.hits[j];
					// The parametric hit time will give us a rough position, so we just need to pin it down within the very local area
					int localBlockPos = Maths.BlockPosToPinpointData(blockHit.getBlockPos(), blockHit.getDirection().ordinal());
					buf.writeInt(localBlockPos);
				}
				case ENTITY ->
				{
					EntityHitResult entityHit = (EntityHitResult)gunshot.hits[j];
					buf.writeInt(entityHit.getEntity().getId()); // any positive id = entity id
					if(entityHit instanceof PlayerHitResult playerHit)
					{
						buf.writeInt(playerHit.GetHitbox().area.ordinal()); // hitbox type
					}
					else
					{
						buf.writeInt(0); // no extra data
					}
				}
				default ->
				{
					buf.writeInt(-2); // invalid
					buf.writeInt(0); // no extra data
				}
			}
		}
	}

	@Nonnull
	public static Gunshot Decode(@Nonnull FriendlyByteBuf buf)
	{
		int bulletHash = buf.readInt();
		int fromShotIndex = buf.readInt();
		int fromBulletIndex = buf.readInt();
		BulletDefinition bulletDef = FlansMod.BULLETS.ByHash(bulletHash);

		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();

		double dx = buf.readFloat();
		double dy = buf.readFloat();
		double dz = buf.readFloat();

		int numHits = buf.readInt();
		HitResult[] hits = new HitResult[numHits];
		for(int i = 0; i < numHits; i++)
		{
			float t = buf.readFloat();
			Vec3 hitPos = new Vec3(x + dx * t, y + dy * t, z + dz * t);
			int entityIdOrType = buf.readInt();
			int extraData = buf.readInt();
			switch(entityIdOrType)
			{
				case -2 -> // invalid
				{
					hits[i] = BlockHitResult.miss(hitPos, Direction.UP, BlockPos.containing(hitPos));
				}
				case -1 -> // block
				{
					BlockPos roughBlockPos = BlockPos.containing(hitPos);
					BlockPos exactBlockPos = Maths.ResolveBlockPos(roughBlockPos, extraData);

					hits[i] = new BlockHitResult(
						hitPos,
						Direction.values()[(extraData >> 24) & 0xff],
						exactBlockPos,
						false
					);
				}
				default -> // any positive int = entityId
				{
					hits[i] = new UnresolvedEntityHitResult(hitPos,
						entityIdOrType,
						EPlayerHitArea.values()[extraData]
					);
				}
			}
		}

		return new Gunshot()
			.WithOrigin(x, y, z)
			.FromShot(fromShotIndex)
			.FromBulletIndex(fromBulletIndex)
			.WithTrajectory(dx, dy, dz)
			.WithHits(hits)
			.WithBullet(bulletDef);
	}
}
