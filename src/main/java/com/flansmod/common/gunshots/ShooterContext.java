package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
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
		public  GunContext CreateForGunIndex(int gunSlotIndex) { return GunContext.INVALID; }
		@Override
		public ActionGroupContext[] GetPrioritisedActions(EActionInput action) { return new ActionGroupContext[0]; }
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
	public static ShooterContext GetOrCreate(UUID uuid, boolean client)
	{
		var cache = ContextCache(client);
		if(cache.containsKey(uuid))
			return cache.get(uuid);
		else
		{
			if(client)
			{
				return Client_GetOrCreate(uuid);
			}
			else
			{
				return Server_GetOrCreate(uuid);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static ShooterContext Client_GetOrCreate(UUID uuid)
	{
		if(Minecraft.getInstance().level != null)
		{
			for (Entity entity : Minecraft.getInstance().level.entitiesForRendering())
			{
				if(entity.getUUID().equals(uuid))
					return GetOrCreate(entity);
			}
		}
		return ShooterContext.INVALID;
	}

	private static ShooterContext Server_GetOrCreate(UUID uuid)
	{
		MinecraftServer server = MinecraftHelpers.GetServer();
		if(server != null && server.isRunning())
		{
			for(ServerLevel serverLevel : server.getAllLevels())
			{
				Entity entity = serverLevel.getEntity(uuid);
				if(entity != null)
					return GetOrCreate(entity);
			}
		}
		return ShooterContext.INVALID;
	}
	// ---------------------------------------------------------------------------------------------------

	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE (Contained in the ShooterContext and built over time)
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<Integer, GunContext> GunContextCache = new HashMap<>();
	@Nonnull
	public GunContext GetOrCreate(int slotIndex)
	{
		if(GunContextCache.containsKey(slotIndex))
			return GunContextCache.get(slotIndex);

		GunContext context = CreateForGunIndex(slotIndex);
		GunContextCache.put(slotIndex, context);
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

	// ---------------------------------------------------------------------------------------------------
	// INTERFACE
	// ---------------------------------------------------------------------------------------------------
	public abstract int GetNumValidContexts();
	public abstract GunContext[] GetAllActiveGunContexts();
	public abstract GunContext CreateForGunIndex(int gunSlotIndex);
	public abstract ActionGroupContext[] GetPrioritisedActions(EActionInput action);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Container GetAttachedInventory();
	public abstract Transform GetShootOrigin();
	public abstract boolean IsValid();
	public abstract boolean IsCreative();
	public abstract int HashModifierSources();
	public abstract void RecalculateModifierCache();

}
