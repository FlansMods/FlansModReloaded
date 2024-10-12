package com.flansmod.common.blocks;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.blocks.TurretBlockDefinition;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class TurretBlockEntity extends ShooterBlockEntity implements WorldlyContainer, MenuProvider, Clearable, ICapabilityProvider
{
    @Nonnull
    public final TurretBlockDefinition Def;
    @Nonnull
    public ItemStack GunStack;
    @Nonnull
    public ActionStack Actions;
    @Nonnull
    public Map<EPlayerInput, Integer> InputHeldTicks = new HashMap<>();
    @Nonnull
    public final ItemStack[] BulletStacks;

    public TurretBlockEntity(@Nonnull ResourceLocation defLoc, @Nonnull BlockPos pos, @Nonnull BlockState state)
    {
        super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(defLoc), pos, state);
        Def = FlansMod.TURRETS.Get(defLoc);
        BulletStacks = new ItemStack[Def.ammoSlots.slots.length];
        Arrays.fill(BulletStacks, ItemStack.EMPTY);
        GunStack = ItemStack.EMPTY;
        Actions = ActionStack.Invalid;
    }

    @Override @Nonnull
    public UUID GetShooterID()
    {
        return ShooterContextBlockEntity.GenerateBlockEntityShooterID(getLevel().dimension(), getBlockPos());
    }
    @Override
    public int GetNumValidContexts() { return 1; }
    @Override @Nonnull
    public UUID[] GetAllGunIDs() { return new UUID[] { GetGunID() }; }
    @Override @Nonnull
    public UUID GetGunIDForSlot(int gunSlotIndex) { return gunSlotIndex == 0 ? GetGunID() : GunContext.INVALID.GetUUID(); }
    @Override @Nonnull
    public GunContext CreateContext(@Nonnull UUID gunID)
    {
        ShooterContext shooterContext = GetShooterContext();
        if(shooterContext.IsValid() && shooterContext instanceof ShooterContextBlockEntity shooterContextBlockEntity)
            return new GunContextTileEntity(shooterContextBlockEntity, 0);
        return GunContext.INVALID;
    }
    @Override @Nonnull
    public Transform GetShootOrigin(float deltaTick)
    {
        return Transform.fromPosAndQuat(getBlockPos().getCenter(), getBlockState().getValue(TurretBlock.FACING).getRotation().rotateX(Maths.TauF / 4f), ()->"");
    }
    @Nonnull
    public UUID GetGunID() { return GunItem.GetGunID(GunStack); }
    @Nonnull
    public GunContext GetGunContext() { return CreateContext(GetGunID()); }
    @Override @Nonnull
    public ActionStack GetActionStack(int gunSlotIndex)
    {
        if(!Actions.IsValid())
            OnGunStackChange();

        return Actions;
    }

    public static class TurretBlockEntityTypeHolder implements BlockEntityType.BlockEntitySupplier<TurretBlockEntity>
    {
        private final ResourceLocation DefLoc;

        public TurretBlockEntityTypeHolder(ResourceLocation defLoc)
        {
            DefLoc = defLoc;
        }

        @Override
        @Nonnull
        public TurretBlockEntity create(@Nonnull BlockPos pos, @Nonnull BlockState state)
        {
            return new TurretBlockEntity(DefLoc, pos, state);
        }

        public BlockEntityType<TurretBlockEntity> CreateType()
        {
            return new BlockEntityType<>(this, Set.of(), null)
            {
                @Override
                public boolean isValid(@Nonnull BlockState state)
                {
                    return state.getBlock() instanceof TurretBlock;
                }
            };
        }
    }


    // As a container
    @Override
    @Nonnull
    public int[] getSlotsForFace(@Nonnull Direction direction) { return new int[0]; }
    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, @Nonnull ItemStack stack, @Nullable Direction direction)
    {
        return false;
    }
    @Override
    public boolean canTakeItemThroughFace(int p_19239_, @Nonnull ItemStack stack, @Nonnull Direction direction)
    {
        return false;
    }
    @Override
    public int getContainerSize() { return BulletStacks.length + 1; }
    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : BulletStacks)
            if(!stack.isEmpty())
                return false;
        return GunStack.isEmpty();
    }
    @Override
    public boolean stillValid(@Nonnull Player player) { return true; }
    @Override @Nonnull
    public ItemStack getItem(int slot)
    {
        if(slot == 0)
            return GunStack;
        else if(0 <= slot - 1 && slot - 1 < BulletStacks.length)
            return BulletStacks[slot - 1];
        return ItemStack.EMPTY;
    }
    @Override @Nonnull
    public ItemStack removeItem(int slot, int count)
    {
        if(slot == 0)
        {
            ItemStack ret = GunStack.split(count);
            OnGunStackChange();
            return ret;
        }
        else if(0 <= slot - 1 && slot - 1 < BulletStacks.length)
        {
            return BulletStacks[slot - 1].split(count);
        }
        return ItemStack.EMPTY;
    }
    @Override @Nonnull
    public ItemStack removeItemNoUpdate(int slot)
    {
        if(slot == 0)
        {
            ItemStack ret = GunStack;
            GunStack = ItemStack.EMPTY;
            OnGunStackChange();
            return ret;
        }
        else if(0 <= slot - 1 && slot - 1 < BulletStacks.length)
        {
            ItemStack ret = BulletStacks[slot - 1];
            BulletStacks[slot - 1] = ItemStack.EMPTY;
            return ret;
        }
        return ItemStack.EMPTY;
    }
    @Override
    public void setItem(int slot, @Nonnull ItemStack stack)
    {
        if(slot == 0)
        {
            GunStack = stack;
            OnGunStackChange();
        }
        else if(0 <= slot - 1 && slot - 1 < BulletStacks.length)
        {
            BulletStacks[slot - 1] = stack;
        }
    }
    @Override
    public void clearContent()
    {
        GunStack = ItemStack.EMPTY;
        Arrays.fill(BulletStacks, ItemStack.EMPTY);
        OnGunStackChange();
    }

    @Nonnull
    public ItemStack SwapGun(@Nonnull ItemStack gunStack)
    {
        ItemStack ret = GunStack;
        GunStack = gunStack;
        OnGunStackChange();
        return ret;
    }
    @Nonnull
    public ItemStack QuickStackBullets(@Nonnull ItemStack bulletStack)
    {
        for(int i = 0; i < BulletStacks.length; i++)
        {
            if(BulletStacks[i].isEmpty()) {
                BulletStacks[i] = bulletStack;
                bulletStack = ItemStack.EMPTY;
            }
            else if(ItemStack.isSameItemSameTags(bulletStack, BulletStacks[i]))
            {
                int count = Maths.Min(bulletStack.getCount(), getMaxStackSize() - BulletStacks[i].getCount());
                BulletStacks[i].setCount(BulletStacks[i].getCount() + count);
                bulletStack.setCount(bulletStack.getCount() - count);
            }

            if(bulletStack.isEmpty())
                break;
        }
        return bulletStack;
    }
    @Nonnull
    public ItemStack TakeFirstStack()
    {
        if(!GunStack.isEmpty())
            return removeItem(0, 1);

        for(int i = 0; i < BulletStacks.length; i++)
            if(!BulletStacks[i].isEmpty())
                return removeItem(i+1, BulletStacks[i].getCount());

        return ItemStack.EMPTY;
    }
    private void OnGunStackChange()
    {
        Actions = new ActionStack(level.isClientSide);
        setChanged();
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Nonnull
    public ShooterContext GetShooterContext() { return ShooterContext.of(this); }
    //@Nonnull
    //public GunContext GetGunContext()
    //{
    //    return GunContext.of(this, this, 0);
    //}

    public void SetInputs(@Nonnull List<EPlayerInput> activeInputs)
    {
        for(EPlayerInput input : activeInputs)
        {
            if(!InputHeldTicks.containsKey(input))
            {
                InputHeldTicks.put(input, 1);
                SimulatePress(input);
            }
            else
            {
                InputHeldTicks.put(input, InputHeldTicks.get(input) + 1);
                SimulateHold(input);
            }
        }

        List<EPlayerInput> deactivations = new ArrayList<>(activeInputs.size());
        for(EPlayerInput input : InputHeldTicks.keySet())
            if(!activeInputs.contains(input))
                deactivations.add(input);

        for(EPlayerInput deactivated : deactivations)
        {
            InputHeldTicks.remove(deactivated);
            SimulateRelease(deactivated);
        }

    }
    private void SimulatePress(@Nonnull EPlayerInput inputType)
    {
        FlansMod.ACTIONS_SERVER.KeyPressed(GetShooterContext(), inputType);
    }
    private void SimulateHold(@Nonnull EPlayerInput inputType)
    {

    }
    private void SimulateRelease(@Nonnull EPlayerInput inputType)
    {

    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tags)
    {
        super.saveAdditional(tags);

        tags.put("gun", GunStack.save(new CompoundTag()));
        for(int i = 0; i < BulletStacks.length; i++)
            tags.put("bullet_" + i, BulletStacks[i].save(new CompoundTag()));
    }
    protected void saveForClientUpdate(@Nonnull CompoundTag tags)
    {
        super.saveAdditional(tags);
        // Just send the gun, bullets are held privately
        tags.put("gun", GunStack.save(new CompoundTag()));
    }
    @Override
    public void load(@Nonnull CompoundTag tags)
    {
        super.load(tags);

        if(tags.contains("gun"))
            GunStack = ItemStack.of(tags.getCompound("gun"));
        for(int i = 0; i < BulletStacks.length; i++)
            if(tags.contains("bullet_" + i))
                BulletStacks[i] = ItemStack.of(tags.getCompound("bullet_" + i));
        //OnGunStackChange();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Nonnull
    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tags = new CompoundTag();
        saveForClientUpdate(tags);
        return tags;
    }

    private static final Component DISPLAY_NAME = Component.translatable("turret.title");
    @Override
    @Nonnull
    public Component getDisplayName() { return DISPLAY_NAME; }
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, @Nonnull Inventory inventory, @Nonnull Player player)
    {
        return new TurretContainerMenu(containerID, inventory, this);
    }
    protected void ServerTick()
    {
        if(!Actions.IsValid())
            OnGunStackChange();

        Actions.OnTick(level, GetGunContext());
    }
    protected void ClientTick()
    {
        if(!Actions.IsValid())
            OnGunStackChange();

        Actions.OnTick(level, GetGunContext());
    }
    public static void ServerTurretTicker(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull TurretBlockEntity turret)
    {
        turret.ServerTick();
    }
    public static void ClientTurretTicker(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull TurretBlockEntity turret)
    {
        turret.ClientTick();
    }
}

