package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.item.FlanItem;
import com.flansmod.physics.common.util.EContextSide;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ShooterContextUnresolvedEntity extends ShooterContext
{
	@Nonnull
	public final EContextSide Side;
	@Nonnull
	public final UUID OwnerUUID;
	@Nonnull
	public final UUID EntityUUID;

	public ShooterContextUnresolvedEntity(@Nonnull UUID ownerUUID, @Nonnull UUID entityUUID, @Nonnull EContextSide side)
	{
		Side = side;
		OwnerUUID = ownerUUID;
		EntityUUID = entityUUID;
	}

	@Override @Nonnull
	public UUID ShooterID() { return EntityUUID; }
	@Override
	@Nonnull
	public EContextSide GetSide() { return Side; }
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
	public GunContext CreateContext(@Nonnull UUID gunID) { return GunContext.INVALID; }
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
	@Nonnull
	public Transform GetShootOrigin(float deltaTick) { return Transform.IDENTITY; }
	@Override
	public boolean IsValid() { return true; }
	@Override
	public boolean IsCreative() { return false; }
	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker) {}

	@Override
	public String toString()
	{
		return "Shooter ("+EntityUUID+")" + (OwnerUUID.equals(EntityUUID) ? "" : " | Owner ("+OwnerUUID+")");
	}
}
