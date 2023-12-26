package com.flansmod.common.actions.contexts;

import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class ShooterContext
{
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override
		public int GetNumValidContexts() { return 0; }
		@Override
		@Nonnull
		public UUID[] GetAllGunIDs() { return new UUID[0]; }
		@Override
		@Nonnull
		public UUID GetGunIDForSlot(int gunSlotIndex) { return FlanItem.InvalidGunUUID; }
		@Override
		@Nonnull
		public GunContext CreateContext(UUID gunID) { return GunContext.INVALID; }
		@Override
		public Entity Entity() { return null; }
		@Override
		public Entity Owner() { return null; }
		@Override
		public Transform GetShootOrigin() { return null; }
		@Override
		public boolean IsValid() { return false; }
		@Override
		public boolean IsCreative() { return false; }
		@Override
		public int hashCode() { return 0; }
		@Override
		public Container GetAttachedInventory() { return null; }
		@Override
		public int HashModifierSources() { return 0; }
		@Override
		public void RecalculateModifierCache() {}
	};

	// ---------------------------------------------------------------------------------------------------
	// SHOOTER CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	public static final UUID InvalidShooterContextUUID = new UUID(0L, 0L);
	private static final HashMap<UUID, ShooterContext> Client_ContextCache = new HashMap<>();
	private static final HashMap<UUID, ShooterContext> Server_ContextCache = new HashMap<>();
	private static HashMap<UUID, ShooterContext> ContextCache(boolean client)
	{
		if(client)
			return Client_ContextCache;
		else
			return Server_ContextCache;
	}

	public static void OnLevelLoaded()
	{
		Client_ContextCache.clear();
		Server_ContextCache.clear();
	}

	@Nonnull
	public static ShooterContext GetOrCreate(Entity entity)
	{
		if(entity == null)
			return ShooterContext.INVALID;

		var cache = ContextCache(entity.level().isClientSide);

		UUID uuid = entity.getUUID();
		if(cache.containsKey(uuid))
		{
			ShooterContext context = cache.get(uuid);
			if(context.Entity() == entity)
				return context;
			else
				cache.remove(uuid);
		}

		ShooterContext context;
		if(entity instanceof Player player)
			context = new ShooterContextPlayer(player);
		else if(entity instanceof LivingEntity living)
			context = new ShooterContextLiving(living);
		else
			context = INVALID;

		cache.put(uuid, context);
		return context;
	}

	@Nonnull
	public static ShooterContext GetOrCreate(UUID ownerUUID, UUID shooterUUID, boolean client)
	{
		var cache = ContextCache(client);
		ShooterContext context;
		if (cache.containsKey(shooterUUID))
		{
			context = cache.get(shooterUUID);
			if(context.Entity() != null && context.Entity().isRemoved())
				cache.remove(shooterUUID);
			else
				return context;
		}


		if(client)
		{
			return Client_GetOrCreate(shooterUUID, ownerUUID);
		}
		else
		{
			return Server_GetOrCreate(shooterUUID, ownerUUID);
		}
	}

	@Nonnull
	public static ShooterContext GetOrCreate(UUID uuid, boolean client)
	{
		return GetOrCreate(uuid, uuid, client);
	}

	@OnlyIn(Dist.CLIENT)
	private static ShooterContext Client_GetOrCreate(UUID shooterUUID, UUID ownerUUID)
	{
		if(Minecraft.getInstance().level != null)
		{
			for (Entity entity : Minecraft.getInstance().level.entitiesForRendering())
			{
				if(entity.getUUID().equals(shooterUUID))
					return GetOrCreate(entity);
			}
		}
		return new ShooterContextUnresolvedEntity(ownerUUID, shooterUUID);
	}

	private static ShooterContext Server_GetOrCreate(UUID shooterUUID, UUID ownerUUID)
	{
		MinecraftServer server = MinecraftHelpers.GetServer();
		if(server != null && server.isRunning())
		{
			for(ServerLevel serverLevel : server.getAllLevels())
			{
				Entity shooter = serverLevel.getEntity(shooterUUID);
				if(shooter != null)
					return GetOrCreate(shooter);
			}
		}
		return new ShooterContextUnresolvedEntity(ownerUUID, shooterUUID);
	}
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<ModifierDefinition, Float> ModifierCache;
	private int ModifierHash;
	protected void AddModifierToCache(ModifierDefinition modDef, float multiplier)
	{
		ModifierCache.put(modDef, ModifierCache.getOrDefault(modDef, 0.0f) + multiplier);
	}

	public ShooterContext()
	{
		ModifierCache = new HashMap<>();
		ModifierHash = 0;
	}

	public boolean IsLocalPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player && player.isLocalPlayer();
	}

	@Nullable
	public Level Level()
	{
		return Entity() != null ? Entity().level() : null;
	}
	@Nonnull
	public UUID EntityUUID() { return Entity() != null ? Entity().getUUID() : InvalidShooterContextUUID; }
	@Nonnull
	public UUID OwnerUUID() { return Owner() != null ? Owner().getUUID() : InvalidShooterContextUUID; }


	@Nonnull
	public Map<ModifierDefinition, Float> GetModifiers()
	{
		int updatedModifierHash = HashModifierSources();
		if(updatedModifierHash != ModifierHash)
		{
			ModifierCache.clear();
			RecalculateModifierCache();
			ModifierHash = updatedModifierHash;
		}
		return ModifierCache;
	}
	public void Apply(@Nonnull ModifierStack modStack)
	{
		for(var kvp : GetModifiers().entrySet())
			modStack.Modify(kvp.getKey(), kvp.getValue());
	}

	public void Save(@Nonnull CompoundTag tags)
	{
		tags.putUUID("owner", OwnerUUID());
		tags.putUUID("entity", EntityUUID());
	}

	@Nonnull
	public static ShooterContext Load(@Nonnull CompoundTag tags, boolean client)
	{
		UUID ownerID = tags.getUUID("owner");
		UUID entityID = tags.getUUID("entity");
		return ShooterContext.GetOrCreate(ownerID, entityID, client);
	}

	@Nonnull
	public GunContext GetGunContextForSlot(int gunSlotIndex, boolean client)
	{
		UUID gunID = GetGunIDForSlot(gunSlotIndex);
		if(gunID != FlanItem.InvalidGunUUID)
			return GunContextCache.Get(client).Create(this, gunID);
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext[] GetAllGunContexts(boolean client)
	{
		UUID[] ids = GetAllGunIDs();
		GunContext[] contexts = new GunContext[ids.length];
		for(int i = 0; i < ids.length; i++)
		{
			contexts[i] = GunContextCache.Get(client).Create(this, ids[i]);
		}
		return contexts;
	}

	// ---------------------------------------------------------------------------------------------------
	// INTERFACE
	// ---------------------------------------------------------------------------------------------------
	public abstract int GetNumValidContexts();
	@Nonnull
	public abstract UUID[] GetAllGunIDs();
	@Nonnull
	public abstract UUID GetGunIDForSlot(int gunSlotIndex);
	@Nonnull
	public abstract GunContext CreateContext(UUID gunID);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Container GetAttachedInventory();
	public abstract Transform GetShootOrigin();
	public abstract boolean IsValid();
	public abstract boolean IsCreative();
	public abstract int HashModifierSources();
	public abstract void RecalculateModifierCache();

	@Override
	public String toString()
	{
		return "ShooterContext (Shooter:" + EntityUUID() + ", Owner:" + OwnerUUID() + ")";
	}
}
