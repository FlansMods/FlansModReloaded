package com.flansmod.packs.basics.common;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.packs.basics.BasicPartsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public class DistillationTowerMenu extends AbstractContainerMenu
{
	public final boolean IsTop;
	public final DistillationTowerBlockEntity Distiller;
	public final ContainerData DistillationDataContainer;

	public DistillationTowerMenu(int containerID, Inventory inventory,
						 boolean isTop,
						 DistillationTowerBlockEntity distiller,
						 ContainerData dataAccess)
	{
		super(BasicPartsMod.DISTILLATION_TOWER_MENU.get(), containerID);
		IsTop = isTop;
		Distiller = distiller;
		DistillationDataContainer = dataAccess;
		CreateSlots(inventory);
	}

	public DistillationTowerMenu(int containerID, Inventory inventory, FriendlyByteBuf data)
	{
		super(BasicPartsMod.DISTILLATION_TOWER_MENU.get(), containerID);
		BlockPos blockPos = data.readBlockPos();
		BlockEntity blockEntity = inventory.player.level.getBlockEntity(blockPos);
		if(blockEntity instanceof DistillationTowerBlockEntity distillationTowerBlockEntity)
		{
			IsTop = distillationTowerBlockEntity.IsTop;
			Distiller = distillationTowerBlockEntity;
			DistillationTowerBlockEntity topDistiller = distillationTowerBlockEntity.GetTopDistillationTileEntity();
			DistillationDataContainer = topDistiller == null ? distillationTowerBlockEntity.DataAccess : topDistiller.DataAccess;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read DistillationTowerMenu data");
			IsTop = false;
			Distiller = null;
			DistillationDataContainer = null;
		}
		CreateSlots(inventory);
	}

	@Override
	public boolean stillValid(@Nonnull Player player) { return Distiller.stillValid(player); }

	private void CreateSlots(Inventory playerInventory)
	{
		if (Distiller == null)
			return;

		addDataSlots(DistillationDataContainer);

		DistillationTowerBlockEntity[] distillerStack = Distiller.GetStack();

		boolean hasTop = distillerStack[0] != null;
		if(hasTop)
		{
			addSlot(new RestrictedSlot(distillerStack[0], DistillationTowerBlockEntity.INPUT_SLOT,15, 23));
			addSlot(new RestrictedSlot(distillerStack[0], DistillationTowerBlockEntity.FUEL_SLOT,15, 67));
		}

		for(int i = 0; i < DistillationTowerBlockEntity.MAX_DISTILLATION_STACK_HEIGHT; i++)
		{
			boolean hasLayer = distillerStack[i+1] != null;
			if(hasLayer)
			{
				addSlot(new RestrictedSlot(distillerStack[i+1], DistillationTowerBlockEntity.OUTPUT_SLOT,79, 40 + 20 * i));
			}
		}

		// Player inventory slots
		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				addSlot(new RestrictedSlot(playerInventory, x + y * 9 + 9, 6 + x * 18, 137 + y * 18));
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			addSlot(new RestrictedSlot(playerInventory, x, 6 + x * 18, 195));
		}
	}

	@Override
	@Nonnull
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		// TODO:
		return null;
	}


}
