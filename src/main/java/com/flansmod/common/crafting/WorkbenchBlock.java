package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class WorkbenchBlock extends BaseEntityBlock
{
	protected static final VoxelShape DEFAULT_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
	public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
	private final ResourceLocation definitionLocation;
	public WorkbenchDefinition Def() { return FlansMod.WORKBENCHES.Get(definitionLocation); }

	public WorkbenchBlock(@Nonnull ResourceLocation defLoc, @Nonnull Properties props)
	{
		super(props);

		definitionLocation = defLoc;

		registerDefaultState(stateDefinition.any()
			.setValue(DIRECTION, Direction.NORTH));
	}

	@Override
	@Nonnull
	public RenderShape getRenderShape(@Nonnull BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter getter, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
	{
		return DEFAULT_SHAPE;
	}

	@Override
	public MenuProvider getMenuProvider(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos)
	{
		return new SimpleMenuProvider(
			(containerID, inventory, playerEntity) ->
			{
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity instanceof WorkbenchBlockEntity workbenchBlockEntity)
					return workbenchBlockEntity.createMenu(containerID, inventory, playerEntity);
				return null;
			},
			Component.translatable("menu.title.flansmod.gun_modification_menu"));
	}

	@Override
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(!level.isClientSide && blockEntity != null && player instanceof ServerPlayer serverPlayer)
		{
			NetworkHooks.openScreen(serverPlayer, getMenuProvider(state, level, pos), pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		BlockEntityType<?> type = ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(definitionLocation);
		if(type != null)
			return type.create(pos, state);
		return null;
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

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type)
	{
		return level.isClientSide ? null : (level1, pos1, state1, blockEntity) ->
		{
			if(blockEntity instanceof WorkbenchBlockEntity workbenchBlockEntity)
				WorkbenchBlockEntity.serverTick(level1, pos1, state1, workbenchBlockEntity);
		};
	}

	@Override
	public void onRemove(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean flag)
	{
		if (!blockState.is(newState.getBlock()))
		{
			BlockEntity blockentity = level.getBlockEntity(pos);
			if (blockentity instanceof WorkbenchBlockEntity workbenchBlockEntity)
			{
				if (level instanceof ServerLevel)
				{
					Containers.dropContents(level, pos, workbenchBlockEntity);
				}

				level.updateNeighbourForOutputSignal(pos, this);
			}
			super.onRemove(blockState, level, pos, newState, flag);
		}
	}
}
