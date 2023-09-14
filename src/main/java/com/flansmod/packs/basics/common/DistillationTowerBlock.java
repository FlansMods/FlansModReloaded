package com.flansmod.packs.basics.common;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.packs.basics.BasicPartsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class DistillationTowerBlock extends BaseEntityBlock
{
	protected static final VoxelShape DEFAULT_SHAPE = Block.box(1.0D, 1.0D, 1.0D, 15.0D, 15.0D, 15.0D);
	public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

	public final boolean IsTop;

	public DistillationTowerBlock(boolean isTop, Properties props)
	{
		super(props);
		IsTop = isTop;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
	{
		return DEFAULT_SHAPE;
	}
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
	{
		return new SimpleMenuProvider(
			(containerID, inventory, playerEntity) ->
			{
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity instanceof DistillationTowerBlockEntity distillationTowerBlockEntity)
					return distillationTowerBlockEntity.createMenu(containerID, inventory, playerEntity);
				return null;
			},
			Component.translatable("menu.title.flansmod.distillation_tower_menu"));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(IsTop)
			return BasicPartsMod.DISTILLATION_TOWER_TOP_TILE_ENTITY.get().create(pos, state);
		else
			return BasicPartsMod.DISTILLATION_TOWER_TILE_ENTITY.get().create(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(DIRECTION);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return stateDefinition.any().setValue(DIRECTION,
			context.getHorizontalDirection().getOpposite());
	}

	@Override
	@Nonnull
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if(player.getItemInHand(hand).getItem() instanceof BlockItem blockItem)
		{
			if(blockItem.getBlock() instanceof DistillationTowerBlock)
			{
				return InteractionResult.PASS;
			}
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(!level.isClientSide && blockEntity != null && player instanceof ServerPlayer serverPlayer)
		{
			NetworkHooks.openScreen(serverPlayer, getMenuProvider(state, level, pos), pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	public int GetTowerHeight(Level level, BlockPos pos, BlockState state)
	{
		int blocksAbove = 0;
		int blocksBelow = 0;
		boolean valid = true;
		while(blocksAbove < 128)
		{
			BlockState testState = level.getBlockState(pos.above(blocksAbove));
			if(testState.is(BasicPartsMod.DISTILLATION_TOWER_TOP.get()))
				break;
			else if(!testState.is(BasicPartsMod.DISTILLATION_TOWER.get()))
			{
				valid = false;
				break;
			}

			blocksAbove++;
		}
		while(blocksBelow > -128)
		{
			BlockState testState = level.getBlockState(pos.above(blocksBelow));
			if(!testState.is(BasicPartsMod.DISTILLATION_TOWER.get()))
				break;

			blocksBelow--;
		}
		return valid ? (blocksAbove - blocksBelow + 1) : 0;
	}

	public BlockPos GetTopBlock(Level level, BlockPos pos, BlockState state)
	{
		if(state.is(BasicPartsMod.DISTILLATION_TOWER_TOP.get()))
		{
			return pos;
		}
		if(state.is(BasicPartsMod.DISTILLATION_TOWER.get()))
		{
			return GetTopBlock(level, pos.above(), level.getBlockState(pos.above()));
		}
		// We hit non-distillation, go back one.
		return pos.below();
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return level.isClientSide ? null : new BlockEntityTicker<T>()
		{
			@Override
			public void tick(Level level1, BlockPos pos1, BlockState state1, T blockEntity)
			{
				if(blockEntity instanceof DistillationTowerBlockEntity distillationTowerBlockEntity)
					DistillationTowerBlockEntity.serverTick(level1, pos1, state1, distillationTowerBlockEntity);
			}
		};
	}


	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(Level level, BlockEntityType<T> type, BlockEntityType<? extends DistillationTowerBlockEntity> workbenchType)
	{
		return level.isClientSide ? null : createTickerHelper(type, workbenchType, DistillationTowerBlockEntity::serverTick);
	}

	@Override
	public void animateTick(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource rand)
	{
		double x = (double)pos.getX() + 0.5D;
		double y = (double)pos.getY();
		double z = (double)pos.getZ() + 0.5D;

		if(IsTop)
		{
			DistillationTowerBlockEntity tileEntity = level.getBlockEntity(pos, BasicPartsMod.DISTILLATION_TOWER_TOP_TILE_ENTITY.get()).orElse(null);
			if (tileEntity != null && tileEntity.IsLit())
			{
				if (rand.nextDouble() < 0.1D)
				{
					level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				}
				Direction direction = state.getValue(DIRECTION);
				Direction.Axis direction$axis = direction.getAxis();
				final double offsetFromCenter = -0.48D;
				double leftRightRng = rand.nextDouble() * 0.3D - 0.15D;
				double xOffset = direction$axis == Direction.Axis.X ? (double) direction.getStepX() * offsetFromCenter : leftRightRng;
				double yOffset = (rand.nextDouble() * 3.0D + 3.0D) / 16.0D;
				double zOffset = direction$axis == Direction.Axis.Z ? (double) direction.getStepZ() * offsetFromCenter : leftRightRng;
				level.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
				level.addParticle(ParticleTypes.FLAME, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);

			}
		}
		else
		{
			DistillationTowerBlockEntity tileEntity = level.getBlockEntity(pos, BasicPartsMod.DISTILLATION_TOWER_TILE_ENTITY.get()).orElse(null);
			if(tileEntity != null)
			{
				DistillationTowerBlockEntity topTileEntity = tileEntity.GetTopDistillationTileEntity();
				if(topTileEntity != null && topTileEntity.IsDistillationInProgress())
				{
					if (rand.nextDouble() < 0.1D)
					{
						level.playLocalSound(x, y, z, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 1.0F, 1.0F, false);
					}

					Direction direction = state.getValue(DIRECTION);
					Direction.Axis direction$axis = direction.getAxis();
					final double offsetFromCenter = -0.56D;

					double leftRightRng = rand.nextDouble() * 0.3D - 0.15D;
					double xOffset = direction$axis == Direction.Axis.X ? (double) direction.getStepX() * offsetFromCenter : leftRightRng;
					double yOffset = (rand.nextDouble() * 2.0D + 4.0D) / 16.0D;
					double zOffset = direction$axis == Direction.Axis.Z ? (double) direction.getStepZ() * offsetFromCenter : leftRightRng;

					level.addParticle(ParticleTypes.BUBBLE_POP, x - xOffset, y + yOffset, z - zOffset, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}
}
