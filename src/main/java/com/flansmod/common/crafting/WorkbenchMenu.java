package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorkbenchMenu extends AbstractContainerMenu
{
	public enum ModSlot
	{
		SCOPE(1,0, EAttachmentType.Sights, 0),
		BARREL(0, 1, EAttachmentType.Barrel, 0),
		GRIP(1, 2, EAttachmentType.Grip, 0),
		STOCK(2, 1, EAttachmentType.Stock, 0);

		ModSlot(int x, int y, EAttachmentType type, int index)
		{
			this.x = x;
			this.y = y;
			this.attachType = type;
			this.attachIndex = index;
		}

		public int x, y;
		public EAttachmentType attachType;
		public int attachIndex;
	}

	public final Container GunContainer;
	private final Container MaterialContainer;
	private final Container BatteryContainer;
	public final WorkbenchDefinition Def;

	private RestrictedSlot GunSlot;
	private AttachmentSlot[] AttachmentSlots;

	//public static final int BUTTON_CANCEL = 0;

	public WorkbenchMenu(int containerID, Inventory inventory,
						 WorkbenchDefinition def,
						 Container gunContainer,
						 Container materialContainer,
						 Container batteryContainer,
						 Container fuelContainer)
	{
		super(FlansMod.WORKBENCH_MENU.get(), containerID);
		Def = def;
		GunContainer = gunContainer;
		MaterialContainer = materialContainer;
		BatteryContainer = batteryContainer;
		CreateSlots(inventory);
	}

	public WorkbenchMenu(int containerID, Inventory inventory, FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU.get(), containerID);

		BlockPos blockPos = data.readBlockPos();
		BlockEntity blockEntity = inventory.player.level.getBlockEntity(blockPos);
		if(blockEntity instanceof WorkbenchBlockEntity workbenchBlockEntity)
		{
			Def = workbenchBlockEntity.Def;
			GunContainer = workbenchBlockEntity.GunContainer;
			MaterialContainer = workbenchBlockEntity.MaterialContainer;
			BatteryContainer = workbenchBlockEntity.BatteryContainer;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			Def = WorkbenchDefinition.INVALID;
			GunContainer = null; // um?
			MaterialContainer = null;
			BatteryContainer = null;
		}
		CreateSlots(inventory);
	}

	private void CreateSlots(Inventory playerInventory)
	{


		addSlot(GunSlot = new RestrictedSlot(GunContainer, 0, 120, 99));

		AttachmentSlots = new AttachmentSlot[ModSlot.values().length];
		for(ModSlot modSlot : ModSlot.values())
		{
			addSlot(AttachmentSlots[modSlot.ordinal()] = new AttachmentSlot(GunSlot, modSlot.attachType, modSlot.attachIndex, GunContainer, modSlot.x, modSlot.y));
		}

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < MaterialContainer.getContainerSize() / 3 + 1; j++)
			{

			}
		}

		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				addSlot(new RestrictedSlot(playerInventory, x + y * 9 + 9, 6 + x * 18, 120 + y * 18));
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			addSlot(new RestrictedSlot(playerInventory, x, 6 + x * 18, 178));
		}

		HideSlots();
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToGunModification()
	{
		HideSlots();
		GunSlot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void HideSlots()
	{
		GunSlot.SetActive(false);
		for(AttachmentSlot slot : AttachmentSlots)
			slot.SetActive(false);
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonID)
	{
		switch(buttonID)
		{
			//case BUTTON_CANCEL -> { return true; }
		}

		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override
	public boolean stillValid(Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }

}
