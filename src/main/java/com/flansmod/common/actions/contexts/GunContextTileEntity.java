package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.util.Transform;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class GunContextTileEntity extends GunContextInventoryItem
{
	@Nonnull
	public final ShooterContextBlockEntity ShooterContext;

	public GunContextTileEntity(
		@Nonnull ShooterContextBlockEntity parent,
		int slot)
	{
		super(parent.GetAttachedInventory(), slot);
		ShooterContext = parent;
	}

	@Override
	@Nullable
	public DamageSource CreateDamageSource()
	{
		return ShooterContext.Level().damageSources().generic();
	}
	@Override
	@Nonnull
	public ActionStack GetActionStack()
	{
		Optional<ShooterBlockEntity> blockEntity = ShooterContext.GetBlockEntity();
        return blockEntity.map(shooterBlockEntity -> shooterBlockEntity.GetActionStack(GetInventorySlotIndex())).orElse(ActionStack.Invalid);
    }
	@Override
	@Nonnull
	public ShooterContext GetShooter() { return ShooterContext; }
	@Override
	@Nullable
	public Level GetLevel() { return ShooterContext.Level(); }
	@Override
	@Nullable
	public Transform GetPosition() { return Transform.FromBlockPos(ShooterContext.Pos, () -> "\"BlockPos\""); }
}
