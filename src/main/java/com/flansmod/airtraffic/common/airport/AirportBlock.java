package com.flansmod.airtraffic.common.airport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class AirportBlock extends BaseEntityBlock
{
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	public AirportBlock(@Nonnull Properties props)
	{
		super(props);
		registerDefaultState(stateDefinition.any()
			.setValue(ENABLED, false));
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(ENABLED);
	}

	@Override @Nullable
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return new AirportBlockEntity(pos, state);
	}
}
