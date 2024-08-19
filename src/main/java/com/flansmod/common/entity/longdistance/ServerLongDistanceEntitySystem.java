package com.flansmod.common.entity.longdistance;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.midi.Track;
import java.util.*;

public class ServerLongDistanceEntitySystem extends LongDistanceEntitySystem
{
	private record TrackingEntry(@Nonnull LongDistanceEntity LongEntity, @Nonnull Optional<Entity> LoadedEntity)
	{
		@Nonnull public static TrackingEntry of(@Nonnull LongDistanceEntity longEntity, @Nonnull Entity entity) { return new TrackingEntry(longEntity, Optional.of(entity)); }
		@Nonnull public static TrackingEntry of(@Nonnull LongDistanceEntity longEntity) { return new TrackingEntry(longEntity, Optional.empty()); }

		public boolean IsTrackedEntityLoaded() { return LoadedEntity.isPresent(); }
	}

	private static class TrackingList
	{
		public Map<UUID, Long> TrackingEntities = new HashMap<>();

		public void BeginTracking(@Nonnull UUID entityID)
		{
			TrackingEntities.put(entityID, 0L);
		}
		public boolean IsTracking(@Nonnull UUID entityID)
		{
			return TrackingEntities.containsKey(entityID);
		}
		public long TickOfLastUpdateFor(@Nonnull UUID entityID)
		{
			return TrackingEntities.getOrDefault(entityID, 0L);
		}
		public void StopTracking(@Nonnull UUID entityID)
		{
			TrackingEntities.remove(entityID);
		}

	}

	private final Map<UUID, TrackingEntry> Trackables = new HashMap<>();
	private final Map<Player, TrackingList> Players = new HashMap<>();

	@Nullable @Override
	public LongDistanceEntity GetFor(@Nonnull UUID entityID)
	{
		if(Trackables.containsKey(entityID))
		{
			return Trackables.get(entityID).LongEntity;
		}
		return null;
	}
	@Nonnull @Override
	public Collection<LongDistanceEntity> GetAllLongEntities(@Nonnull Level level)
	{
		List<LongDistanceEntity> longEntities = new ArrayList<>(Trackables.size());
		for(TrackingEntry entry : Trackables.values())
			if(entry.LongEntity.Dimension == level.dimension())
				longEntities.add(entry.LongEntity);
		return longEntities;
	}

	public boolean IsTrackingLoadedEntity(@Nonnull UUID entityID)
	{
		return Trackables.containsKey(entityID) && Trackables.get(entityID).IsTrackedEntityLoaded();
	}
	public boolean IsTracking(@Nonnull UUID entityID)
	{
		return Trackables.containsKey(entityID);
	}


	public void OnTrackableEntitySpawn(@Nonnull Entity entity)
	{
		if(IsSupportedEntity(entity))
		{
			UUID entityID = entity.getUUID();
			if(Trackables.containsKey(entityID))
			{
				TrackingEntry existing = Trackables.get(entityID);
				if(!existing.IsTrackedEntityLoaded())
				{
					// Need to update this LongEntity to point to the loaded Entity
					OnEntityBecameLoaded(entityID, entity, existing.LongEntity);
					Trackables.put(entityID, TrackingEntry.of(existing.LongEntity, entity));
				}
			}
			else
			{
				// Add a fresh entity
				LongDistanceEntity longEntity = CreateFor(entity);
				if(longEntity != null)
				{
					OnAddedLoadedEntity(entityID, entity, longEntity);
					Trackables.put(entityID, TrackingEntry.of(longEntity, entity));
				}
			}
		}
	}
	public void OnTrackableEntityDespawn(@Nonnull Entity entity)
	{
		if(IsSupportedEntity(entity))
		{
			UUID entityID = entity.getUUID();
			if(Trackables.containsKey(entityID))
			{
				// Remove our reference. We now only have the LongEntity to rely on
				TrackingEntry existing = Trackables.get(entityID);
				OnEntityBecameUnloaded(entityID, entity, existing.LongEntity);
				Trackables.put(entityID, TrackingEntry.of(existing.LongEntity));
			}
		}
	}
	public void OnTrackableEntityDestroyed(@Nonnull Entity entity)
	{
		UUID entityID = entity.getUUID();
		if(Trackables.containsKey(entityID))
		{
			// Remove our reference completely, it is dead.
			TrackingEntry existing = Trackables.get(entityID);
			OnEntityDestroyed(entityID, entity, existing.LongEntity);
			OnEntityBecameUnloaded(entityID, entity, existing.LongEntity);
			Trackables.remove(entityID);
		}
	}


	public void OnAddedLoadedEntity(@Nonnull UUID entityID, @Nonnull Entity entity, @Nonnull LongDistanceEntity longEntity)
	{
		for(var playerTracking : Players.entrySet())
		{
			if(longEntity.ShouldBeTrackedBy(playerTracking.getKey()))
			{
				playerTracking.getValue().BeginTracking(entityID);
			}
		}
	}
	public void OnAddedUnloadedEntity(@Nonnull UUID entityID, @Nonnull LongDistanceEntity longEntity)
	{
		for(var playerTracking : Players.entrySet())
		{
			if(longEntity.ShouldBeTrackedBy(playerTracking.getKey()))
			{
				playerTracking.getValue().BeginTracking(entityID);
			}
		}
	}
	public void OnEntityBecameLoaded(@Nonnull UUID entityID, @Nonnull Entity entity, @Nonnull LongDistanceEntity longEntity)
	{
		longEntity.AfterEntityLoaded(entity);
	}
	public void OnEntityBecameUnloaded(@Nonnull UUID entityID, @Nonnull Entity entity, @Nonnull LongDistanceEntity longEntity)
	{
		longEntity.BeforeEntityUnloaded(entity);
	}
	public void OnEntityDestroyed(@Nonnull UUID entityID, @Nonnull Entity entity, @Nonnull LongDistanceEntity longEntity)
	{
		longEntity.BeforeEntityDestroyed(entity);
		for(var playerTracking : Players.entrySet())
		{
			playerTracking.getValue().StopTracking(entityID);
		}
	}


	public void Clear()
	{
		Trackables.clear();
		Players.clear();
	}
}
