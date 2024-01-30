package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GunshotCollection
{
	public static final ResourceKey<Level> InvalidDimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("null"));

	// Raw data is reference-free for networking
	public ResourceKey<Level> OwnerDimension;
	public int OwnerEntityID;
	public ResourceKey<Level> ShooterDimension;
	public int ShooterEntityID;
	public int SeatID; // If relevant to this entity type, it will specify the seat number
	public int GunHash;
	public int GroupPathHash;
	public int FiredTick;
	public List<Gunshot> Shots = new ArrayList<>(2);

	// Accessors
	public GunDefinition Gun()
	{
		return FlansMod.GUNS.ByHash(GunHash);
	}
	public Gunshot Get(int index) { return Shots.get(index); }
	public int Count() { return Shots.size(); }

	public GunshotCollection()
	{
		Shots = new ArrayList<>(8);
		ShooterDimension = InvalidDimensionKey;
		OwnerDimension = InvalidDimensionKey;
	}

	public GunshotCollection CopySubset(int triggerMin, int triggerMax)
	{
		GunshotCollection copy = new GunshotCollection()
			.WithGun(GunHash)
			.WithOwner(OwnerDimension, OwnerEntityID)
			.WithShooter(ShooterDimension, ShooterEntityID)
			.FiredOnTick(FiredTick)
			.FromActionGroup(GroupPathHash);

		for(Gunshot shot : Shots)
		{
			if(triggerMin <= shot.fromShotIndex && shot.fromShotIndex <= triggerMax)
				copy.AddShot(shot);
		}

		return copy;
	}

	public GunshotCollection WithGun(int gunHash)
	{
		this.GunHash = gunHash;
		return this;
	}

	public GunshotCollection WithGun(GunDefinition gun)
	{
		GunHash = gun.hashCode();
		return this;
	}

	public GunshotCollection WithOwner(ResourceKey<Level> ownerDimension, int ownerEntityID)
	{
		OwnerDimension = ownerDimension;
		OwnerEntityID = ownerEntityID;
		return this;
	}

	public GunshotCollection WithOwner(Entity owner)
	{
		OwnerDimension = owner.level().dimension();
		OwnerEntityID = owner.getId();
		return this;
	}

	public GunshotCollection WithShooter(ResourceKey<Level> shooterDimension, int shooterEntityID)
	{
		ShooterDimension = shooterDimension;
		ShooterEntityID = shooterEntityID;
		return this;
	}

	public GunshotCollection WithShooter(Entity owner)
	{
		ShooterDimension = owner.level().dimension();
		ShooterEntityID = owner.getId();
		return this;
	}

	public GunshotCollection FromActionGroup(int groupPathHash)
	{
		GroupPathHash = groupPathHash;
		return this;
	}

	public GunshotCollection FromActionGroup(String groupPath)
	{
		GroupPathHash = groupPath.hashCode();
		return this;
	}

	public GunshotCollection FiredOnTick(int tick)
	{
		FiredTick = tick;
		return this;
	}

	public GunshotCollection AddShot(Gunshot shot)
	{
		Shots.add(shot);
		return this;
	}

	public Level Dimension()
	{
		return MinecraftHelpers.GetLevel(ShooterDimension);
	}

	public boolean HasOwner() { return OwnerEntityID > 0; }
	@Nullable
	public Entity Owner()
	{
		Level level = Dimension();
		if(level != null)
			return level.getEntity(OwnerEntityID);
		return null;
	}

	public boolean HasShooter() { return ShooterEntityID > 0; }
	@Nullable
	public Entity Shooter()
	{
		Level level = Dimension();
		if(level != null)
			return level.getEntity(ShooterEntityID);
		return null;
	}

	public static void Encode(@Nullable GunshotCollection shotCollection, @Nonnull FriendlyByteBuf buf)
	{
		if(shotCollection == null
		|| shotCollection.OwnerDimension.location().getPath().equals("null")
		|| shotCollection.ShooterDimension.location().getPath().equals("null"))
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

			buf.writeInt(shotCollection.OwnerEntityID);
			buf.writeInt(shotCollection.ShooterEntityID);
			buf.writeInt(shotCollection.SeatID);
			buf.writeInt(shotCollection.GunHash);
			buf.writeInt(shotCollection.GroupPathHash);
			buf.writeInt(shotCollection.FiredTick);
			buf.writeResourceKey(shotCollection.ShooterDimension);
		}
	}

	public static void Decode(@Nonnull GunshotCollection shotCollection, @Nonnull FriendlyByteBuf buf)
	{
		int numShots = buf.readInt();
		if(numShots >= 0)
		{
			for(int i = 0; i < numShots; i++)
			{
				shotCollection.Shots.add(Gunshot.Decode(buf));
			}

			shotCollection.OwnerEntityID = buf.readInt();
			shotCollection.ShooterEntityID = buf.readInt();
			shotCollection.SeatID = buf.readInt();
			shotCollection.GunHash = buf.readInt();
			shotCollection.GroupPathHash = buf.readInt();
			shotCollection.FiredTick = buf.readInt();
			shotCollection.ShooterDimension = buf.readResourceKey(Registries.DIMENSION);
		}
	}
}
