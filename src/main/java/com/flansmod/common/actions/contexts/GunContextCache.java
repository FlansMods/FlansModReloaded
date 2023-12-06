package com.flansmod.common.actions.contexts;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class GunContextCache
{
	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<UUID, GunContext> GunContextCache = new HashMap<>();

	public static GunContextCache Get(boolean client)
	{
		if(client)
			return FlansModClient.GUN_CONTEXTS_CLIENT;
		else
			return FlansMod.GUN_CONTEXTS_SERVER;
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
			if(existingContext.IsValid())
				return existingContext;
		}
		return null;
	}

	// Only use when you really don't know which logical side you are on
	@Nonnull
	public static GunContextItem CreateWithoutCaching(ItemStack stack)
	{
		return new GunContextItem(stack);
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
		if (shooter.IsValid())
		{
			int slot = 0;
			if (shooter instanceof ShooterContextPlayer player)
				slot = hand == InteractionHand.MAIN_HAND ? player.Player.getInventory().selected : Inventory.SLOT_OFFHAND;
			else
				slot = hand == InteractionHand.MAIN_HAND ? 0 : 1;
			return Create(shooter, shooter.GetGunIDForSlot(slot));
		}
		return GunContext.INVALID;
	}
	@Nonnull
	public GunContext Create(ShooterContext shooter, int gunSlotIndex)
	{
		if (shooter.IsValid())
			return Create(shooter, shooter.GetGunIDForSlot(gunSlotIndex));
		return GunContext.INVALID;
	}
	// Or you are looking at something that can shoot
	@Nonnull
	public GunContext Create(ShooterContext shooter, UUID gunID)
	{
		if(gunID != FlanItem.InvalidGunUUID)
		{
			// Check for an existing context
			GunContext existingContext = GetContextIfStillValid(gunID);
			if (existingContext != null)
			{
				if (existingContext instanceof GunContextPlayer)
				{
					if (shooter instanceof ShooterContextPlayer)
						return existingContext;
					else
						FlansMod.LOGGER.warn("Found GunContextPlayer under non-player ShooterContext " + shooter);
				} else if (existingContext instanceof GunContextLiving)
				{
					if (shooter instanceof ShooterContextLiving)
						return existingContext;
					else
						FlansMod.LOGGER.warn("Found GunContextLiving under non-living ShooterContext " + shooter);
				}
			}

			GunContext newContext = shooter.CreateContext(gunID);
			GunContextCache.put(gunID, newContext);
			return newContext;
		}

		return GunContext.INVALID;
	}

}
