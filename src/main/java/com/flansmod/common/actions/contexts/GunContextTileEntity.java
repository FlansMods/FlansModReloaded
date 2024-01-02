package com.flansmod.common.actions.contexts;

import com.flansmod.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class GunContextTileEntity extends GunContextInventoryItem
{
	@Nonnull
	public final BlockEntity TileEntity;

	public GunContextTileEntity(
		@Nonnull BlockEntity tileEntity,
		@Nonnull Container container,
		int slot)
	{
		super(container, slot);
		TileEntity = tileEntity;
	}

	@Override
	@Nullable
	public Level GetLevel() { return TileEntity.getLevel(); }
	@Override
	@Nullable
	public Transform GetPosition() { return Transform.FromBlockPos("\"BlockPos\"", TileEntity.getBlockPos()); }
}
