package com.flansmod.common.blocks;

import com.flansmod.common.FlansMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TurretContainerMenu extends AbstractContainerMenu
{
    public TurretContainerMenu(int id, @Nonnull Inventory playerInventory, @Nonnull TurretBlockEntity blockEntity)
    {
        super(FlansMod.TURRET_MENU.get(), id);
    }

    public TurretContainerMenu(int containerID,
                               @Nonnull Inventory inventory,
                               @Nonnull FriendlyByteBuf data)
    {
        super(FlansMod.TURRET_MENU.get(), containerID);

    }

    @Override @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int i)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) { return true; }
}
