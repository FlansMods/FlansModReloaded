package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ShooterBlockEntity extends BlockEntity
{
    public ShooterBlockEntity(@Nonnull BlockEntityType<?> type, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(type, pos, state);

    }

    @Nonnull
    public abstract UUID GetShooterID();
    public abstract int GetNumValidContexts();
    @Nonnull
    public abstract UUID[] GetAllGunIDs();
    @Nonnull
    public abstract UUID GetGunIDForSlot(int gunSlotIndex);
    @Nonnull
    public abstract GunContext CreateContext(@Nonnull UUID gunID);
    @Nonnull
    public abstract Transform GetShootOrigin(float deltaTick); // delta will be 0 on server
    @Nonnull
    public abstract ActionStack GetActionStack(int gunSlotIndex);

    // Returning an owner of this BlockEntity is optional.
    @Nullable
    public Entity Owner() { return null; }
    @Nonnull
    public UUID OwnerUUID() { return GetShooterID(); }
}
