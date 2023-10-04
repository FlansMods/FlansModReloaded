package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.parts.PartDefinition;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
	public final Container GunCraftingInputContainer;
	public final Container GunCraftingOutputContainer;
	public final Container PartCraftingInputContainer;
	public final Container PartCraftingOutputContainer;
	public final Container MaterialContainer;
	public final Container BatteryContainer;
	public final Container FuelContainer;
	public final WorkbenchDefinition Def;
	public final ContainerData WorkbenchData;
	public final WorkbenchBlockEntity BlockEntity;

	public static final int NO_RECIPE_SELECTED = -1;
	public int SelectedGunRecipeIndex = NO_RECIPE_SELECTED;
	public int SelectedPartRecipeIndex = NO_RECIPE_SELECTED;

	private RestrictedSlot GunSlot;
	private RestrictedSlot PaintCanSlot;
	private RestrictedSlot MagUpgradeSlot;
	protected GunCraftingInputSlot[] GunCraftingInputSlots;
	protected GunCraftingOutputSlot GunCraftingOutputSlot;
	protected RestrictedSlot[] PartCraftingInputSlots;
	protected RestrictedSlot[] PartCraftingOutputSlots;
	private RestrictedSlot FuelSlot;
	private RestrictedSlot BatterySlot;
	private AttachmentSlot[] AttachmentSlots;
	private RestrictedSlot[] MaterialSlots;
	private Slot[] InventorySlots;




	public static final int PART_CRAFTING_NUM_INPUT_SLOTS_X = 4;
	public static final int PART_CRAFTING_NUM_INPUT_SLOTS_Y = 11;
	public static final int PART_CRAFTING_INPUT_SLOTS_X = 5;
	public static final int PART_CRAFTING_INPUT_SLOTS_Y = 18;
	public static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_X = 4;
	public static final int PART_CRAFTING_NUM_OUTPUT_SLOTS_Y = 6;
	public static final int PART_CRAFTING_OUTPUT_SLOTS_X = 279;
	public static final int PART_CRAFTING_OUTPUT_SLOTS_Y = 115;


	public static final int GUN_CRAFTING_NUM_SLOTS_X = 4;
	public static final int GUN_CRAFTING_NUM_SLOTS_Y = 2;

	public int ScrollIndex = 0;
	public static final int BUTTON_SELECT_GUN_RECIPE_0 				= 0x0;	// 32 gun recipes
	public static final int BUTTON_SELECT_GUN_RECIPE_MAX 			= 0x1f;
	public static final int BUTTON_SELECT_SKIN_0 					= 0x20; // 16 skins
	public static final int BUTTON_SELECT_SKIN_MAX 					= 0x2f;
	public static final int BUTTON_SELECT_MAGAZINE_0 				= 0x30; // 16 mags
	public static final int BUTTON_SELECT_MAGAZINE_MAX				= 0x3f;
	public static final int BUTTON_AUTO_FILL_INGREDIENT_0 			= 0x40; // 16 auto-fill buttons
	public static final int BUTTON_AUTO_FULL_INGREDIENT_MAX 		= 0x4f;
	public static final int BUTTON_CRAFT_1 							= 0x51; // Technically there are 16 buttons here
	public static final int BUTTON_CRAFT_5							= 0x55;
	public static final int BUTTON_CRAFT_ALL						= 0x5f;
	public static final int BUTTON_QUEUE_CANCEL_0 					= 0x60; // 16 queue cancels
	public static final int BUTTON_QUEUE_CANCEL_MAX					= 0x6f;
	public static final int BUTTON_SELECT_PART_RECIPE_0				= 0x70; // 144 part recipes
	public static final int BUTTON_SELECT_PART_RECIPE_MAX			= 0xff;


	public static final int BUTTON_SET_RECIPE_SCROLL_0				= 0x1000; // Needs to be networked??
	public static final int BUTTON_SET_RECIPE_SCROLL_MAX 			= 0x10ff;


	public WorkbenchMenu(int containerID, Inventory inventory,
						 WorkbenchDefinition def,
						 WorkbenchBlockEntity workbench,
						 Container gunContainer,
						 Container paintCanContainer,
						 Container magUpgradeContainer,
						 Container gunCraftingInputContainer,
						 Container gunCraftingOutputContainer,
						 Container partCraftingInputContainer,
						 Container partCraftingOutputContainer,
						 Container materialContainer,
						 Container batteryContainer,
						 Container fuelContainer,
						 ContainerData dataAccess)
	{
		super(FlansMod.WORKBENCH_MENU.get(), containerID);
		Def = def;
		BlockEntity = workbench;
		GunContainer = gunContainer;
		PaintCanContainer = paintCanContainer;
		MagUpgradeContainer = magUpgradeContainer;
		GunCraftingInputContainer = gunCraftingInputContainer;
		GunCraftingOutputContainer = gunCraftingOutputContainer;
		PartCraftingInputContainer = partCraftingInputContainer;
		PartCraftingOutputContainer = partCraftingOutputContainer;
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
			BlockEntity = workbenchBlockEntity;
			GunContainer = workbenchBlockEntity.GunContainer;
			PaintCanContainer = workbenchBlockEntity.PaintCanContainer;
			MagUpgradeContainer = workbenchBlockEntity.MagUpgradeContainer;
			GunCraftingInputContainer = workbenchBlockEntity.GunCraftingInputContainer;
			GunCraftingOutputContainer = workbenchBlockEntity.GunCraftingOutputContainer;
			PartCraftingInputContainer = workbenchBlockEntity.PartCraftingInputContainer;
			PartCraftingOutputContainer = workbenchBlockEntity.PartCraftingOutputContainer;
			MaterialContainer = workbenchBlockEntity.MaterialContainer;
			BatteryContainer = workbenchBlockEntity.BatteryContainer;
			FuelContainer = workbenchBlockEntity.FuelContainer;
			WorkbenchData = workbenchBlockEntity.DataAccess;
		}
		else
		{
			FlansMod.LOGGER.error("Could not read GunModificationMenu data");
			Def = WorkbenchDefinition.INVALID;
			BlockEntity = null;
			GunContainer = null; // um?
			PaintCanContainer = null;
			MagUpgradeContainer = null;
			GunCraftingInputContainer = null;
			GunCraftingOutputContainer = null;
			PartCraftingInputContainer = null;
			PartCraftingOutputContainer = null;
			MaterialContainer = null;
			BatteryContainer = null;
			FuelContainer = null;
			WorkbenchData = null;
		}
		CreateSlots(inventory);
	}

	private void CreateSlots(Inventory playerInventory)
	{
		if(GunContainer == null)
			return;

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

		if(GunCraftingOutputContainer.getContainerSize() > 0)
		{
			addSlot(GunCraftingOutputSlot = new GunCraftingOutputSlot(this, GunCraftingOutputContainer, 0, 150, 77));
		}
		GunCraftingInputSlots = new GunCraftingInputSlot[GunCraftingInputContainer.getContainerSize()];
		if(GunCraftingInputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < GunCraftingInputContainer.getContainerSize(); i++)
			{
				addSlot(GunCraftingInputSlots[i] = new GunCraftingInputSlot(this, GunCraftingInputContainer, i, 78, 66));
			}
		}

		PartCraftingInputSlots = new RestrictedSlot[PartCraftingInputContainer.getContainerSize()];
		if(PartCraftingInputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
			{
				int x = i % PART_CRAFTING_NUM_INPUT_SLOTS_X;
				int y = i / PART_CRAFTING_NUM_INPUT_SLOTS_X;
				addSlot(PartCraftingInputSlots[i] = new RestrictedSlot(PartCraftingInputContainer, i, PART_CRAFTING_INPUT_SLOTS_X + 1 + x * 18, PART_CRAFTING_INPUT_SLOTS_Y + 1 + y * 18));
			}
		}
		PartCraftingOutputSlots = new RestrictedSlot[PartCraftingOutputContainer.getContainerSize()];
		if(PartCraftingOutputContainer.getContainerSize() > 0)
		{
			for(int i = 0; i < PartCraftingOutputContainer.getContainerSize(); i++)
			{
				int x = i % PART_CRAFTING_NUM_OUTPUT_SLOTS_X;
				int y = i / PART_CRAFTING_NUM_OUTPUT_SLOTS_X;
				addSlot(PartCraftingOutputSlots[i] = new RestrictedSlot(PartCraftingOutputContainer, i, PART_CRAFTING_OUTPUT_SLOTS_X + 1 + x * 18, PART_CRAFTING_OUTPUT_SLOTS_Y + 1 + y * 18));
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

		if(GunCraftingOutputContainer.getContainerSize() > 0)
			SwitchToGunCrafting();
		else if(PartCraftingOutputContainer.getContainerSize() > 0)
			SwitchToPartCrafting();
		else if(GunContainer.getContainerSize() > 0)
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
		HideSlots();
		OffsetInventorySlots(0, 0);
		if(GunSlot != null)
		{
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
		OffsetInventorySlots(0, 0);
		for(RestrictedSlot slot : MaterialSlots)
			slot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToPower()
	{
		HideSlots();
		OffsetInventorySlots(0, 0);
		if(FuelSlot != null)
			FuelSlot.SetActive(true);
		if(BatterySlot != null)
			BatterySlot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToPartCrafting()
	{
		HideSlots();
		OffsetInventorySlots(92, 0);
		for (RestrictedSlot slot : PartCraftingInputSlots)
			slot.SetActive(true);
		for (RestrictedSlot slot : PartCraftingOutputSlots)
			slot.SetActive(true);
	}

	@OnlyIn(Dist.CLIENT)
	public void SwitchToArmourCrafting()
	{
		HideSlots();
		OffsetInventorySlots(0, 0);
	}

	public void SwitchToGunCrafting()
	{
		HideSlots();
		OffsetInventorySlots(0, 0);
		if(GunCraftingOutputSlot != null)
			GunCraftingOutputSlot.SetActive(true);
		// TODO: Check scroller and move them
		for(int i = 0; i < GunCraftingInputSlots.length; i++)
		{
			boolean active = false;
			if(!GunCraftingInputSlots[i].getItem().isEmpty())
				active = true;
			if(SelectedGunRecipeIndex >= 0)
			{
				GunCraftingEntryDefinition selectedEntry = GetEntry(SelectedGunRecipeIndex);
				if(selectedEntry != null)
				{
					int slotCount = 0;
					for(RecipePartDefinition part : selectedEntry.parts)
						slotCount += part.tieredIngredients.length + part.additionalIngredients.length;
					if(i < slotCount)
						active = true;
				}
			}

			// x,y are final. Let's hack
			GunCraftingInputSlot replacementSlot = new GunCraftingInputSlot(
				this,
				GunCraftingInputContainer,
				i,
				active ? 50 + 20 * (i % GUN_CRAFTING_NUM_SLOTS_X) : -1000,
				active ? 56 + 30 * (i / GUN_CRAFTING_NUM_SLOTS_X) : -1000);

			replacementSlot.index = GunCraftingInputSlots[i].index;
			GunCraftingInputSlots[i].SetActive(false);
			slots.set(replacementSlot.index, replacementSlot);
			GunCraftingInputSlots[i] = replacementSlot;
			GunCraftingInputSlots[i].SetActive(active);
		}
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
		if(GunCraftingOutputSlot != null)
			GunCraftingOutputSlot.SetActive(false);
		for(GunCraftingInputSlot slot : GunCraftingInputSlots)
			slot.SetActive(false);
		if(FuelSlot != null)
			FuelSlot.SetActive(false);
		if(BatterySlot != null)
			BatterySlot.SetActive(false);
		for(AttachmentSlot slot : AttachmentSlots)
			slot.SetActive(false);
		for(RestrictedSlot slot : MaterialSlots)
			slot.SetActive(false);
		for (RestrictedSlot slot : PartCraftingInputSlots)
			slot.SetActive(false);
		for (RestrictedSlot slot : PartCraftingOutputSlots)
			slot.SetActive(false);
	}

	@OnlyIn(Dist.CLIENT)
	public void OffsetInventorySlots(int xOffset, int yOffset)
	{
		// x,y are final. Let's hack
		Slot[] previousSlots = InventorySlots;
		InventorySlots = new Slot[4 * 9];
		for(int y = 0; y < 3; ++y)
		{
			for(int x = 0; x < 9; ++x)
			{
				Slot replacementSlot = new RestrictedSlot(previousSlots[x + 9 * y + 9].container, x + y * 9 + 9, 6 + x * 18 + xOffset, 137 + y * 18 + yOffset);
				replacementSlot.index = previousSlots[x + 9 * y + 9].index;
				slots.set(replacementSlot.index, replacementSlot);
				InventorySlots[x + 9 * y + 9] = replacementSlot;
			}
		}

		for(int x = 0; x < 9; ++x)
		{
			Slot replacementSlot = new RestrictedSlot(previousSlots[x].container, x, 6 + x * 18 + xOffset, 195 + yOffset);
			replacementSlot.index = previousSlots[x].index;
			slots.set(replacementSlot.index, replacementSlot);
			InventorySlots[x] = replacementSlot;
		}
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		switch(buttonID)
		{
			case BUTTON_CRAFT_ALL -> {
				CraftParts(-1);
				return true;
			}
			default -> {
				if(BUTTON_SELECT_GUN_RECIPE_0 <= buttonID && buttonID <= BUTTON_SELECT_GUN_RECIPE_MAX)
				{
					SelectedGunRecipeIndex = buttonID - BUTTON_SELECT_GUN_RECIPE_0;
					UpdateGunCraftingOutput();
					SwitchToGunCrafting();
					return true;
				}
				if(BUTTON_SELECT_SKIN_0 <= buttonID && buttonID <= BUTTON_SELECT_SKIN_MAX)
				{
					int skinIndex = buttonID - BUTTON_SELECT_SKIN_0;
					if(!player.level.isClientSide)
						WorkbenchBlockEntity.PaintGun(player, GunContainer, PaintCanContainer, skinIndex);
					return true;
				}
				if(BUTTON_SELECT_MAGAZINE_0 <= buttonID && buttonID <= BUTTON_SELECT_MAGAZINE_MAX)
				{
					int magIndex = buttonID - BUTTON_SELECT_MAGAZINE_0;
					if(!player.level.isClientSide)
						WorkbenchBlockEntity.SelectMagazine(player, GunContainer, MagUpgradeContainer, magIndex);
					return true;
				}
				if(BUTTON_AUTO_FILL_INGREDIENT_0 <= buttonID && buttonID <= BUTTON_AUTO_FULL_INGREDIENT_MAX)
				{
					int ingredientIndex = buttonID - BUTTON_AUTO_FILL_INGREDIENT_0;
					// Auto fill operates only on the containers, so we can do it in the Menu, not the BlockEntity
					AutoFillGunCraftingInputSlot(player, ingredientIndex);
					return true;
				}
				if(BUTTON_SET_RECIPE_SCROLL_0 <= buttonID && buttonID <= BUTTON_SET_RECIPE_SCROLL_MAX)
				{
					int scrollIndex = buttonID - BUTTON_SET_RECIPE_SCROLL_0;
					if(scrollIndex != ScrollIndex)
					{
						ScrollIndex = scrollIndex;
						SwitchToGunCrafting();
					}
					return true;
				}
				if(BUTTON_CRAFT_1 <= buttonID && buttonID < BUTTON_CRAFT_ALL)
				{
					int craftCount = buttonID - BUTTON_CRAFT_1 + 1;
					CraftParts(craftCount);
					return true;
				}
				if(BUTTON_SELECT_PART_RECIPE_0 <= buttonID && buttonID <= BUTTON_SELECT_PART_RECIPE_MAX)
				{
					SelectedPartRecipeIndex = buttonID - BUTTON_SELECT_PART_RECIPE_0;
					BlockEntity.SelectPartCraftingRecipe(SelectedPartRecipeIndex);
					SwitchToPartCrafting();
					return true;
				}
				if(BUTTON_QUEUE_CANCEL_0 <= buttonID && buttonID <= BUTTON_QUEUE_CANCEL_MAX)
				{
					int cancelIndex = buttonID - BUTTON_QUEUE_CANCEL_0;
					BlockEntity.CancelQueue(cancelIndex);
					SwitchToPartCrafting();
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
		else if(PaintCanSlot != null && slot == PaintCanSlot.index)
			return QuickStackIntoInventory(player, PaintCanSlot);
		else if(MagUpgradeSlot != null && slot == MagUpgradeSlot.index)
			return QuickStackIntoInventory(player, MagUpgradeSlot);
		else if(GunCraftingOutputSlot != null && slot == GunCraftingOutputSlot.index)
			return QuickStackIntoInventory(player, GunCraftingOutputSlot);
		else if(GunCraftingInputSlots != null && GunCraftingInputSlots.length > 0 && slot >= GunCraftingInputSlots[0].index && slot < GunCraftingInputSlots[0].index + GunCraftingInputSlots.length)
		{
			int craftingInputSlotIndex = slot - GunCraftingInputSlots[0].index;
			return QuickStackIntoInventory(player, GunCraftingInputSlots[craftingInputSlotIndex]);
		}
		else if(PartCraftingInputSlots != null && PartCraftingInputSlots.length > 0 && slot >= PartCraftingInputSlots[0].index && slot < PartCraftingInputSlots[0].index + PartCraftingInputSlots.length)
		{
			int craftingInputSlotIndex = slot - PartCraftingInputSlots[0].index;
			return QuickStackIntoInventory(player, PartCraftingInputSlots[craftingInputSlotIndex]);
		}
		else if(PartCraftingOutputSlots != null && PartCraftingOutputSlots.length > 0 && slot >= PartCraftingOutputSlots[0].index && slot < PartCraftingOutputSlots[0].index + PartCraftingOutputSlots.length)
		{
			int craftingOutputSlotIndex = slot - PartCraftingOutputSlots[0].index;
			return QuickStackIntoInventory(player, PartCraftingOutputSlots[craftingOutputSlotIndex]);
		}
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
			for(RestrictedSlot partInputSlot : PartCraftingInputSlots)
			{
				if(partInputSlot.getItem().isEmpty())
				{
					partInputSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
			for(AttachmentSlot attachmentSlot : AttachmentSlots)
			{
				if(attachmentSlot.getItem().isEmpty())
				{
					attachmentSlot.set(stack);
					slots.get(slot).set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public void CraftParts(int count)
	{
		BlockEntity.QueueCrafting(count);
	}

	public void AutoFillGunCraftingInputSlot(Player player, int inputSlotIndex)
	{
		if(SelectedGunRecipeIndex >= 0
			&& 0 <= inputSlotIndex && inputSlotIndex < GunCraftingInputSlots.length
			&& GunCraftingInputSlots[inputSlotIndex].getItem().isEmpty())
		{
			GunCraftingEntryDefinition selectedEntry = GetEntry(SelectedGunRecipeIndex);
			Either<TieredIngredientDefinition, IngredientDefinition> ingredient = selectedEntry.GetIngredient(inputSlotIndex);
			if(ingredient != null)
			{
				ingredient.ifLeft((TieredIngredientDefinition tiered) -> {
					if(player.isCreative())
					{
						List<ItemStack> options = new ArrayList<>();
						tiered.GenerateMatches(options);
						if(options.size() > 0)
							GunCraftingInputSlots[inputSlotIndex].set(options.get(0));
					}
					else
					{
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack playerItem = player.getInventory().getItem(i);
							if(tiered.Matches(playerItem))
							{
								GunCraftingInputSlots[inputSlotIndex].set(playerItem.copyWithCount(1));
								playerItem.setCount(playerItem.getCount() - 1);
								break;
							}
						}
					}
				});

				ingredient.ifRight((IngredientDefinition basic) -> {
					if(player.isCreative())
					{
						List<ItemStack> options = new ArrayList<>();
						basic.GenerateMatches(options);
						if(options.size() > 0)
							GunCraftingInputSlots[inputSlotIndex].set(options.get(0));
					}
					else
					{
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack playerItem = player.getInventory().getItem(i);
							if(basic.Matches(playerItem))
							{
								GunCraftingInputSlots[inputSlotIndex].set(playerItem.copyWithCount(1));
								playerItem.setCount(playerItem.getCount() - 1);
								break;
							}
						}
					}
				});
			}

		}
	}

	public void ConsumeGunCraftingInputs()
	{
		if(GunCraftingOutputContainer.getContainerSize() > 0
			&& !GunCraftingOutputContainer.isEmpty()
			&& GunCraftingInputContainer.getContainerSize() > 0
			&& SelectedGunRecipeIndex >= 0)
		{
			GunCraftingEntryDefinition selectedEntry = GetEntry(SelectedGunRecipeIndex);
			if(selectedEntry != null)
			{
				int inputSlotIndex = 0;

				for(RecipePartDefinition part : selectedEntry.parts)
				{
					for(TieredIngredientDefinition tiered : part.tieredIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(tiered.Matches(input))
							input.setCount(input.getCount() - 1);

						inputSlotIndex++;
					}
					for(IngredientDefinition specific : part.additionalIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(specific.Matches(input))
							input.setCount(input.getCount() - specific.count);

						inputSlotIndex++;
					}
				}
			}
		}
	}

	public void UpdateGunCraftingOutput()
	{
		if(GunCraftingOutputContainer.getContainerSize() > 0 && GunCraftingInputContainer.getContainerSize() > 0 && SelectedGunRecipeIndex >= 0)
		{
			GunCraftingEntryDefinition selectedEntry = GetEntry(SelectedGunRecipeIndex);
			if(selectedEntry != null)
			{
				int inputSlotIndex = 0;
				List<ItemStack> craftedFromParts = new ArrayList<>();
				boolean match = true;

				for(RecipePartDefinition part : selectedEntry.parts)
				{
					for(TieredIngredientDefinition tiered : part.tieredIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(tiered.Matches(input))
							craftedFromParts.add(input);
						else
							match = false;

						inputSlotIndex++;
					}
					for(IngredientDefinition specific : part.additionalIngredients)
					{
						ItemStack input = GunCraftingInputContainer.getItem(inputSlotIndex);
						if(specific.Matches(input))
							craftedFromParts.add(input);
						else
							match = false;

						inputSlotIndex++;
					}
				}

				if(match)
				{
					ItemStack stack = selectedEntry.outputs[0].CreateStack();
					if(stack.getItem() instanceof FlanItem flanItem)
					{
						flanItem.SetCraftingInputs(stack, craftedFromParts);
					}
					GunCraftingOutputContainer.setItem(0, stack);
				}
				else
				{
					GunCraftingOutputContainer.setItem(0, ItemStack.EMPTY);
				}
			}
		}
	}

	private GunCraftingEntryDefinition GetEntry(int recipeIndex)
	{
		int index = 0;
		for(GunCraftingPageDefinition page : Def.gunCrafting.pages)
		{
			if(index <= recipeIndex && recipeIndex < index + page.entries.length)
			{
				return page.entries[recipeIndex - index];
			}
			else index += page.entries.length;
		}
		return null;
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
