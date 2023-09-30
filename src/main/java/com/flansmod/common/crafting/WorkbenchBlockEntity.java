package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.crafting.elements.TieredIngredientDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider, Clearable
{
	public final WorkbenchDefinition Def;
	public final RestrictedContainer GunContainer;
	public final RestrictedContainer PaintCanContainer;
	public final RestrictedContainer MagUpgradeContainer;
	public final RestrictedContainer GunCraftingInputContainer;
	public final RestrictedContainer GunCraftingOutputContainer;
	public final RestrictedContainer PartCraftingInputContainer;
	public final RestrictedContainer PartCraftingOutputContainer;
	public final RestrictedContainer MaterialContainer;
	public final RestrictedContainer BatteryContainer;
	public final RestrictedContainer FuelContainer;
	private EnergyStorage EnergyStorage;
	private static final double INTERACT_RANGE = 5.0d;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_FORGE_ENERGY = 2;

	public static final int DATA_CRAFT_QUEUE_COUNT = 3;
	public static final int DATA_CRAFT_TIME = 4;
	public static final int DATA_CRAFT_DURATION = 5;
	public static final int DATA_CRAFT_SELECTION = 6;

	public static final int NUM_DATA_MEMBERS = 7;

	public static final int CRAFTING_NO_PART = -1;

	public int LitTime = 0;
	public int LitDuration = 0;

	public int CraftTime = 0;
	public int CraftDuration = 0;
	public int CraftQueueCount = 0;
	public int CraftingPart = CRAFTING_NO_PART;

	protected final ContainerData DataAccess = new ContainerData()
	{
		@Override
		public int get(int id)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { return LitTime; }
				case DATA_LIT_DURATION -> { return LitDuration; }
				case DATA_FORGE_ENERGY -> { return EnergyStorage == null ? 0 : EnergyStorage.getEnergyStored(); }
				case DATA_CRAFT_QUEUE_COUNT -> { return CraftQueueCount; }
				case DATA_CRAFT_TIME -> { return CraftTime; }
				case DATA_CRAFT_DURATION -> { return CraftDuration; }
				case DATA_CRAFT_SELECTION -> { return CraftingPart; }
				default -> { return 0; }
			}
		}

		@Override
		public void set(int id, int value)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> LitTime = value;
				case DATA_LIT_DURATION -> LitDuration = value;
				case DATA_FORGE_ENERGY -> { if(EnergyStorage != null) EnergyStorage.receiveEnergy(value - EnergyStorage.getEnergyStored(), false); }
				case DATA_CRAFT_QUEUE_COUNT -> CraftQueueCount = value;
				case DATA_CRAFT_TIME -> CraftTime = value;
				case DATA_CRAFT_DURATION -> CraftDuration = value;
				case DATA_CRAFT_SELECTION -> CraftingPart = value;
			}
		}

		@Override
		public int getCount() { return NUM_DATA_MEMBERS; }
	};

	// As a container
	@Override
	public int getContainerSize()
	{
		return GunContainer.getContainerSize()
			+ PaintCanContainer.getContainerSize()
			+ MagUpgradeContainer.getContainerSize()
			+ GunCraftingInputContainer.getContainerSize()
			+ GunCraftingOutputContainer.getContainerSize()
			+ PartCraftingInputContainer.getContainerSize()
			+ PartCraftingOutputContainer.getContainerSize()
			+ MaterialContainer.getContainerSize()
			+ FuelContainer.getContainerSize()
			+ BatteryContainer.getContainerSize();
	}
	@Override
	public boolean isEmpty()
	{
		return GunContainer.isEmpty()
			&& PaintCanContainer.isEmpty()
			&& MagUpgradeContainer.isEmpty()
			&& GunCraftingInputContainer.isEmpty()
			&& GunCraftingOutputContainer.isEmpty()
			&& PartCraftingInputContainer.isEmpty()
			&& PartCraftingOutputContainer.isEmpty()
			&& MaterialContainer.isEmpty()
			&& FuelContainer.isEmpty()
			&& BatteryContainer.isEmpty();
	}
	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return GunContainer.stillValid(player)
			&& PaintCanContainer.stillValid(player)
			&& MagUpgradeContainer.stillValid(player)
			&& GunCraftingInputContainer.stillValid(player)
			&& GunCraftingOutputContainer.stillValid(player)
			&& PartCraftingInputContainer.stillValid(player)
			&& PartCraftingOutputContainer.stillValid(player)
			&& MaterialContainer.stillValid(player)
			&& FuelContainer.stillValid(player)
			&& BatteryContainer.stillValid(player);
	}

	public Pair<Container, Integer> GetSubContainer(int slot)
	{
		if(slot < GunContainer.getContainerSize())
			return Pair.of(GunContainer, slot);
		else slot -= GunContainer.getContainerSize();

		if(slot < PaintCanContainer.getContainerSize())
			return Pair.of(PaintCanContainer, slot);
		else slot -= PaintCanContainer.getContainerSize();

		if(slot < MagUpgradeContainer.getContainerSize())
			return Pair.of(MagUpgradeContainer, slot);
		else slot -= MagUpgradeContainer.getContainerSize();

		if(slot < GunCraftingInputContainer.getContainerSize())
			return Pair.of(GunCraftingInputContainer, slot);
		else slot -= GunCraftingInputContainer.getContainerSize();

		if(slot < GunCraftingOutputContainer.getContainerSize())
			return Pair.of(GunCraftingOutputContainer, slot);
		else slot -= GunCraftingOutputContainer.getContainerSize();

		if(slot < PartCraftingInputContainer.getContainerSize())
			return Pair.of(PartCraftingInputContainer, slot);
		else slot -= PartCraftingInputContainer.getContainerSize();

		if(slot < PartCraftingOutputContainer.getContainerSize())
			return Pair.of(PartCraftingOutputContainer, slot);
		else slot -= PartCraftingOutputContainer.getContainerSize();

		if(slot < MaterialContainer.getContainerSize())
			return Pair.of(MaterialContainer, slot);
		else slot -= MaterialContainer.getContainerSize();

		if(slot < FuelContainer.getContainerSize())
			return Pair.of(FuelContainer, slot);
		else slot -= FuelContainer.getContainerSize();

		if(slot < BatteryContainer.getContainerSize())
			return Pair.of(BatteryContainer, slot);
		else slot -= BatteryContainer.getContainerSize();

		// Huh?
		return Pair.of(null, Inventory.NOT_FOUND_INDEX);
	}

	@Override
	@Nonnull
	public ItemStack getItem(int slot)
	{
		Pair<Container, Integer> adjustedSlot = GetSubContainer(slot);
		if(adjustedSlot.getSecond() == Inventory.NOT_FOUND_INDEX)
			return ItemStack.EMPTY;
		return adjustedSlot.getFirst().getItem(adjustedSlot.getSecond());
	}

	@Override
	@Nonnull
	public ItemStack removeItem(int slot, int count)
	{
		Pair<Container, Integer> adjustedSlot = GetSubContainer(slot);
		if(adjustedSlot.getSecond() == Inventory.NOT_FOUND_INDEX)
			return ItemStack.EMPTY;
		return adjustedSlot.getFirst().removeItem(adjustedSlot.getSecond(), count);
	}

	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int slot)
	{
		Pair<Container, Integer> adjustedSlot = GetSubContainer(slot);
		if(adjustedSlot.getSecond() == Inventory.NOT_FOUND_INDEX)
			return ItemStack.EMPTY;
		return adjustedSlot.getFirst().removeItemNoUpdate(adjustedSlot.getSecond());
	}

	@Override
	public void setItem(int slot, @Nonnull ItemStack stack)
	{
		Pair<Container, Integer> adjustedSlot = GetSubContainer(slot);
		if(adjustedSlot.getSecond() != Inventory.NOT_FOUND_INDEX)
			adjustedSlot.getFirst().setItem(adjustedSlot.getSecond(), stack);
	}



	public static class WorkbenchBlockEntityTypeHolder implements BlockEntityType.BlockEntitySupplier<WorkbenchBlockEntity>
	{
		private final ResourceLocation DefLoc;

		public WorkbenchBlockEntityTypeHolder(ResourceLocation defLoc)
		{
			DefLoc = defLoc;
		}

		@Override
		@Nonnull
		public WorkbenchBlockEntity create(@Nonnull BlockPos pos, @Nonnull BlockState state)
		{
			return new WorkbenchBlockEntity(DefLoc, pos, state);
		}

		public BlockEntityType<WorkbenchBlockEntity> CreateType()
		{
			return new BlockEntityType<WorkbenchBlockEntity>(this, Set.of(), null)
			{
				@Override
				public boolean isValid(@Nonnull BlockState state) { return state.getBlock() instanceof WorkbenchBlock; }
			};
		}
	}

	public WorkbenchBlockEntity(ResourceLocation defLoc, BlockPos pos, BlockState state)
	{
		super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(defLoc), pos, state);
		Def = FlansMod.WORKBENCHES.Get(defLoc);

		if(Def.gunModifying.isActive)
		{
			GunContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				1,
				(stack) -> { return stack.getItem() instanceof GunItem; }
			);
			PaintCanContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> { return stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get(); }
			);
			MagUpgradeContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> { return stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get(); }
			);
		}
		else
		{
			GunContainer = new RestrictedContainer(this);
			PaintCanContainer = new RestrictedContainer(this);
			MagUpgradeContainer = new RestrictedContainer(this);
		}

		if(Def.gunCrafting.isActive)
		{
			GunCraftingInputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.gunCrafting.GetMaxInputSlots(),
				64,
				(stack) -> { return true; } );
			GunCraftingOutputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				ItemStack::isEmpty );
		}
		else
		{
			GunCraftingInputContainer = new RestrictedContainer(this);
			GunCraftingOutputContainer = new RestrictedContainer(this);
		}

		if(Def.partCrafting.isActive)
		{
			PartCraftingInputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.partCrafting.inputSlots,
				64,
				(stack) -> { return true; } );
			PartCraftingOutputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.partCrafting.outputSlots,
				64,
				ItemStack::isEmpty);
		}
		else
		{
			PartCraftingInputContainer = new RestrictedContainer(this);
			PartCraftingOutputContainer = new RestrictedContainer(this);
		}

		if(Def.itemHolding.slots.length > 0)
		{
			MaterialContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.itemHolding.slots.length,
				Def.itemHolding.maxStackSize,
				(stack) -> { return true; }
			);
		}
		else MaterialContainer = new RestrictedContainer(this);

		if(Def.energy.maxFE > 0)
		{
			EnergyStorage = new EnergyStorage(Def.energy.maxFE);
			BatteryContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numBatterySlots,
				Def.energy.batterySlotStackSize,
				(stack) -> { return stack.getCapability(ForgeCapabilities.ENERGY).isPresent(); }
			);
			FuelContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numSolidFuelSlots,
				64,
				(stack) -> { return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0f; }
			);
		}
		else
		{
			BatteryContainer = new RestrictedContainer(this);
			FuelContainer = new RestrictedContainer(this);
		}
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("gun", GunContainer.save(new CompoundTag()));
		tags.put("paintcans", PaintCanContainer.save(new CompoundTag()));
		tags.put("magupgrades", MagUpgradeContainer.save(new CompoundTag()));
		tags.put("gun_input", GunCraftingInputContainer.save(new CompoundTag()));
		tags.put("gun_output", GunCraftingOutputContainer.save(new CompoundTag()));
		tags.put("part_input", PartCraftingInputContainer.save(new CompoundTag()));
		tags.put("part_output", PartCraftingOutputContainer.save(new CompoundTag()));
		tags.put("materials", MaterialContainer.save(new CompoundTag()));
		tags.put("battery", BatteryContainer.save(new CompoundTag()));
		tags.put("fuel", FuelContainer.save(new CompoundTag()));
		if(EnergyStorage != null)
			tags.put("energy", EnergyStorage.serializeNBT());

		tags.putInt("lit_time", LitTime);
		tags.putInt("lit_duration", LitDuration);
		tags.putInt("craft_queue", CraftQueueCount);
		tags.putInt("craft_duration", CraftDuration);
		tags.putInt("craft_time", CraftTime);
		tags.putInt("craft_selection", CraftingPart);
	}

	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags.getCompound("gun"));
		PaintCanContainer.load(tags.getCompound("paintcans"));
		MagUpgradeContainer.load(tags.getCompound("magupgrades"));
		GunCraftingInputContainer.load(tags.getCompound("gun_input"));
		GunCraftingOutputContainer.load(tags.getCompound("gun_output"));
		PartCraftingInputContainer.load(tags.getCompound("part_input"));
		PartCraftingOutputContainer.load(tags.getCompound("part_output"));
		MaterialContainer.load(tags.getCompound("materials"));
		BatteryContainer.load(tags.getCompound("battery"));
		FuelContainer.load(tags.getCompound("fuel"));
		if(EnergyStorage != null)
			EnergyStorage.deserializeNBT(tags.getCompound("energy"));

		LitTime = tags.getInt("lit_time");
		LitDuration = tags.getInt("lit_duration");
		CraftQueueCount = tags.getInt("craft_queue");
		CraftDuration = tags.getInt("craft_duration");
		CraftTime = tags.getInt("craft_time");
		CraftingPart = tags.getInt("craft_selection");
	}

	private static final Component DISPLAY_NAME = Component.translatable("workbench.title");
	@Override
	@Nonnull
	public Component getDisplayName() { return DISPLAY_NAME; }

	@Override
	public void clearContent()
	{
		GunContainer.clearContent();
		PaintCanContainer.clearContent();
		MagUpgradeContainer.clearContent();
		GunCraftingInputContainer.clearContent();
		GunCraftingOutputContainer.clearContent();
		PartCraftingInputContainer.clearContent();
		PartCraftingOutputContainer.clearContent();
		MaterialContainer.clearContent();
		BatteryContainer.clearContent();
		FuelContainer.clearContent();
	}


	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, @Nonnull Inventory inventory, @Nonnull Player player)
	{
		return new WorkbenchMenu(containerID,
			inventory,
			Def,
			this,
			GunContainer,
			PaintCanContainer,
			MagUpgradeContainer,
			GunCraftingInputContainer,
			GunCraftingOutputContainer,
			PartCraftingInputContainer,
			PartCraftingOutputContainer,
			MaterialContainer,
			BatteryContainer,
			FuelContainer,
			DataAccess);
	}

	public static void serverTick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull WorkbenchBlockEntity workbench)
	{
		if(workbench.EnergyStorage != null)
		{
			if (workbench.LitTime > 0)
			{
				workbench.LitTime--;
				workbench.EnergyStorage.receiveEnergy(workbench.Def.energy.solidFEPerFuelTime, false);
				if (workbench.LitTime <= 0)
				{
					workbench.LitDuration = 0;
					setChanged(level, pos, state);
				}
			}

			if (workbench.LitTime <= 0 && workbench.EnergyStorage.getEnergyStored() < workbench.EnergyStorage.getMaxEnergyStored())
			{
				if (workbench.FuelContainer.getContainerSize() > 0)
				{
					int burnTime = ForgeHooks.getBurnTime(workbench.FuelContainer.getItem(0), RecipeType.SMELTING);
					if (burnTime > 0)
					{
						workbench.FuelContainer.removeItem(0, 1);
						workbench.LitDuration = burnTime;
						workbench.LitTime = burnTime;
						setChanged(level, pos, state);
					}
				}
			}
		}

		if(workbench.PartCraftingOutputContainer.getContainerSize() > 0)
		{
			if(workbench.CraftTime > 0)
			{
				workbench.CraftTime--;
				if(workbench.CraftTime <= 0)
				{
					workbench.CraftDuration = 0;
					boolean success = workbench.CraftOnePart();
					if(success)
						setChanged(level, pos, state);
				}
			}

			// Then try find a new craft
			if(workbench.CraftTime <= 0 && workbench.CraftQueueCount > 0)
			{
				int inputCount = workbench.GetMaxPartsCraftableFromInput();
				boolean canOutput = workbench.GetOutputSlotToCraftPart() != Inventory.NOT_FOUND_INDEX;
				boolean canPower = true;
				if(workbench.Def.partCrafting.FECostPerCraft > 0)
				{
					canPower = workbench.EnergyStorage.getEnergyStored() >= workbench.Def.partCrafting.FECostPerCraft;
				}
				if(inputCount > 0 && canOutput && canPower)
				{
					workbench.CraftTime = Maths.Floor(workbench.Def.partCrafting.timePerCraft * 20);
					workbench.CraftDuration = workbench.CraftTime;
					workbench.CraftQueueCount--;
					setChanged(level, pos, state);
				}
			}
		}
	}

	public boolean RecipeCanBeCraftedInThisWorkbench(ItemStack output)
	{
		for(ItemStack stack : Def.partCrafting.GetAllOutputs())
			if(stack.sameItem(output))
				return true;
		return false;
	}

	@Nonnull
	public List<PartFabricationRecipe> GetAllRecipes()
	{
		return Def.partCrafting.GetAllRecipes(level);
	}


	public void SelectPartCraftingRecipe( int index)
	{
		// Can't change the selected recipe while one is in progress.
		if(CraftQueueCount > 0)
			return;

		if(index < GetAllRecipes().size())
			CraftingPart = index;
	}

	public int GetMaxPartsCraftableFromInput()
	{
		if(CraftingPart == CRAFTING_NO_PART)
			return 0;

		if(CraftingPart >= GetAllRecipes().size())
			return 0;

		PartFabricationRecipe recipe = GetAllRecipes().get(CraftingPart);
		if(recipe == null)
			return 0;

		ItemStack[] copyStacks = new ItemStack[PartCraftingInputContainer.getContainerSize()];
		for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
		{
			copyStacks[i] = PartCraftingInputContainer.getItem(i).copy();
		}

		int amountCanCraft = 0;
		while(amountCanCraft < 99)
		{
			for (Ingredient ingredient : recipe.InputIngredients)
			{
				for (ItemStack copyStack : copyStacks)
				{
					if (ingredient.test(copyStack))
					{
						// TODO: Non-1 amount?
						copyStack.setCount(copyStack.getCount() - 1);
						break;
					}
				}
			}

			// All ingredients were successfully consumed from our copied inventory
			// Tally up and try another
			amountCanCraft++;
		}

		return amountCanCraft;
	}

	public int GetOutputSlotToCraftPart()
	{
		if(CraftingPart == CRAFTING_NO_PART)
			return Inventory.NOT_FOUND_INDEX;

		if(CraftingPart >= GetAllRecipes().size())
			return Inventory.NOT_FOUND_INDEX;

		PartFabricationRecipe recipe = GetAllRecipes().get(CraftingPart);
		if(recipe == null)
			return 0;

		ItemStack result = recipe.Result;
		// First pass, find a similar item to stack with
		for(int i = 0; i < PartCraftingOutputContainer.getContainerSize(); i++)
		{
			ItemStack stackInSlot = PartCraftingOutputContainer.getItem(i);
			if(result.sameItem(stackInSlot))
				if(stackInSlot.getCount() < PartCraftingOutputContainer.getMaxStackSize())
					return i;
		}

		// Second pass, find an empty slot
		for(int i = 0; i < PartCraftingOutputContainer.getContainerSize(); i++)
		{
			if(PartCraftingOutputContainer.getItem(i).isEmpty())
				return i;
		}

		return Inventory.NOT_FOUND_INDEX;
	}

	public void QueueCrafting(int count)
	{
		CraftQueueCount += count;
	}

	public void CancelQueue()
	{
		CraftQueueCount = 0;
	}

	public boolean CraftOnePart()
	{
		int outputSlot = GetOutputSlotToCraftPart();
		if(outputSlot != Inventory.NOT_FOUND_INDEX)
		{
			PartFabricationRecipe recipe = GetAllRecipes().get(CraftingPart);
			ItemStack result = recipe.Result;
			ItemStack existing = PartCraftingOutputContainer.getItem(outputSlot);
			if(existing.isEmpty())
				PartCraftingOutputContainer.setItem(outputSlot, result);
			else if(existing.sameItem(result))
				existing.setCount(existing.getCount() + result.getCount());
			else
				FlansMod.LOGGER.error("CraftOnePart tried to craft into invalid slot " + outputSlot);
			return true;
		}
		return false;
	}

	public static int GetPaintUpgradeCost(Container gunContainer, int paintIndex)
	{
		if (gunContainer.getContainerSize() <= 0)
			return 0;
		if(paintIndex == 0)
			return 0;

		if (gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
		{
			PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
			if (paintableDefinition.IsValid())
			{
				if (0 <= paintIndex - 1 && paintIndex - 1 < paintableDefinition.paintjobs.length)
				{
					return paintableDefinition.paintjobs[paintIndex - 1].paintBucketsRequired;
				}
			}
		}
		return 0;
	}
	public static boolean CanPaintGun(Player player, Container gunContainer, Container paintCanContainer, int skinIndex)
	{
		// Check some obvious errors
		if(skinIndex < 0)
			return false;
		if (gunContainer.getContainerSize() <= 0)
			return false;
		if(gunContainer.getItem(0).isEmpty())
			return false;
		if(gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
		{
			// We can always revert to the default skin, no matter what, and for free.
			if(skinIndex == 0)
				return true;

			ItemStack gunStack = gunContainer.getItem(0);

			// Check if the item is paintable and our selection makes sense
			PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
			if (!paintableDefinition.IsValid())
				return false;
			if (skinIndex >= paintableDefinition.paintjobs.length + 1)
				return false;
			PaintjobDefinition paintjobDefinition = paintableDefinition.paintjobs[skinIndex - 1];
			if (paintjobDefinition == null)
				return false;

			// If this skin has entitlements, check them out
			if (!paintjobDefinition.entitlementKey.isEmpty())
			{
				// TODO: Entitlement checks
			}

			// Creative players get to skip the cost checks
			if (player.isCreative())
				return true;

			// Now we just need to check the cost
			int paintCost = paintjobDefinition.paintBucketsRequired;
			if (paintCanContainer.getItem(0).getCount() < paintCost)
				return false;

			// All checks passed, let's paint!
			return true;
		}
		return false;
	}

	public void PaintGun(@Nonnull Player player, int skinIndex) { PaintGun(player, GunContainer, PaintCanContainer, skinIndex); }
	public static void PaintGun(@Nonnull Player player, @Nonnull Container gunContainer, @Nonnull Container paintCanContainer, int skinIndex)
	{
		if(CanPaintGun(player, gunContainer, paintCanContainer, skinIndex))
		{
			if (gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = gunContainer.getItem(0);
				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if (paintableDefinition.IsValid())
				{
					if (skinIndex == 0)
					{
						flanItem.SetPaintjobName(gunStack, "default");
					} else
					{
						PaintjobDefinition paintjobDefinition = paintableDefinition.paintjobs[skinIndex - 1];
						paintCanContainer.getItem(0).setCount(paintCanContainer.getItem(0).getCount() - paintjobDefinition.paintBucketsRequired);
						flanItem.SetPaintjobName(gunStack, paintjobDefinition.textureName);
					}
				}
			}
		}
	}

	public static int GetMagUpgradeCost(@Nonnull Container gunContainer, int magIndex)
	{
		if (gunContainer.getContainerSize() <= 0)
			return 0;

		if (gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
		{
			List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
			if (0 <= magIndex && magIndex < mags.size())
			{
				return gunItem.Def().primaryMagazines.baseCostToSwap + mags.get(magIndex).upgradeCost;
			}
			return gunItem.Def().primaryMagazines.baseCostToSwap;
		}
		return 0;
	}
	public static boolean CanSelectMagazine(@Nonnull Player player, @Nonnull Container gunContainer, @Nonnull Container magUpgradeContainer, int magIndex)
	{
		// Check some obvious errors
		if(magIndex < 0)
			return false;
		if (gunContainer.getContainerSize() <= 0)
			return false;
		if(gunContainer.getItem(0).isEmpty())
			return false;
		if(gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
		{
			ItemStack gunStack = gunContainer.getItem(0);

			// Check if the item has mag options and our selection makes sense
			List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
			if(magIndex >= mags.size())
				return false;

			MagazineDefinition mag = mags.get(magIndex);
			if(!mag.IsValid())
				return false;

			// Creative players get to skip the cost checks
			if (player.isCreative())
				return true;

			// Now we just need to check the cost
			int magCost = mag.upgradeCost + gunItem.Def().primaryMagazines.baseCostToSwap;
			if (magUpgradeContainer.getItem(0).getCount() < magCost)
				return false;

			// All checks passed, let's swap the mags!
			return true;
		}
		return false;
	}
	public void SelectMagazine(@Nonnull Player player, int magIndex) { SelectMagazine(player, GunContainer, MagUpgradeContainer, magIndex); }
	public static void SelectMagazine(@Nonnull Player player, @Nonnull Container gunContainer, @Nonnull Container magUpgradeContainer, int magIndex)
	{
		if(CanSelectMagazine(player, gunContainer, magUpgradeContainer, magIndex))
		{
			if (gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
			{
				ItemStack gunStack = gunContainer.getItem(0);
				List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
				if (0 <= magIndex && magIndex < mags.size())
				{
					int magCost = mags.get(magIndex).upgradeCost + gunItem.Def().primaryMagazines.baseCostToSwap;
					magUpgradeContainer.getItem(0).setCount(magUpgradeContainer.getItem(0).getCount() - magCost);
					gunItem.SetMagazineType(gunStack, EActionInput.PRIMARY, 0, mags.get(magIndex));
				}
				else
				{
					FlansMod.LOGGER.warn(player.getName().getString() + " tried to set mag index " + magIndex + " on gun (" + gunItem.Def().Location + ") with only " + mags.size() + " mag options");
				}
			}
		}
	}
}
