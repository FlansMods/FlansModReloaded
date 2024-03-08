package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.slots.AttachmentSlot;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WorkbenchMenuModification extends WorkbenchMenu
{
	public enum ModSlot
	{
		SIGHTS(1,0, EAttachmentType.Sights, 0),
		BARREL(0, 1, EAttachmentType.Barrel, 0),
		GRIP(1, 2, EAttachmentType.Grip, 0),
		STOCK(2, 1, EAttachmentType.Stock, 0),

		SECONDARY_SIGHTS(2,0, EAttachmentType.Sights, 1),
		SECONDARY_BARREL(0, 0, EAttachmentType.Barrel, 1),
		SECONDARY_GRIP(0, 2, EAttachmentType.Grip, 1),
		SECONDARY_STOCK(2, 2, EAttachmentType.Stock, 1);

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

	public static final int BUTTON_SELECT_SKIN_0 					= 0x00; // 128 skins
	public static final int BUTTON_SELECT_SKIN_MAX 					= 0x7f;
	public static final int BUTTON_SELECT_MAGAZINE_0 				= 0x80; // 128 mags
	public static final int BUTTON_SELECT_MAGAZINE_MAX				= 0xff;


	public final Container GunContainer;
	public final Container PaintCanContainer;
	public final Container MagUpgradeContainer;

	private RestrictedSlot GunSlot;
	private RestrictedSlot PaintCanSlot;
	private RestrictedSlot MagUpgradeSlot;
	private AttachmentSlot[] AttachmentSlots;

	public WorkbenchMenuModification(int containerID,
							  @Nonnull Inventory inventory,
							  @Nonnull WorkbenchBlockEntity workbench)
	{
		super(FlansMod.WORKBENCH_MENU_MODIFICATION.get(), containerID, inventory, workbench);
		GunContainer = BlockEntity.GunContainer;
		PaintCanContainer = BlockEntity.PaintCanContainer;
		MagUpgradeContainer = BlockEntity.MagUpgradeContainer;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenuModification(int containerID,
							  @Nonnull Inventory inventory,
							  @Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_MODIFICATION.get(), containerID, inventory, data);

		GunContainer = BlockEntity.GunContainer;
		PaintCanContainer = BlockEntity.PaintCanContainer;
		MagUpgradeContainer = BlockEntity.MagUpgradeContainer;
		CreateSlots(inventory, 0);
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		// Signed byte please
		if(buttonID < 0)
			buttonID += 256;
		if(BUTTON_SELECT_SKIN_0 <= buttonID && buttonID <= BUTTON_SELECT_SKIN_MAX)
		{
			int skinIndex = buttonID - BUTTON_SELECT_SKIN_0;
			if(!player.level().isClientSide)
				WorkbenchBlockEntity.PaintGun(player, GunContainer, PaintCanContainer, skinIndex);
			return true;
		}
		if(BUTTON_SELECT_MAGAZINE_0 <= buttonID && buttonID <= BUTTON_SELECT_MAGAZINE_MAX)
		{
			int magIndex = buttonID - BUTTON_SELECT_MAGAZINE_0;
			if(!player.level().isClientSide)
				WorkbenchBlockEntity.SelectMagazine(player, GunContainer, MagUpgradeContainer, magIndex);
			return true;
		}
		return false;
	}

	@Override
	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		super.CreateSlots(playerInventory, inventorySlotOffsetX);
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
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(GunSlot != null && slot == GunSlot.index)
			return QuickStackIntoInventory(player, GunSlot);
		else if(PaintCanSlot != null && slot == PaintCanSlot.index)
			return QuickStackIntoInventory(player, PaintCanSlot);
		else if(MagUpgradeSlot != null && slot == MagUpgradeSlot.index)
			return QuickStackIntoInventory(player, MagUpgradeSlot);
		else if(AttachmentSlots.length > 0 && slot >= AttachmentSlots[0].index && slot < AttachmentSlots[0].index + AttachmentSlots.length)
		{
			int attachmentSlotIndex = slot - AttachmentSlots[0].index;
			return QuickStackIntoInventory(player, AttachmentSlots[attachmentSlotIndex]);
		}
		else
		{
			// We are shifting from the player into the inventory
			ItemStack stack = slots.get(slot).getItem();
			if(GunSlot != null && GunSlot.getItem().isEmpty() && stack.getItem() instanceof GunItem)
			{
				GunSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			if(PaintCanSlot != null && PaintCanSlot.getItem().isEmpty() && stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get())
			{
				PaintCanSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			if(MagUpgradeSlot != null && MagUpgradeSlot.getItem().isEmpty() && stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get())
			{
				MagUpgradeSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			for(AttachmentSlot attachmentSlot : AttachmentSlots)
			{
				if(attachmentSlot.getItem().isEmpty() && attachmentSlot.mayPlace(stack))
				{
					attachmentSlot.set(stack.copyWithCount(1));
					slots.get(slot).set(stack.copyWithCount(stack.getCount() - 1));
					return ItemStack.EMPTY;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}

