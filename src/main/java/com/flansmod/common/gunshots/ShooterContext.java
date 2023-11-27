package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.GunContext;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class ShooterContext
{
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override
		public int GetNumValidContexts() { return 0; }
		@Override
		public GunContext[] GetAllActiveGunContexts() { return new GunContext[0]; }
		@Override
		public GunContext CreateForGunIndex(int gunSlotIndex) { return GunContext.INVALID; }
		@Override
		public GunContext CreateForSpecificStack(int gunSlotIndex, ItemStack stack) { return GunContext.GetGunContext(stack); }
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

		var cache = ContextCache(entity.level.isClientSide);

		UUID uuid = entity.getUUID();
		if(cache.containsKey(uuid))
			return cache.get(uuid);
		else
		{
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
	}

	@Nonnull
	public static ShooterContext GetOrCreate(UUID ownerUUID, UUID shooterUUID, boolean client)
	{
		var cache = ContextCache(client);
		if(cache.containsKey(shooterUUID))
			return cache.get(shooterUUID);
		else
		{
			if(client)
			{
				return Client_GetOrCreate(shooterUUID, ownerUUID);
			}
			else
			{
				return Server_GetOrCreate(shooterUUID, ownerUUID);
			}
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

	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE (Contained in the ShooterContext and built over time)
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<Integer, List<GunContext>> GunContextCache = new HashMap<>();
	@Nonnull
	public GunContext GetOrCreateSpecificContext(int slotIndex, int contextHash)
	{
		// Check we have a cache for this inventory slot
		if(!GunContextCache.containsKey(slotIndex))
			GunContextCache.put(slotIndex, new ArrayList<>());

		// Then check the most recent gun in that slot
		List<GunContext> gunsInSlot = GunContextCache.get(slotIndex);
		for(int i = gunsInSlot.size() - 1; i >= 0; i--)
		{
			GunContext gunInSlot = gunsInSlot.get(i);
			if(gunInSlot.IsValid() && gunInSlot.hashCode() == contextHash)
			{
				return gunInSlot;
			}
		}

		// If our gun wasn't found, maybe it hasn't been cached yet, so check the slot
		GunContext context = CreateForGunIndex(slotIndex);
		if(context.IsValid())
		{
			gunsInSlot.add(context);
			// If the current slot has a good context, check it is the one we are asking for before returning it
			if(context.hashCode() == contextHash)
			{
				return context;
			}
			else
			{
				FlansMod.LOGGER.error("Context mismatch for Shooter " + this + " in slot " + slotIndex + ". Got hash " + context.hashCode() + ", but we needed " + contextHash);
			}
		}

		// We want a specific context, not just the latest, so return invalid
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext CreateOldGunContext(int slotIndex, int contextHash, ItemStack snapshotOfStack)
	{
		GunContext currentMatch = GetOrCreateSpecificContext(slotIndex, contextHash);
		if(currentMatch.IsValid())
			return currentMatch;

		// Otherwise, this is a context of an old gun that is no longer in that slot,
		// and was probably last in that slot on a previous session
		return CreateForSpecificStack(slotIndex, snapshotOfStack);
	}

	@Nonnull
	public GunContext GetOrCreate(int slotIndex)
	{
		// Check we have a cache for this inventory slot
		if(!GunContextCache.containsKey(slotIndex))
			GunContextCache.put(slotIndex, new ArrayList<>());

		// Then check the most recent gun in that slot
		List<GunContext> gunsInSlot = GunContextCache.get(slotIndex);
		if(gunsInSlot.size() != 0)
		{
			GunContext mostRecentGun = gunsInSlot.get(gunsInSlot.size() - 1);
			if (mostRecentGun.IsValid())
			{
				boolean invalidated = mostRecentGun.UpdateFromItemStack();
				if(!invalidated)
					return mostRecentGun;
			}
		}

		// If there wasn't a good most recent gun, we should check to see if the slot has a new gun in it
		GunContext context = CreateForGunIndex(slotIndex);
		if(context.IsValid())
			gunsInSlot.add(context);

		// Now return this context, whether it is invalid or not
		return context;
	}
	// ---------------------------------------------------------------------------------------------------


	public ShooterContext()
	{
		ModifierCache = new ArrayList<>();
		ModifierHash = 0;
	}

	public boolean IsLocalPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player && player.isLocalPlayer();
	}

	@Nullable
	public Level Level()
	{
		return Entity() != null ? Entity().level : null;
	}
	@Nonnull
	public UUID EntityUUID() { return Entity() != null ? Entity().getUUID() : InvalidShooterContextUUID; }
	@Nonnull
	public UUID OwnerUUID() { return Owner() != null ? Owner().getUUID() : InvalidShooterContextUUID; }

	protected final List<ModifierDefinition> ModifierCache;
	private int ModifierHash;
	@Nonnull
	public List<ModifierDefinition> GetModifiers()
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
	public void Apply(ModifierStack modStack)
	{
		for(ModifierDefinition mod : GetModifiers())
			modStack.Apply(mod);
	}

	public void Save(CompoundTag tags)
	{
		tags.putUUID("owner", OwnerUUID());
		tags.putUUID("entity", EntityUUID());
	}

	public static ShooterContext Load(CompoundTag tags, boolean client)
	{
		UUID ownerID = tags.getUUID("owner");
		UUID entityID = tags.getUUID("entity");
		return ShooterContext.GetOrCreate(ownerID, entityID, client);
	}

	// ---------------------------------------------------------------------------------------------------
	// INTERFACE
	// ---------------------------------------------------------------------------------------------------
	public abstract int GetNumValidContexts();
	public abstract GunContext[] GetAllActiveGunContexts();
	public abstract GunContext CreateForGunIndex(int gunSlotIndex);
	public abstract GunContext CreateForSpecificStack(int gunSlotIndex, ItemStack stack);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Container GetAttachedInventory();
	public abstract Transform GetShootOrigin();
	public abstract boolean IsValid();
	public abstract boolean IsCreative();
	public abstract int HashModifierSources();
	public abstract void RecalculateModifierCache();


	@Override
	public int hashCode()
	{
		return EntityUUID().hashCode() ^ OwnerUUID().hashCode();
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof ShooterContext otherContext)
		{
			return EntityUUID().equals(otherContext.EntityUUID())
				&& OwnerUUID().equals(otherContext.OwnerUUID());
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "ShooterContext (Shooter:" + EntityUUID() + ", Owner:" + OwnerUUID() + ")";
	}
}
