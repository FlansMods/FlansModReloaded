package com.flansmod.common.blocks;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.blocks.TurretBlockDefinition;
import com.flansmod.common.types.blocks.elements.RedstoneResponseDefinition;
import com.flansmod.common.types.blocks.elements.TurretSideDefinition;
import com.flansmod.common.types.elements.EPlayerInput;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TurretBlock extends BaseEntityBlock
{
    protected static final VoxelShape DEFAULT_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty TRIGGERED  = BlockStateProperties.TRIGGERED;
    private static final int TRIGGER_DURATION = 4;
    @Nonnull
    public final LazyDefinition<TurretBlockDefinition> DefRef;
    @Nonnull
    public TurretBlockDefinition Def() { return DefRef.DefGetter().get(); }

    public TurretBlock(@Nonnull ResourceLocation defLoc, @Nonnull Properties props)
    {
        super(props);

        DefRef = LazyDefinition.of(defLoc, FlansMod.TURRETS);

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }



    // Redstone input
    public void neighborChanged(@Nonnull BlockState blockState,
                                @Nonnull Level level,
                                @Nonnull BlockPos thisBlockPos,
                                @Nonnull Block srcBlock,
                                @Nonnull BlockPos srcBlockPos,
                                boolean p_52705_)
    {
        TurretBlockDefinition def = Def();

        // Check sided inputs
        List<EPlayerInput> activeInputs = new ArrayList<>();
        int indirectInput = level.getBestNeighborSignal(thisBlockPos.above());
        for(Direction dir : Direction.values())
        {
            TurretSideDefinition sideDef = def.GetSideDefinition(dir);
            for(RedstoneResponseDefinition response : sideDef.redstoneResponses)
            {
                int redstoneInput = level.getSignal(thisBlockPos.relative(dir), dir);
                if(response.minRedstoneLevel <= redstoneInput && redstoneInput <= response.maxRedstoneLevel)
                    activeInputs.add(response.simulateInput);
                else if(response.allowIndirectPower && response.minRedstoneLevel <= indirectInput && indirectInput <= response.maxRedstoneLevel)
                    activeInputs.add(response.simulateInput);
            }
        }

        boolean shouldBeActive = !activeInputs.isEmpty();
        boolean isActive = blockState.getValue(TRIGGERED);
        if (shouldBeActive && !isActive)
        {
            level.scheduleTick(thisBlockPos, this, 4);
            level.setBlock(thisBlockPos, blockState.setValue(TRIGGERED, true), 4);
        }
        else if (!shouldBeActive && isActive)
        {
            level.setBlock(thisBlockPos, blockState.setValue(TRIGGERED, false), 4);
        }

        if(level.getBlockEntity(thisBlockPos) instanceof TurretBlockEntity turretBlockEntity)
        {
            turretBlockEntity.SetInputs(activeInputs);
        }
    }

    @Override
    public MenuProvider getMenuProvider(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos)
    {
        return new SimpleMenuProvider(
                (containerID, inventory, playerEntity) ->
                {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if(blockEntity instanceof TurretBlockEntity turretBlockEntity)
                        return turretBlockEntity.createMenu(containerID, inventory, playerEntity);
                    return null;
                },
                Component.translatable("menu.title.flansmod.turret_menu"));
    }
    @Override
    @Nonnull
    public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(!level.isClientSide && blockEntity instanceof TurretBlockEntity turretBlockEntity && player instanceof ServerPlayer serverPlayer)
        {
            //ItemStack withItem = player.getItemInHand(hand);
            //if(withItem.isEmpty() && player.isCrouching() && !turretBlockEntity.isEmpty())
            //{
            //    player.setItemInHand(hand, turretBlockEntity.TakeFirstStack());
            //}
            //else if(withItem.getItem() instanceof GunItem)
            //{
            //    player.setItemInHand(hand, turretBlockEntity.SwapGun(withItem));
            //}
            //else if(withItem.getItem() instanceof BulletItem)
            //{
            //    player.setItemInHand(hand, turretBlockEntity.QuickStackBullets(withItem));
            //}
            //else
            //{
                NetworkHooks.openScreen(serverPlayer, getMenuProvider(state, level, pos), pos);
            //}
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
    {
        BlockEntityType<?> type = ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(DefRef.Loc());
        if(type != null)
            return type.create(pos, state);
        return null;
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type)
    {
        return (level1, pos1, state1, blockEntity) ->
        {
            if(blockEntity instanceof TurretBlockEntity turretBlockEntity)
            {
                if (level1.isClientSide)
                    TurretBlockEntity.ClientTurretTicker(level1, pos1, state1, turretBlockEntity);
                else
                    TurretBlockEntity.ServerTurretTicker(level1, pos1, state1, turretBlockEntity);
            }
        };
    }
    @Override @Nonnull
    public BlockState rotate(@Nonnull BlockState src, @Nonnull Rotation rotation)
    {
        return src.setValue(FACING, rotation.rotate(src.getValue(FACING)));
    }
    @Override @Nonnull
    public BlockState mirror(@Nonnull BlockState src, @Nonnull Mirror mirror)
    {
        return src.rotate(mirror.getRotation(src.getValue(FACING)));
    }
    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, TRIGGERED);
    }
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
    {
        return stateDefinition.any()
                .setValue(TRIGGERED, false)
                .setValue(FACING, context.getNearestLookingDirection());
    }
    @Override @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }
    @Override @Nonnull
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter getter, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
    {
        return DEFAULT_SHAPE;
    }
}
