package com.flansmod.airtraffic.common.runway;

import com.flansmod.airtraffic.api.Rotation8;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RunwayBlock extends BaseEntityBlock
{
	public static final IntegerProperty ROTATION =
		IntegerProperty.create("rotation", 0, Rotation8.count());
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	public RunwayBlock(@Nonnull Properties props)
	{
		super(props);
		registerDefaultState(stateDefinition.any()
			.setValue(ROTATION, 0)
			.setValue(ENABLED, false));
	}

	@Override @Nonnull
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		return defaultBlockState()
			.setValue(ROTATION, context.getPlayer() != null
				? Rotation8.fromAngle(context.getPlayer().getYRot()).ordinal()
				: 0);
	}

	@Override @Nonnull
	public BlockState rotate(@Nonnull BlockState state, @Nonnull Rotation rotation)
	{
		return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 8));
	}
	@Override @Nonnull
	public BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirrorAxis)
	{
		return state.setValue(ROTATION, mirrorAxis.mirror(state.getValue(ROTATION), 8));
	}
	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(ROTATION);
		builder.add(ENABLED);
	}
	@Override @Nullable
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return new RunwayBlockEntity(pos, state);
	}
}
