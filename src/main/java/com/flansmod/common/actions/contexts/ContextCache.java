package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionManager;
import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class ContextCache
{
	private final HashMap<UUID, ShooterContext> ShooterContexts = new HashMap<>();
	private final HashMap<UUID, GunContext> GunContextCache = new HashMap<>();
	protected final EContextSide Side;

	public ContextCache(EContextSide side)
	{
		Side = side;
	}

	public void OnLevelUnloaded(@Nonnull ActionManager actionManager)
	{
		actionManager.Clear();
		ShooterContexts.clear();
		GunContextCache.clear();
	}

	public void ClearPlayer(@Nonnull UUID playerID, @Nonnull ActionManager actionManager)
	{
		ShooterContexts.remove(playerID);

		List<UUID> gunsToClear = new ArrayList<>();
		for(var kvp : GunContextCache.entrySet())
			if(kvp.getValue().GetShooter().OwnerUUID().equals(playerID))
				gunsToClear.add(kvp.getKey());

		for(UUID gunID : gunsToClear)
		{
			actionManager.ClearGun(gunID);
			GunContextCache.remove(gunID);
		}
	}

	public abstract boolean SidedValidation(@Nonnull ShooterContext shooter);

	// ---------------------------------------------------------------------------------------------------
	// SHOOTER CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	@Nonnull
	public ShooterContext GetShooter(@Nonnull Entity shooter)
	{
		return GetShooter(shooter.getUUID(), shooter.getUUID(), shooter.level());
	}
	@Nonnull
	public ShooterContext GetShooter(@Nonnull Entity shooter, @Nullable Entity owner)
	{
		return GetShooter(shooter.getUUID(), owner != null ? owner.getUUID() : ShooterContext.InvalidID, shooter.level());
	}
	@Nonnull
	public ShooterContext GetShooter(@Nonnull UUID shooterID, @Nonnull UUID ownerID, @Nullable Level checkLevel)
	{
		ShooterContext existing = TryGetExistingShooter(shooterID, ownerID);
		if(existing != null)
			if(existing.EntityUUID().equals(shooterID))
				if(existing.OwnerUUID().equals(ownerID))
					if(checkLevel == null || checkLevel == existing.Level())
						if(SidedValidation(existing))
							return existing;

		ShooterContext newContext = CreateShooterContext(shooterID, ownerID);
		ShooterContexts.put(shooterID, newContext);
		return newContext;
	}
	@Nullable
	private ShooterContext TryGetExistingShooter(@Nonnull UUID shooterID, @Nonnull UUID ownerID)
	{
		if (ShooterContexts.containsKey(shooterID))
		{
			ShooterContext candidate = ShooterContexts.get(shooterID);
			candidate = ValidateShooterContext(candidate);
			if(candidate != null)
				return candidate;
		}

		return null;
	}
	@Nullable
	private ShooterContext ValidateShooterContext(ShooterContext shooterContext)
	{
		if(!VerifyCallingFromCorrectSide(shooterContext))
			return null;
		if(shooterContext instanceof ShooterContextUnresolvedEntity unresolvedContext)
		{
			// Try find the actual context
			return TryResolve(unresolvedContext);
		}
		return shooterContext;
	}
	@Nonnull
	protected abstract ShooterContext CreateShooterContext(@Nonnull UUID shooterID, @Nonnull UUID ownerID);
	@Nonnull
	protected abstract ShooterContext TryResolve(@Nonnull ShooterContextUnresolvedEntity unresolvedContext);

	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	// Only use when you really don't know which logical side you are on
	@Nonnull
	public static GunContextItem CreateWithoutCaching(ItemStack stack)
	{
		return new GunContextItem(stack);
	}

	@Nonnull
	public GunContext GetLastKnownAppearanceOfGun(UUID gunID)
	{
		if(GunContextCache.containsKey(gunID))
			return GunContextCache.get(gunID);
		return GunContext.INVALID;
	}

	@Nullable
	public GunContext GetContextIfStillValid(UUID gunID)
	{
		if(GunContextCache.containsKey(gunID))
		{
			GunContext existingContext = GunContextCache.get(gunID);
			if(existingContext.IsValid() && existingContext.GetUUID().equals(gunID))
				return existingContext;
		}
		return null;
	}

	// The most minimal context - you just know that you have an item, but not necessarily where it is
	// This method may return a context with more information, depending on where the gun last was
	@Nonnull
	public GunContext Create(ItemStack stack)
	{
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			// Check for an existing context - if it contains more context than we expected, that's okay
			GunContext existingContext = GetContextIfStillValid(gunID);
			if(existingContext != null)
				return existingContext;

			// Okay, we don't know this gun, just wrap and cache a simple Item Context
			GunContextItem itemContext = new GunContextItem(stack);
			GunContextCache.put(gunID, itemContext);
			return itemContext;
		}
		return GunContext.INVALID;
	}

	// The next step up, you know which container it is in
	@Nonnull
	public GunContext Create(Container container, int slot)
	{
		ItemStack stack = container.getItem(slot);
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			// Check for an existing context
			GunContext existingContext = GetContextIfStillValid(gunID);
			if (existingContext != null)
			{
				// If we found an InventoryItem context, we can ship it
				// This could contain more context than we asked for, but that's okay
				if (existingContext instanceof GunContextInventoryItem)
				{
					return existingContext;
				}
			}

			GunContextInventoryItem containerContext = new GunContextInventoryItem(container, slot);
			GunContextCache.put(gunID, containerContext);
			return containerContext;
		}
		return GunContext.INVALID;
	}

	// Now to get more specific, maybe you know you are looking inside a TileEntity
	@Nonnull
	public GunContext Create(BlockEntity blockEntity, Container container, int slot)
	{
		ItemStack stack = container.getItem(slot);
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			// Check for an existing context
			GunContext existingContext = GetContextIfStillValid(gunID);
			if (existingContext != null)
			{
				if(existingContext instanceof GunContextTileEntity tileEntityContext)
				{
					if(tileEntityContext.TileEntity == blockEntity)
						return existingContext;
					else
						FlansMod.LOGGER.warn("Getting TileEntity GunContext found different TileEntity, this should be handled in GetContextIfStillValid");
				}
			}

			GunContextTileEntity tileEntityContext = new GunContextTileEntity(blockEntity, container, slot);
			GunContextCache.put(gunID, tileEntityContext);
			return tileEntityContext;
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext Create(ShooterContext shooter, InteractionHand hand)
	{
		if(!VerifyCallingFromCorrectSide(shooter))
			return GunContext.INVALID;

		if (!shooter.IsValid())
			return GunContext.INVALID;

		int slot;
		if (shooter instanceof ShooterContextPlayer player)
			slot = hand == InteractionHand.MAIN_HAND ? player.Player.getInventory().selected : Inventory.SLOT_OFFHAND;
		else
			slot = hand == InteractionHand.MAIN_HAND ? 0 : 1;
		return Create(shooter, shooter.GetGunIDForSlot(slot));
	}
	@Nonnull
	public GunContext Create(ShooterContext shooter, int gunSlotIndex)
	{
		if(!VerifyCallingFromCorrectSide(shooter))
			return GunContext.INVALID;

		if (shooter.IsValid())
			return Create(shooter, shooter.GetGunIDForSlot(gunSlotIndex));

		return GunContext.INVALID;
	}
	// Or you are looking at something that can shoot
	@Nonnull
	public GunContext Create(@Nonnull ShooterContext shooter, @Nonnull UUID gunID)
	{
		if(gunID != FlanItem.InvalidGunUUID)
		{
			// Check for an existing context
			GunContext existingContext = GetContextIfStillValid(gunID);
			if (existingContext != null)
			{
				// Check for context EQUALITY, this should not be different...
				if(existingContext.GetShooter() == shooter)
				{
					if(!existingContext.UpdateStackFromInventory())
						return existingContext;
				}
			}

			GunContext newContext = shooter.CreateContext(gunID);
			GunContextCache.put(gunID, newContext);
			return newContext;
		}

		return GunContext.INVALID;
	}


	private boolean VerifyCallingFromCorrectSide(@Nonnull ShooterContext shooter)
	{
		EContextSide side = shooter.GetSide();
		return side == Side || side == EContextSide.Unknown;
	}
	private boolean VerifyCallingFromCorrectSide(@Nonnull GunContext gun)
	{
		EContextSide side = gun.GetShooter().GetSide();
		return side == Side || side == EContextSide.Unknown;
	}
}
