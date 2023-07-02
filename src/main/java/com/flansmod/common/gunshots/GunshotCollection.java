package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GunshotCollection
{
	// Raw data is reference-free for networking
	public int ownerEntityID;
	public int shooterEntityID;
	public int seatID; // If relevant to this entity type, it will specify the seat number
	public int bulletHash;
	public int gunHash;
	public EActionInput actionUsed;
	public ResourceKey<Level> dimension;
	public List<Gunshot> shots = new ArrayList<>(2);

	// Accessors
	public GunDefinition Gun()
	{
		return FlansMod.GUNS.ByHash(gunHash);
	}
	public BulletDefinition Bullet()
	{
		return FlansMod.BULLETS.ByHash(bulletHash);
	}
	public Gunshot Get(int index) { return shots.get(index); }

	//public Entity shooterEntity() { return }
	public int Count() { return shots.size(); }


	public GunshotCollection()
	{
		shots = new ArrayList<>(8);
	}

	public GunshotCollection WithGun(GunDefinition gun)
	{
		gunHash = gun.hashCode();
		return this;
	}

	public GunshotCollection WithBullet(BulletDefinition bullet)
	{
		bulletHash = bullet.hashCode();
		return this;
	}

	public GunshotCollection WithOwner(Entity owner)
	{
		dimension = owner.getLevel().dimension();
		ownerEntityID = owner.getId();
		return this;
	}

	public GunshotCollection WithShooter(Entity owner)
	{
		dimension = owner.getLevel().dimension();
		shooterEntityID = owner.getId();
		return this;
	}

	public GunshotCollection FromAction(EActionInput actionSet)
	{
		actionUsed = actionSet;
		return this;
	}

	public GunshotCollection AddShot(Vec3 origin, Vec3 trajectory)
	{
		shots.add(new Gunshot().WithOrigin(origin).WithTrajectory(trajectory));
		return this;
	}

	public GunshotCollection AddShot(Gunshot shot)
	{
		shots.add(shot);
		return this;
	}

	public Level Dimension()
	{
		return MinecraftHelpers.GetLevel(dimension);
	}

	public boolean HasOwner() { return ownerEntityID > 0; }
	@Nullable
	public Entity Owner()
	{
		Level level = Dimension();
		if(level != null)
			return level.getEntity(ownerEntityID);
		return null;
	}

	public boolean HasShooter() { return shooterEntityID > 0; }
	@Nullable
	public Entity Shooter()
	{
		Level level = Dimension();
		if(level != null)
			return level.getEntity(shooterEntityID);
		return null;
	}

	public static void Encode(GunshotCollection shotCollection, FriendlyByteBuf buf)
	{
		if(shotCollection == null)
		{
			buf.writeInt(-1);
		}
		else
		{
			buf.writeInt(shotCollection.Count());
			for(int i = 0; i < shotCollection.Count(); i++)
			{
				Gunshot gunshot = shotCollection.Get(i);
				Gunshot.Encode(gunshot, buf);
			}

			buf.writeInt(shotCollection.ownerEntityID);
			buf.writeInt(shotCollection.shooterEntityID);
			buf.writeInt(shotCollection.seatID);
			buf.writeInt(shotCollection.bulletHash);
			buf.writeInt(shotCollection.gunHash);
			buf.writeBoolean(shotCollection.actionUsed == EActionInput.PRIMARY);
			buf.writeResourceKey(shotCollection.dimension);
		}
	}

	public static void Decode(GunshotCollection shotCollection, FriendlyByteBuf buf)
	{
		int numShots = buf.readInt();
		if(numShots >= 0)
		{
			for(int i = 0; i < numShots; i++)
			{
				shotCollection.shots.add(Gunshot.Decode(buf));
			}

			shotCollection.ownerEntityID = buf.readInt();
			shotCollection.shooterEntityID = buf.readInt();
			shotCollection.seatID = buf.readInt();
			shotCollection.bulletHash = buf.readInt();
			shotCollection.gunHash = buf.readInt();
			shotCollection.actionUsed = buf.readBoolean() ? EActionInput.PRIMARY : EActionInput.SECONDARY;
			shotCollection.dimension = buf.readResourceKey(Registries.DIMENSION);
		}
	}
}
