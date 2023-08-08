package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ShooterContext
{
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override
		public int GetNumValidContexts() { return 0; }
		@Override
		public GunContext[] GetAllGunContexts() { return new GunContext[0]; }
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
	// CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	private static class ShooterContextID
	{
		public final ResourceKey<Level> Dimension;
		public final int EntityID;
		public ShooterContextID(Entity entity)
		{
			if(entity != null)
			{
				Dimension = entity.getLevel().dimension();
				EntityID = entity.getId();
			}
			else
			{
				Dimension = Level.OVERWORLD;
				EntityID = 0;
			}
		}
		public int hashCode() { return (Dimension.hashCode() << 8) ^ EntityID; }
	}
	private static final HashMap<ShooterContextID, ShooterContext> ContextCache = new HashMap<>();
	@Nonnull
	public static ShooterContext CreateFrom(Entity entity)
	{
		ShooterContextID id = new ShooterContextID(entity);
		if(ContextCache.containsKey(id))
			return ContextCache.get(id);
		else
		{
			ShooterContext context;
			if(entity instanceof Player player)
				context = new ShooterContextPlayer(player);
			else if(entity instanceof LivingEntity living)
				context = new ShooterContextLiving(living);
			else
				context = INVALID;
			ContextCache.put(id, context);
			return context;
		}
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
	public abstract GunContext[] GetAllGunContexts();
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
