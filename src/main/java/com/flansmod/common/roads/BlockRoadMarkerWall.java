package com.flansmod.common.roads;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockRoadMarkerWall extends BlockRoadMarker
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty ACTIVE = RedstoneTorchBlock.LIT;


	public BlockRoadMarkerWall(@Nonnull Properties props)
	{
		super(props);
		registerDefaultState(stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(ACTIVE, Boolean.TRUE)
			);

	}

	@Override @Nonnull
	public String getDescriptionId() {
		return this.asItem().getDescriptionId();
	}
	@Override @Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter getter, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return WallTorchBlock.getShape(state);
	}
	@Override
	public boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos) {
		return Blocks.WALL_TORCH.canSurvive(state, level, pos);
	}
	@Override @Nonnull
	public BlockState updateShape(@Nonnull BlockState oldState, @Nonnull Direction dir, @Nonnull BlockState newState,
								  @Nonnull LevelAccessor level, @Nonnull BlockPos oldPos, @Nonnull BlockPos newPos) {
		return Blocks.WALL_TORCH.updateShape(oldState, dir, newState, level, oldPos, newPos);
	}
	@Override @Nullable
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
		BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
		return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
	}
	@Override
	public void animateTick(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource rand) {
		if (state.getValue(ACTIVE)) {
			Direction direction = state.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double)pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepX();
			double d2 = (double)pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double)pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepZ();
			level.addParticle(this.flameParticle, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
	}
	@Override @Nonnull
	public BlockState rotate(@Nonnull BlockState state, @Nonnull Rotation rot) {
		return Blocks.WALL_TORCH.rotate(state, rot);
	}
	@Override @Nonnull
	public BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirrorAxis) {
		return Blocks.WALL_TORCH.mirror(state, mirrorAxis);
	}
	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE);
	}


}
