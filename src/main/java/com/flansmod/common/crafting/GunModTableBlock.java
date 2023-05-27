package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class GunModTableBlock extends Block implements EntityBlock
{
	public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

	public GunModTableBlock(Properties props)
	{
		super(props);

		registerDefaultState(stateDefinition.any()
			.setValue(DIRECTION, Direction.NORTH));
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
	{
		return new SimpleMenuProvider(
			(containerID, inventory, playerEntity) ->
			{
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity instanceof GunModBlockEntity gunModBlock)
					return gunModBlock.createMenu(containerID, inventory, playerEntity);
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
		return FlansMod.GUN_MOD_TILE_ENTITY.get().create(pos, state);
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
}
