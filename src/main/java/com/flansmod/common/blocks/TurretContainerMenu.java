package com.flansmod.common.blocks;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TurretContainerMenu extends AbstractContainerMenu
{
    public final Container TurretContainer;
    private Slot[] InventorySlots;
    private RestrictedSlot GunSlot;
    private RestrictedSlot[] AmmoSlots;

    public TurretContainerMenu(int id, @Nonnull Inventory inventory, @Nonnull TurretBlockEntity blockEntity)
    {
        super(FlansMod.TURRET_MENU.get(), id);
        TurretContainer = blockEntity;
        CreateSlots(inventory);
    }

    public TurretContainerMenu(int containerID,
                               @Nonnull Inventory inventory,
                               @Nonnull FriendlyByteBuf data)
    {
        super(FlansMod.TURRET_MENU.get(), containerID);
        BlockPos blockPos = data.readBlockPos();
        if(inventory.player.level().getBlockEntity(blockPos) instanceof TurretBlockEntity blockEntity)
        {
            TurretContainer = blockEntity;
        }
        else
        {
            TurretContainer = null;
        }

        CreateSlots(inventory);
    }

    protected void CreateSlots(@Nonnull Inventory playerInventory)
    {
        //addDataSlots(WorkbenchData);

        InventorySlots = new Slot[4 * 9];
        for(int y = 0; y < 3; ++y)
        {
            for(int x = 0; x < 9; ++x)
            {
                addSlot(InventorySlots[x + 9 * y + 9] = new RestrictedSlot(playerInventory, x + y * 9 + 9, 6 + x * 18, 137 + y * 18));
            }
        }

        for(int x = 0; x < 9; ++x)
        {
            addSlot(InventorySlots[x] = new RestrictedSlot(playerInventory, x, 6 + x * 18, 195));
        }

        if(TurretContainer != null)
        {
            addSlot(GunSlot = new RestrictedSlot(TurretContainer, 0, 6, 24));
            AmmoSlots = new RestrictedSlot[TurretContainer.getContainerSize() - 1];
            for(int i = 0; i < TurretContainer.getContainerSize() - 1; i++)
            {
                int xIndex = i % 9;
                int yIndex = i / 9;
                addSlot(AmmoSlots[i] = new RestrictedSlot(TurretContainer, i + 1, 6 + xIndex*18, 44+yIndex*18));
            }
        }
    }

    @Override @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int i)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) { return true; }
}
