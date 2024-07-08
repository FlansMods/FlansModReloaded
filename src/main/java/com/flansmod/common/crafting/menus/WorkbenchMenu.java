package com.flansmod.common.crafting.menus;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.*;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.crafting.temporary.TemporaryWorkbench;
import com.flansmod.common.item.TemporaryWorkbenchItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WorkbenchMenu extends AbstractContainerMenu
{
	@Nonnull
	public final WorkbenchDefinition Def;
	@Nonnull
	public final ContainerData WorkbenchData;
	@Nonnull
	public final AbstractWorkbench Workbench;
	@Nonnull
	public final Level World;

	private Slot[] InventorySlots;

	public int ScrollIndex = 0;


	public WorkbenchMenu(@Nonnull MenuType<? extends WorkbenchMenu> menuType,
						 int containerID,
						 @Nonnull Inventory inventory,
						 @Nonnull AbstractWorkbench workbench)
	{
		super(menuType, containerID);
		World = inventory.player.level();
		Def = workbench.Def;
		Workbench = workbench;
		WorkbenchData = workbench.GetDataAccess();
	}

	@OnlyIn(Dist.CLIENT)
	public WorkbenchMenu(@Nonnull MenuType<? extends WorkbenchMenu> menuType,
						 int containerID,
						 @Nonnull Inventory inventory,
						 @Nonnull FriendlyByteBuf data)
	{
		super(menuType, containerID);

		World = inventory.player.level();
		BlockPos blockPos = data.readBlockPos();
		if(blockPos.equals(BlockPos.ZERO) && inventory.player.getMainHandItem().getItem() instanceof TemporaryWorkbenchItem workbenchItem)
		{
			Def = FlansMod.WORKBENCHES.Get(workbenchItem.DefLoc);
			TemporaryWorkbench tempWorkbench = FlansModClient.INVENTORY_MANAGER.GetTemporaryInventory(workbenchItem.DefLoc);
			Workbench = tempWorkbench.Workbench;
			WorkbenchData = tempWorkbench.Workbench.GetDataAccess();
		}
		else if(inventory.player.level().getBlockEntity(blockPos) instanceof WorkbenchBlockEntity workbenchBlockEntity)
		{
			Def = workbenchBlockEntity.Def;
			Workbench = workbenchBlockEntity.Workbench;
			WorkbenchData = workbenchBlockEntity.DataAccess;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			Def = WorkbenchDefinition.INVALID;
			Workbench = null;
			WorkbenchData = null;
		}
	}

	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		addDataSlots(WorkbenchData);

		InventorySlots = new Slot[4 * 9];
		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				addSlot(InventorySlots[x + 9 * y + 9] = new RestrictedSlot(playerInventory, x + y * 9 + 9, inventorySlotOffsetX + 6 + x * 18, 137 + y * 18));
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			addSlot(InventorySlots[x] = new RestrictedSlot(playerInventory, x, inventorySlotOffsetX + 6 + x * 18, 195));
		}
	}

	@Nonnull
	protected ItemStack QuickStackIntoInventory(@Nonnull Player player, @Nonnull Slot slot)
	{
		if(player.getInventory().add(slot.getItem()))
		{
			slot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		return slot.getItem();
	}

	@Override
	public void removed(@Nonnull Player player)
	{
		super.removed(player);

		if(player.level().isClientSide)
		{
			ClientRemoved();
		}
		else
		{
			FlansMod.INVENTORY_MANAGER.CloseTemporaryInventory(player);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientRemoved()
	{
		FlansModClient.INVENTORY_MANAGER.CloseTemporaryInventory();
	}

	@Override
	public boolean stillValid(@Nonnull Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }

}
