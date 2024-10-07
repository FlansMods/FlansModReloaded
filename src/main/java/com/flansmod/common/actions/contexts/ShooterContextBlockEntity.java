package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ShooterContextBlockEntity extends ShooterContext
{
    @Nonnull
    public EContextSide Side;
    @Nonnull
    public ResourceKey<Level> Dimension;
    @Nonnull
    public BlockPos Pos;
    @Nonnull
    private final UUID ShooterID;

    @Override @Nonnull
    public UUID ShooterID() { return ShooterID; }

    @Nonnull
    public static UUID GenerateBlockEntityShooterID(@Nonnull ResourceKey<Level> dimension, @Nonnull BlockPos pos)
    {
        long a = (long)dimension.location().hashCode() << 32 | (long)pos.getX();
        long b = ((long)pos.getY() << 32) | (long)pos.getZ();
        return new UUID(a, b);
    }
    @Nonnull
    public static Pair<Integer, BlockPos> ConvertShooterIDToCoords(@Nonnull UUID shooterID)
    {
        long a = shooterID.getMostSignificantBits();
        int dimHash = (int)(a >> 32);
        int x = (int)a;
        long b = shooterID.getLeastSignificantBits();
        int y = (int)(b >> 32);
        int z = (int)b;
        return Pair.of(dimHash, new BlockPos(x,y,z));
    }

    public ShooterContextBlockEntity(@Nonnull ResourceKey<Level> dimension, @Nonnull EContextSide side, @Nonnull BlockPos pos)
    {
        Dimension = dimension;
        Side = side;
        Pos = pos;
        ShooterID = GenerateBlockEntityShooterID(dimension, pos);
    }
    public ShooterContextBlockEntity(@Nonnull Level level, @Nonnull BlockPos pos)
    {
       this(level.dimension(), level.isClientSide ? EContextSide.Client : EContextSide.Server, pos);
    }
    public ShooterContextBlockEntity(@Nonnull ShooterBlockEntity blockEntity)
    {
        this(blockEntity.getLevel().dimension(), blockEntity.getLevel().isClientSide ? EContextSide.Client : EContextSide.Server, blockEntity.getBlockPos());
    }







    @Override @Nonnull
    public EContextSide GetSide() { return Side; }
    @Override @Nonnull
    public ResourceKey<Level> Dimension() { return Dimension; }
    @Override @Nullable
    public Level Level()
    {
        return MinecraftHelpers.GetLevel(Dimension);
    }

    @Nonnull
    public Optional<ShooterBlockEntity> GetBlockEntity() { return GetBlockEntity(false); }
    @Nonnull
    public Optional<ShooterBlockEntity> GetBlockEntity(boolean canLoadChunks)
    {
        Level level = Level();
        if(level == null)
            return Optional.empty();

        if(!canLoadChunks && !level.isLoaded(Pos))
            return Optional.empty();

        BlockEntity blockEntity = level.getBlockEntity(Pos);
        if(blockEntity instanceof ShooterBlockEntity shooterBlockEntity && !shooterBlockEntity.isRemoved())
            return Optional.of(shooterBlockEntity);

        return Optional.empty();
    }

    @Override
    public int GetNumValidContexts() { return GetBlockEntity().map(ShooterBlockEntity::GetNumValidContexts).orElse(0); }
    @Override @Nonnull
    public UUID[] GetAllGunIDs() { return GetBlockEntity().map(ShooterBlockEntity::GetAllGunIDs).orElse(new UUID[0]); }
    @Override @Nonnull
    public UUID GetGunIDForSlot(int gunSlotIndex) { return GetBlockEntity().map(block -> block.GetGunIDForSlot(gunSlotIndex)).orElse(GunContext.INVALID.GetUUID()); }
    @Override @Nonnull
    public GunContext CreateContext(@Nonnull UUID gunID) { return GetBlockEntity().map(block -> block.CreateContext(gunID)).orElse(GunContext.INVALID); }
    @Override @Nullable
    public Entity Entity() { return null; }
    @Override @Nullable
    public Entity Owner() { return GetBlockEntity().map(ShooterBlockEntity::Owner).orElse(null); }
    @Override @Nullable
    public Container GetAttachedInventory()
    {
        return GetBlockEntity().map(blockEntity ->
        {
            if(blockEntity instanceof WorldlyContainer container)
                return container;
            return null;
        }).orElse(null);
    }
    @Override @Nonnull
    public Transform GetShootOrigin(float deltaTick) { return GetBlockEntity().map(block -> block.GetShootOrigin(deltaTick)).orElse(Transform.IDENTITY); }
    @Override
    public boolean IsValid() { return GetBlockEntity().isPresent(); }
    @Override
    public boolean IsCreative() { return false; }
    @Override
    public void BakeModifiers(@Nonnull IModifierBaker baker)
    {
        // TODO:
    }

    @Override @Nonnull
    public UUID OwnerUUID() { return GetBlockEntity().map(ShooterBlockEntity::OwnerUUID).orElse(ShooterContext.InvalidID); }
    @Override
    public String toString()
    {
        return "ShooterContext (BlockEntity@" + Dimension + ":" + Pos + ")";
    }

}
