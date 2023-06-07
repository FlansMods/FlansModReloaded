package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class WorkbenchBlock extends BaseEntityBlock
{
	public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
	private ResourceLocation definitionLocation;
	public WorkbenchDefinition Def() { return FlansMod.WORKBENCHES.get(definitionLocation); }

	public WorkbenchBlock(ResourceLocation defLoc, Properties props)
	{
		super(props);

		definitionLocation = defLoc;

		registerDefaultState(stateDefinition.any()
			.setValue(DIRECTION, Direction.NORTH));
	}

	public RenderShape getRenderShape(BlockState p_49232_) {
		return RenderShape.MODEL;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
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
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(definitionLocation).create(pos, state);
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
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return level.isClientSide ? null : new BlockEntityTicker<T>()
		{
			@Override
			public void tick(Level level1, BlockPos pos1, BlockState state1, T blockEntity)
			{
				if(blockEntity instanceof WorkbenchBlockEntity workbenchBlockEntity)
					WorkbenchBlockEntity.serverTick(level1, pos1, state1, workbenchBlockEntity);
			}
		};
	}


	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(Level level, BlockEntityType<T> type, BlockEntityType<? extends WorkbenchBlockEntity> workbenchType)
	{
		return level.isClientSide ? null : createTickerHelper(type, workbenchType, WorkbenchBlockEntity::serverTick);
	}
}
