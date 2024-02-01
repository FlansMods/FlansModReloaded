package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionManager;
import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
	private final HashMap<UUID, ShooterContextHistory> ShooterContexts = new HashMap<>();
	private final HashMap<UUID, GunContextHistory> GunContextCache = new HashMap<>();
	protected final EContextSide Side;

	public ContextCache(EContextSide side)
	{
		Side = side;
	}

	public void OnLevelUnloaded(@Nonnull ActionManager actionManager)
	{
		//actionManager.Clear();
		//ShooterContexts.clear();
		//GunContextCache.clear();
	}

	public void ClearPlayer(@Nonnull UUID playerID, @Nonnull ActionManager actionManager)
	{
		//ShooterContexts.remove(playerID);
		//
		//List<UUID> gunsToClear = new ArrayList<>();
		//for(var kvp : GunContextCache.entrySet())
		//	if(kvp.getValue().GetShooter().OwnerUUID().equals(playerID))
		//		gunsToClear.add(kvp.getKey());
		//
		//for(UUID gunID : gunsToClear)
		//{
		//	actionManager.ClearGun(gunID);
		//	GunContextCache.remove(gunID);
		//}
	}

	// ---------------------------------------------------------------------------------------------------
	// SHOOTER CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	@Nonnull
	private ShooterContextHistory HistoryOfShooter(@Nonnull UUID shooterID)
	{
		if(shooterID.equals(ShooterContext.INVALID.EntityUUID()))
			return ShooterContextHistory.INVALID;

		ShooterContextHistory history = ShooterContexts.get(shooterID);
		if(history == null)
		{
			history = new ShooterContextHistory(shooterID);
			ShooterContexts.put(shooterID, history);
		}
		return history;
	}

	@Nonnull
	public ShooterContext GetShooter(@Nonnull Entity shooter)
	{
		return HistoryOfShooter(shooter.getUUID()).ContextualizeWith(shooter);
	}
	@Nonnull
	public ShooterContext GetShooter(@Nonnull Entity shooter, @Nullable Entity owner)
	{
		return GetShooter(shooter.getUUID(), owner != null ? owner.getUUID() : ShooterContext.InvalidID, shooter.level());
	}
	@Nonnull
	public ShooterContext GetShooter(@Nonnull UUID shooterID, @Nonnull UUID ownerID, @Nullable Level checkLevel)
	{
		return HistoryOfShooter(shooterID).ContextualizeWith(ownerID, shooterID, checkLevel);
	}
	@Nonnull
	private ShooterContext TryGetExistingShooter(@Nonnull UUID shooterID, @Nonnull UUID ownerID)
	{
		return HistoryOfShooter(shooterID).ContextualizeWith(ownerID, shooterID, this::TryFindEntity);
	}
	@Nullable
	protected abstract Entity TryFindEntity(@Nonnull UUID entityID);


	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE
	// ---------------------------------------------------------------------------------------------------
	@Nonnull
	public GunContextHistory HistoryOfGun(@Nonnull UUID gunID)
	{
		if(gunID.equals(GunContext.INVALID.GetUUID()))
			return GunContextHistory.INVALID;

		GunContextHistory history = GunContextCache.get(gunID);
		if(history == null)
		{
			history = new GunContextHistory(gunID);
			GunContextCache.put(gunID, history);
		}
		return history;
	}

	// Only use when you really don't know which logical side you are on
	@Nonnull
	public static GunContextItem CreateWithoutCaching(@Nonnull ItemStack stack)
	{
		return new GunContextItem(stack);
	}

	@Nonnull
	public GunContext GetLastKnownAppearanceOfGun(@Nonnull UUID gunID)
	{
		return HistoryOfGun(gunID).WithoutContext();
	}

	@Nullable
	public GunContext GetContextIfStillValid(@Nonnull UUID gunID)
	{
		return HistoryOfGun(gunID).GetMostRecentValidContext(GunContext::IsValid);
	}

	// The most minimal context - you just know that you have an item, but not necessarily where it is
	// This method may return a context with more information, depending on where the gun last was
	@Nonnull
	public GunContext Create(@Nonnull ItemStack stack)
	{
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			return HistoryOfGun(gunID).ContextualizeWith(stack);
		}
		return GunContext.INVALID;
	}

	// The next step up, you know which container it is in
	@Nonnull
	public GunContext Create(@Nonnull Container container, int slot)
	{
		ItemStack stack = container.getItem(slot);
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			return HistoryOfGun(gunID).ContextualizeWith(container, slot);
		}
		return GunContext.INVALID;
	}

	// Now to get more specific, maybe you know you are looking inside a TileEntity
	@Nonnull
	public GunContext Create(@Nonnull BlockEntity blockEntity, @Nonnull Container container, int slot)
	{
		ItemStack stack = container.getItem(slot);
		UUID gunID = FlanItem.GetGunID(stack);
		if(gunID != FlanItem.InvalidGunUUID)
		{
			return HistoryOfGun(gunID).ContextualizeWith(blockEntity, container, slot);
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext Create(@Nonnull ItemEntity itemEntity)
	{
		UUID gunID = FlanItem.GetGunID(itemEntity.getItem());
		if(gunID != FlanItem.InvalidGunUUID)
		{
			return HistoryOfGun(gunID).ContextualizeWith(itemEntity);
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext Create(@Nonnull ShooterContext shooter, @Nonnull InteractionHand hand)
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
	public GunContext Create(@Nonnull ShooterContext shooter, int gunSlotIndex)
	{
		if(VerifyCallingFromCorrectSide(shooter))
		{
			UUID gunID = shooter.GetGunIDForSlot(gunSlotIndex);
			if(gunID != FlanItem.InvalidGunUUID)
				return HistoryOfGun(gunID).ContextualizeWith(shooter, gunSlotIndex);
		}
		return GunContext.INVALID;
	}
	// Or you are looking at something that can shoot
	@Nonnull
	public GunContext Create(@Nonnull ShooterContext shooter, @Nonnull UUID gunID)
	{
		if(gunID != FlanItem.InvalidGunUUID)
		{
			return HistoryOfGun(gunID).ContextualizeWith(shooter);
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
