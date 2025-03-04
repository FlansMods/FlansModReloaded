package com.flansmod.common.actions.contexts;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.stats.*;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.Constants;
import com.flansmod.physics.common.util.EContextSide;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.util.formulae.FloatAccumulation;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class ShooterContext
{
	public static ResourceKey<Level> DimensionUnknown = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(FlansMod.MODID, "null"));
	public static final UUID InvalidID = new UUID(0L, 0L);
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override @Nonnull
		public UUID ShooterID() { return InvalidID; }
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
		public GunContext CreateContext(@Nonnull UUID gunID) { return GunContext.INVALID; }
		@Override @Nullable
		public Entity Entity() { return null; }
		@Override @Nullable
		public Entity Owner() { return null; }
		@Override
		@Nonnull
		public Transform GetShootOrigin(float deltaTick) { return Transform.IDENTITY; }
		@Override
		public boolean IsValid() { return false; }
		@Override
		public boolean IsCreative() { return false; }
		@Override
		public int hashCode() { return 0; }
		@Override @Nullable
		public Container GetAttachedInventory() { return null; }
		@Override
		public void BakeModifiers(@Nonnull IModifierBaker baker) {}
	};

	@Nonnull
	public static ShooterContext of(@Nullable Entity shooter)
	{
		if(shooter != null)
		{
			if (shooter.level().isClientSide)
				return client(shooter);
			return server(shooter);
		}
		return INVALID;
	}
	@Nonnull
	public static ShooterContext of(@Nullable ShooterBlockEntity shooter)
	{
		if(shooter != null)
		{
			if (shooter.getLevel().isClientSide)
				return client(shooter);
			return server(shooter);
		}
		return INVALID;
	}
	@Nonnull
	public static ShooterContext of(@Nullable Entity shooter, @Nullable Entity owner)
	{
		if(shooter != null)
		{
			if (shooter.level().isClientSide)
				return client(shooter, owner);
			return server(shooter, owner);
		}
		return INVALID;
	}
	@Nonnull
	public static ShooterContext of(@Nonnull UUID shooterID, @Nonnull UUID ownerID, boolean isClient)
	{
		return isClient ? client(shooterID, ownerID) : server(shooterID, ownerID);
	}
	@Nonnull
	public static ShooterContext of(@Nonnull UUID shooterID, @Nonnull UUID ownerID)
	{
		return MinecraftHelpers.isClientThread() ? client(shooterID, ownerID) : server(shooterID, ownerID);
	}
	@Nonnull
	public static ShooterContext server(@Nonnull Entity shooter) { return FlansMod.CONTEXT_CACHE.GetShooter(shooter); }
	@Nonnull
	public static ShooterContext server(@Nonnull ShooterBlockEntity shooter) { return FlansMod.CONTEXT_CACHE.GetShooter(shooter); }
	@Nonnull
	public static ShooterContext server(@Nonnull Entity shooter, @Nullable Entity owner) { return FlansMod.CONTEXT_CACHE.GetShooter(shooter, owner); }
	@Nonnull
	public static ShooterContext server(@Nonnull UUID shooterID, @Nonnull UUID ownerID) { return FlansMod.CONTEXT_CACHE.GetShooter(shooterID, ownerID, null); }

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull Entity shooter) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooter); }
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull ShooterBlockEntity shooter) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooter); }
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull Entity shooter, @Nullable Entity owner) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooter, owner); }
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull UUID shooterID, @Nonnull UUID ownerID) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooterID, ownerID, MinecraftHelpers.clientGetCurrentLevel()); }


	// ---------------------------------------------------------------------------------------------------
	private boolean IsCurrent;

	public ShooterContext()
	{
		ModCache = new ModifierCache(this::BakeModifiers);
		IsCurrent = true;
	}


	// Provides the netsync UUID. Can be exactly an Entity UUID or a constructed one for a block entity
	@Nonnull
	public abstract UUID ShooterID();

	public boolean IsPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player;
	}
	public boolean IsLocalPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player && player.isLocalPlayer();
	}
	@Nonnull
	public EContextSide GetSide() { return EContextSide.of(Entity()); }
	@Nonnull
	public ResourceKey<Level> Dimension()
	{
		Level level = Level();
		return level != null ? level.dimension() : DimensionUnknown;
	}
	@Nullable
	public Level Level()
	{
		return Entity() != null ? Entity().level() : null;
	}
	@Nonnull
	public UUID EntityUUID() { return Entity() != null ? Entity().getUUID() : InvalidID; }
	@Nonnull
	public UUID OwnerUUID() { return Owner() != null ? Owner().getUUID() : InvalidID; }
	public void MarkAsOld() { IsCurrent = false; }

	// ---------------------------------------------------------------------------------------------------
	// MODIFIER CACHE
	// ---------------------------------------------------------------------------------------------------
	private final ModifierCache ModCache;

	@Nonnull
	public StatAccumulator GetModifierFormula(@Nonnull String stat) { return ModCache.GetModifierFormula(stat); }
	@Nonnull
	public Optional<String> GetStringOverride(@Nonnull String stat) { return ModCache.GetStringOverride(stat); }

	// Attribute Modifiers for specific stats
	private static final HashMap<String, RegistryObject<RangedAttribute>> StatToAttribute = new HashMap<>();
	static
	{
		StatToAttribute.put(Constants.STAT_IMPACT_DAMAGE, FlansMod.IMPACT_DAMAGE_MULTIPLIER);
		StatToAttribute.put(Constants.STAT_SHOOT_SPLASH_RADIUS, FlansMod.SPLASH_RADIUS_MULTIPLIER);
		StatToAttribute.put(Constants.STAT_GROUP_REPEAT_DELAY, FlansMod.TIME_BETWEEN_SHOTS_MULTIPLIER);
		StatToAttribute.put(Constants.STAT_SHOT_SPREAD, FlansMod.SHOT_SPREAD_MULTIPLIER);
		StatToAttribute.put(Constants.STAT_SHOT_VERTICAL_RECOIL, FlansMod.VERTICAL_RECOIL_MULTIPLIER);
		StatToAttribute.put(Constants.STAT_SHOT_HORIZONTAL_RECOIL, FlansMod.HORIZONTAL_RECOIL_MULTIPLIER);


	}

	@Nonnull
	public FloatAccumulation ModifyFloat(@Nonnull String stat)
	{
		// Apply Attribute modifiers, if they are present
		if(Owner() instanceof LivingEntity living)
		{
			RegistryObject<RangedAttribute> attrib = StatToAttribute.get(stat);
			if(attrib != null)
			{
				AttributeInstance attribMulti = living.getAttribute(attrib.get());
				if(attribMulti != null)
				{
					return FloatAccumulation.compose(
						GetModifierFormula(stat).Calculate(IStatCalculatorContext.Invalid),
						FloatAccumulation.of(0f, 0f, (float)attribMulti.getValue(), 0f));
				}
			}
		}

		return FloatAccumulation.compose(GetModifierFormula(stat).Calculate(IStatCalculatorContext.Invalid));
	}
	@Nonnull
	public String ModifyString(@Nonnull String stat, @Nonnull String defaultValue)
	{
		return GetStringOverride(stat).orElse(defaultValue);
	}
	// ---------------------------------------------------------------------------------------------------

	public void Save(@Nonnull CompoundTag tags)
	{
		tags.putUUID("owner", OwnerUUID());
		tags.putUUID("entity", EntityUUID());
	}

	@Nonnull
	public static ShooterContext Load(@Nonnull CompoundTag tags, boolean client)
	{
		if(!tags.hasUUID("owner")) return null;
		UUID ownerID = tags.getUUID("owner");
		UUID entityID = tags.getUUID("entity");
		return ShooterContext.of(ownerID, entityID, client);
	}

	@Nonnull
	public GunContext GetGunContextForSlot(int gunSlotIndex, boolean client)
	{
		UUID gunID = GetGunIDForSlot(gunSlotIndex);
		if(gunID != FlanItem.InvalidGunUUID)
			return GunContext.of(this, gunID);
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext[] GetAllGunContexts()
	{
		UUID[] ids = GetAllGunIDs();
		GunContext[] contexts = new GunContext[ids.length];
		for(int i = 0; i < ids.length; i++)
		{
			contexts[i] = GunContext.of(this, ids[i]);
			contexts[i].UpdateFromItemStack();
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
	public abstract GunContext CreateContext(@Nonnull UUID gunID);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Container GetAttachedInventory();
	@Nonnull
	public abstract Transform GetShootOrigin(float deltaTick); // delta will be 0 on server
	public abstract boolean IsValid();
	public abstract boolean IsCreative();

	public abstract void BakeModifiers(@Nonnull IModifierBaker baker);

	@Override
	public String toString()
	{
		return "ShooterContext (Shooter:" + EntityUUID() + ", Owner:" + OwnerUUID() + ")";
	}
}
