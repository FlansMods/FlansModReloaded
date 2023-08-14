package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

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
	public final Container PaintCanContainer;
	public final Container MagUpgradeContainer;
	public final Container MaterialContainer;
	public final Container BatteryContainer;
	public final Container FuelContainer;
	public final WorkbenchDefinition Def;
	public final ContainerData WorkbenchData;
	//public final WorkbenchBlockEntity Workbench;

	private RestrictedSlot GunSlot;
	private RestrictedSlot PaintCanSlot;
	private RestrictedSlot MagUpgradeSlot;
	private RestrictedSlot FuelSlot;
	private RestrictedSlot BatterySlot;
	private AttachmentSlot[] AttachmentSlots;
	private RestrictedSlot[] MaterialSlots;

	//public static final int BUTTON_CANCEL = 0;

	public static final int BUTTON_SELECT_RECIPE_0 = 1000;
	public static final int BUTTON_SELECT_RECIPE_MAX = 1999;
	public static final int BUTTON_SELECT_SKIN_0 = 2000;
	public static final int BUTTON_SELECT_SKIN_MAX = 2999;
	public static final int BUTTON_SELECT_MAGAZINE_0 = 3000;
	public static final int BUTTON_SELECT_MAGAZINE_MAX = 3999;

	public WorkbenchMenu(int containerID, Inventory inventory,
						 WorkbenchDefinition def,
						 Container gunContainer,
						 Container paintCanContainer,
						 Container magUpgradeContainer,
						 Container materialContainer,
						 Container batteryContainer,
						 Container fuelContainer,
						 ContainerData dataAccess)
	{
		super(FlansMod.WORKBENCH_MENU.get(), containerID);
		Def = def;
		GunContainer = gunContainer;
		PaintCanContainer = paintCanContainer;
		MagUpgradeContainer = magUpgradeContainer;
		MaterialContainer = materialContainer;
		BatteryContainer = batteryContainer;
		FuelContainer = fuelContainer;
		WorkbenchData = dataAccess;
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
			PaintCanContainer = workbenchBlockEntity.PaintCanContainer;
			MagUpgradeContainer = workbenchBlockEntity.MagUpgradeContainer;
			MaterialContainer = workbenchBlockEntity.MaterialContainer;
			BatteryContainer = workbenchBlockEntity.BatteryContainer;
			FuelContainer = workbenchBlockEntity.FuelContainer;
			WorkbenchData = workbenchBlockEntity.DataAccess;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			Def = WorkbenchDefinition.INVALID;
			GunContainer = null; // um?
			PaintCanContainer = null;
			MagUpgradeContainer = null;
			MaterialContainer = null;
			BatteryContainer = null;
			FuelContainer = null;
			WorkbenchData = null;
		}
		CreateSlots(inventory);
	}

	private void CreateSlots(Inventory playerInventory)
	{
		addDataSlots(WorkbenchData);

		if (GunContainer.getContainerSize() > 0)
		{
			addSlot(GunSlot = new RestrictedSlot(GunContainer, 0, 34, 55));
			addSlot(PaintCanSlot = new RestrictedSlot(PaintCanContainer, 0, 77, 26));
			addSlot(MagUpgradeSlot = new RestrictedSlot(MagUpgradeContainer, 0, 77, 84));
			AttachmentSlots = new AttachmentSlot[ModSlot.values().length];
			for (ModSlot modSlot : ModSlot.values())
			{
				addSlot(AttachmentSlots[modSlot.ordinal()] =
					new AttachmentSlot(
						GunSlot,
						modSlot.attachType,
						modSlot.attachIndex,
						GunContainer,
						8 + 26 * modSlot.x,
						29 + 26 * modSlot.y));
			}
		}
		else AttachmentSlots = new AttachmentSlot[0];

		MaterialSlots = new RestrictedSlot[MaterialContainer.getContainerSize()];
		for(int j = 0; j < MaterialContainer.getContainerSize() / 9 + 1; j++)
		{
			for(int i = 0; i < 9; i++)
			{
				if(j * 9 + i < MaterialContainer.getContainerSize())
				{
					addSlot(MaterialSlots[j * 9 + i] = new RestrictedSlot(MaterialContainer, j * 9 + i, 6 + i * 18, 23 + j * 18));
				}
			}
		}

		if(BatteryContainer.getContainerSize() > 0)
		{
			addSlot(BatterySlot = new RestrictedSlot(BatteryContainer, 0, 78, 66));
		}

		if(FuelContainer.getContainerSize() > 0)
		{
			addSlot(FuelSlot = new RestrictedSlot(FuelContainer, 0, 129, 66));
		}

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

		if(GunContainer.getContainerSize() > 0)
			SwitchToGunModification();
		else if(MaterialContainer.getContainerSize() > 0)
			SwitchToMaterials();
		else if(BatteryContainer.getContainerSize() > 0)
			SwitchToPower();
		else if(FuelContainer.getContainerSize() > 0)
			SwitchToPower();
		else SwitchToPower();
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToGunModification()
	{
		if(GunSlot != null)
		{
			HideSlots();
			GunSlot.SetActive(true);
			PaintCanSlot.SetActive(true);
			MagUpgradeSlot.SetActive(true);
			for(AttachmentSlot slot : AttachmentSlots)
				slot.SetActive(true);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToMaterials()
	{
		HideSlots();
		for(RestrictedSlot slot : MaterialSlots)
			slot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToPower()
	{
		HideSlots();
		if(FuelSlot != null)
			FuelSlot.SetActive(true);
		if(BatterySlot != null)
			BatterySlot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void HideSlots()
	{
		if(GunSlot != null)
			GunSlot.SetActive(false);
		if(PaintCanSlot != null)
			PaintCanSlot.SetActive(false);
		if(MagUpgradeSlot != null)
			MagUpgradeSlot.SetActive(false);
		if(FuelSlot != null)
			FuelSlot.SetActive(false);
		if(BatterySlot != null)
			BatterySlot.SetActive(false);
		for(AttachmentSlot slot : AttachmentSlots)
			slot.SetActive(false);
		for(RestrictedSlot slot : MaterialSlots)
			slot.SetActive(false);
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonID)
	{
		switch(buttonID)
		{
			//case BUTTON_CANCEL -> { return true; }
			default -> {
				if(BUTTON_SELECT_RECIPE_0 <= buttonID && buttonID <= BUTTON_SELECT_RECIPE_MAX)
				{
					int recipeIndex = buttonID - BUTTON_SELECT_RECIPE_0;
					// Craft
					return true;
				}
				if(BUTTON_SELECT_SKIN_0 <= buttonID && buttonID <= BUTTON_SELECT_SKIN_MAX)
				{
					int skinIndex = buttonID - BUTTON_SELECT_SKIN_0;
					WorkbenchBlockEntity.PaintGun(player, GunContainer, skinIndex);
					return true;
				}

				if(BUTTON_SELECT_MAGAZINE_0 <= buttonID && buttonID <= BUTTON_SELECT_MAGAZINE_MAX)
				{
					int magIndex = buttonID - BUTTON_SELECT_MAGAZINE_0;
					WorkbenchBlockEntity.SelectMagazine(player, GunContainer, magIndex);
					return true;
				}
			}
		}

		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		if(GunSlot != null && slot == GunSlot.index)
			return QuickStackIntoInventory(player, GunSlot);
		if(PaintCanSlot != null && slot == PaintCanSlot.index)
			return QuickStackIntoInventory(player, PaintCanSlot);
		if(MagUpgradeSlot != null && slot == MagUpgradeSlot.index)
			return QuickStackIntoInventory(player, MagUpgradeSlot);
		else if(FuelSlot != null && slot == FuelSlot.index)
			return QuickStackIntoInventory(player, FuelSlot);
		else if(BatterySlot != null && slot == BatterySlot.index)
			return QuickStackIntoInventory(player, BatterySlot);
		else if(AttachmentSlots.length > 0 && slot >= AttachmentSlots[0].index && slot < AttachmentSlots[0].index + AttachmentSlots.length)
		{
			int attachmentSlotIndex = slot - AttachmentSlots[0].index;
			return QuickStackIntoInventory(player, AttachmentSlots[attachmentSlotIndex]);
		}
		else if(MaterialSlots.length > 0 && slot >= MaterialSlots[0].index && slot < MaterialSlots[0].index + MaterialSlots.length)
		{
			int materialSlotIndex = slot - MaterialSlots[0].index;
			return QuickStackIntoInventory(player, MaterialSlots[materialSlotIndex]);
		}
		else
		{
			// We are shifting from the player into the inventory
			ItemStack stack = slots.get(slot).getItem();
			if(GunSlot != null && GunSlot.getItem().isEmpty() && stack.getItem() instanceof GunItem)
			{
				GunSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
			}
			if(PaintCanSlot != null && PaintCanSlot.getItem().isEmpty() && stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get())
			{
				PaintCanSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
			}
			if(MagUpgradeSlot != null && MagUpgradeSlot.getItem().isEmpty() && stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get())
			{
				MagUpgradeSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
			}
			for(AttachmentSlot attachmentSlot : AttachmentSlots)
			{
				if(attachmentSlot.mayPlace(stack))
				{
					attachmentSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
				}
			}
		}


		return ItemStack.EMPTY;
	}
	private ItemStack QuickStackIntoInventory(Player player, Slot slot)
	{
		if(player.getInventory().add(slot.getItem()))
		{
			slot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		return slot.getItem();
	}

	@Override
	public boolean stillValid(Player player) { return true; } //return GunContainer != null && GunContainer.stillValid(player); }

}
