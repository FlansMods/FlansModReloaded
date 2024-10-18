package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GunshotCollection
{
	public static final ResourceKey<Level> InvalidDimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("null"));
	public static final int InvalidEntityID = -1;

	// Raw data is reference-free for networking
	public int SeatID; // If relevant to this entity type, it will specify the seat number
	public int GunHash;
	public int GroupPathHash;
	public int FiredTick;
	public boolean Processed = false;
	public List<Gunshot> Shots = new ArrayList<>(2);

	// Accessors
	@Nonnull
	public GunDefinition Gun()
	{
		return FlansMod.GUNS.ByHash(GunHash);
	}
	@Nonnull
	public Gunshot Get(int index) { return Shots.get(index); }
	public int Count() { return Shots.size(); }

	public GunshotCollection()
	{
		Shots = new ArrayList<>(8);
	}
	@Nonnull
	public GunshotCollection CopySubset(int triggerMin, int triggerMax)
	{
		GunshotCollection copy = new GunshotCollection()
			.WithGun(GunHash)
			.FiredOnTick(FiredTick)
			.FromActionGroup(GroupPathHash);

		for(Gunshot shot : Shots)
		{
			if(triggerMin <= shot.fromShotIndex && shot.fromShotIndex <= triggerMax)
				copy.AddShot(shot);
		}

		return copy;
	}
	@Nonnull
	public GunshotCollection WithGun(int gunHash)
	{
		this.GunHash = gunHash;
		return this;
	}
	@Nonnull
	public GunshotCollection WithGun(@Nonnull GunDefinition gun)
	{
		GunHash = gun.hashCode();
		return this;
	}
	@Nonnull
	public GunshotCollection FromActionGroup(int groupPathHash)
	{
		GroupPathHash = groupPathHash;
		return this;
	}
	@Nonnull
	public GunshotCollection FromActionGroup(@Nonnull String groupPath)
	{
		GroupPathHash = groupPath.hashCode();
		return this;
	}
	@Nonnull
	public GunshotCollection FiredOnTick(int tick)
	{
		FiredTick = tick;
		return this;
	}
	@Nonnull
	public GunshotCollection AddShot(@Nonnull Gunshot shot)
	{
		Shots.add(shot);
		return this;
	}

	public static void Encode(@Nullable GunshotCollection shotCollection, @Nonnull FriendlyByteBuf buf)
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

			buf.writeInt(shotCollection.SeatID);
			buf.writeInt(shotCollection.GunHash);
			buf.writeInt(shotCollection.GroupPathHash);
			buf.writeInt(shotCollection.FiredTick);
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

			shotCollection.SeatID = buf.readInt();
			shotCollection.GunHash = buf.readInt();
			shotCollection.GroupPathHash = buf.readInt();
			shotCollection.FiredTick = buf.readInt();
		}
	}
}
