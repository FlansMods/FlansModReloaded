package com.flansmod.common.actions.contexts;

import com.flansmod.common.item.FlanItem;
import com.flansmod.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ShooterContextUnresolvedEntity extends ShooterContext
{
	public final UUID OwnerUUID;
	public final UUID EntityUUID;

	public ShooterContextUnresolvedEntity(UUID ownerUUID, UUID entityUUID)
	{
		OwnerUUID = ownerUUID;
		EntityUUID = entityUUID;
	}

	@Nonnull
	@Override
	public UUID EntityUUID() { return EntityUUID; }
	@Nonnull
	@Override
	public UUID OwnerUUID() { return OwnerUUID; }

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
	public Entity Entity()
	{
		return null;
	}
	@Override
	public Entity Owner()
	{
		return null;
	}
	@Override
	public Container GetAttachedInventory() { return null; }
	@Override
	public Transform GetShootOrigin() { return null; }
	@Override
	public boolean IsValid() { return true; }
	@Override
	public boolean IsCreative() { return false; }
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}
}
