package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextCache;
import com.flansmod.common.crafting.menus.*;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.crafting.EWorkbenchInventoryType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.WorkbenchIOSettingDefinition;
import com.flansmod.common.types.crafting.elements.WorkbenchSideDefinition;
import com.flansmod.common.types.elements.MaterialSourceDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkbenchBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, Clearable, ICapabilityProvider
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
	private final LazyOptional<IEnergyStorage> EnergyStorageLazyOptional;
	private ItemCapabilityMultiContainer[] DirectionalItemCaps;
	private final List<LazyOptional<IItemHandler>> DirectionalItemCapLazyOptionals;


	private static final double INTERACT_RANGE = 5.0d;
	public static final int NUM_CRAFTING_QUEUE_SLOTS = 4;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_FORGE_ENERGY = 2;



	public static final int DATA_CRAFT_TIME = 3;
	public static final int DATA_CRAFT_DURATION = 4;

	public static final int DATA_CRAFT_QUEUE_COUNT_0 = 5;
	public static final int DATA_CRAFT_QUEUE_COUNT_MAX = DATA_CRAFT_QUEUE_COUNT_0 + NUM_CRAFTING_QUEUE_SLOTS;

	public static final int DATA_CRAFT_SELECTION_0 = DATA_CRAFT_QUEUE_COUNT_MAX + 1;
	public static final int DATA_CRAFT_SELECTION_MAX = DATA_CRAFT_SELECTION_0 + NUM_CRAFTING_QUEUE_SLOTS;

	public static final int NUM_DATA_MEMBERS = DATA_CRAFT_SELECTION_MAX + 1;

	public static final int CRAFTING_NO_PART = -1;

	public int LitTime = 0;
	public int LitDuration = 0;

	public int CraftTime = 0;
	public int CraftDuration = 0;

	public int PlayerSelectedCraftingPart = CRAFTING_NO_PART;
	public int[] CraftQueueCount = new int[NUM_CRAFTING_QUEUE_SLOTS];
	public int[] CraftingPart = new int[] { CRAFTING_NO_PART, CRAFTING_NO_PART, CRAFTING_NO_PART, CRAFTING_NO_PART }; // new int[NUM_CRAFTING_QUEUE_SLOTS];

	public final ContainerData DataAccess = new ContainerData()
	{
		@Override
		public int get(int id)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { return LitTime; }
				case DATA_LIT_DURATION -> { return LitDuration; }
				case DATA_FORGE_ENERGY -> { return EnergyStorage == null ? 0 : EnergyStorage.getEnergyStored(); }
				case DATA_CRAFT_TIME -> { return CraftTime; }
				case DATA_CRAFT_DURATION -> { return CraftDuration; }
				default ->
				{
					if(DATA_CRAFT_SELECTION_0 <= id && id < DATA_CRAFT_SELECTION_MAX)
					{
						return CraftingPart[id - DATA_CRAFT_SELECTION_0];
					}
					if(DATA_CRAFT_QUEUE_COUNT_0 <= id && id < DATA_CRAFT_QUEUE_COUNT_MAX)
					{
						return CraftQueueCount[id - DATA_CRAFT_QUEUE_COUNT_0];
					}
					return 0;
				}
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
				case DATA_CRAFT_TIME -> CraftTime = value;
				case DATA_CRAFT_DURATION -> CraftDuration = value;
				default ->
				{
					if(DATA_CRAFT_SELECTION_0 <= id && id < DATA_CRAFT_SELECTION_MAX)
					{
						CraftingPart[id - DATA_CRAFT_SELECTION_0] = value;
					}
					if(DATA_CRAFT_QUEUE_COUNT_0 <= id && id < DATA_CRAFT_QUEUE_COUNT_MAX)
					{
						CraftQueueCount[id - DATA_CRAFT_QUEUE_COUNT_0] = value;
					}
				}
			}
		}

		@Override
		public int getCount() { return NUM_DATA_MEMBERS; }
	};

	public WorkbenchBlockEntity(ResourceLocation defLoc, BlockPos pos, BlockState state)
	{
		super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(defLoc), pos, state);
		Def = FlansMod.WORKBENCHES.Get(defLoc);

		DirectionalItemCapLazyOptionals = new ArrayList<>(6);
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.DOWN)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.UP)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.NORTH)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.SOUTH)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.WEST)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.EAST)));
		EnergyStorageLazyOptional = LazyOptional.of(() -> EnergyStorage );

		if(Def.gunModifying.isActive)
		{
			GunContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				1,
				(stack) -> stack.getItem() instanceof GunItem
			);
			PaintCanContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get()
			);
			MagUpgradeContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get()
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
				(stack) -> true);
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
				(stack) -> true);
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
				(stack) -> true
			);
		}
		else MaterialContainer = new RestrictedContainer(this);

		if(Def.energy.maxFE > 0)
		{
			EnergyStorage = new EnergyStorage(Def.energy.maxFE, Def.energy.acceptFEPerTick, Def.energy.disperseFEPerTick);
			BatteryContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numBatterySlots,
				Def.energy.batterySlotStackSize,
				(stack) -> stack.getCapability(ForgeCapabilities.ENERGY).isPresent()
			);
			FuelContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numSolidFuelSlots,
				64,
				(stack) -> ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0f
			);
		}
		else
		{
			EnergyStorage = null;
			BatteryContainer = new RestrictedContainer(this);
			FuelContainer = new RestrictedContainer(this);
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
			return new BlockEntityType<>(this, Set.of(), null)
			{
				@Override
				public boolean isValid(@Nonnull BlockState state)
				{
					return state.getBlock() instanceof WorkbenchBlock;
				}
			};
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
		for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS; i++)
		{
			tags.putInt("craft_queue_"+i, CraftQueueCount[i]);
			tags.putInt("craft_selection_"+i, CraftingPart[i]);
		}

		tags.putInt("craft_duration", CraftDuration);
		tags.putInt("craft_time", CraftTime);
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

		CraftDuration = tags.getInt("craft_duration");
		CraftTime = tags.getInt("craft_time");

		for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS; i++)
		{
			CraftQueueCount[i] = tags.getInt("craft_queue_"+i);
			CraftingPart[i] = tags.getInt("craft_selection_"+i);
			if(CraftQueueCount[i] == 0)
				CraftingPart[i] = CRAFTING_NO_PART;
		}
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
		if(Def.gunCrafting.isActive)
			return new WorkbenchMenuGunCrafting(containerID, inventory, this);
		if(Def.partCrafting.isActive)
			return new WorkbenchMenuPartCrafting(containerID, inventory, this);
		if(Def.gunModifying.isActive)
			return new WorkbenchMenuModification(containerID, inventory, this);
		if(Def.energy.maxFE > 0.0f)
			return new WorkbenchMenuPower(containerID, inventory, this);

		return new WorkbenchMenuMaterials(containerID, inventory, this);
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
			if(workbench.CraftTime <= 0)
			{
				// If the thing in slot 0 is done, advance the stack
				if(workbench.CraftQueueCount[0] <= 0)
				{
					for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS - 1; i++)
					{
						workbench.CraftQueueCount[i] = workbench.CraftQueueCount[i + 1];
						workbench.CraftingPart[i] = workbench.CraftingPart[i+1];
					}
					workbench.CraftQueueCount[NUM_CRAFTING_QUEUE_SLOTS - 1] = 0;
					workbench.CraftingPart[NUM_CRAFTING_QUEUE_SLOTS - 1] = CRAFTING_NO_PART;
				}

				// Then, if there's something to craft in slot 0, start crafting one
				if(workbench.CraftQueueCount[0] > 0)
				{
					int inputCount = workbench.GetMaxPartsCraftableFromInput(workbench.CraftingPart[0]);
					boolean canOutput = workbench.GetOutputSlotToCraftPart() != Inventory.NOT_FOUND_INDEX;
					boolean canPower = true;
					if (workbench.Def.partCrafting.FECostPerCraft > 0)
					{
						canPower = workbench.EnergyStorage.getEnergyStored() >= workbench.Def.partCrafting.FECostPerCraft;
					}
					if (inputCount > 0 && canOutput && canPower)
					{
						workbench.CraftTime = Maths.Floor(workbench.Def.partCrafting.timePerCraft * 20);
						workbench.CraftDuration = workbench.CraftTime;
						setChanged(level, pos, state);
					}
				}
			}
		}
	}

	public boolean RecipeCanBeCraftedInThisWorkbench(ItemStack output)
	{
		for(ItemStack stack : Def.partCrafting.GetAllOutputs())
			if(ItemStack.isSameItem(stack, output))
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
		if(0 <= index && index < GetAllRecipes().size())
			PlayerSelectedCraftingPart = index;
		else
			PlayerSelectedCraftingPart = CRAFTING_NO_PART;
	}

	public int GetMaxPartsCraftableFromInput(int recipeIndex)
	{
		if(recipeIndex == CRAFTING_NO_PART)
			return 0;

		if(recipeIndex >= GetAllRecipes().size())
			return 0;

		PartFabricationRecipe recipe = GetAllRecipes().get(recipeIndex);
		if(recipe == null)
			return 0;

		int lowestMatch = Integer.MAX_VALUE;
		for (int i = 0; i < recipe.getIngredients().size(); i++)
		{
			Ingredient ingredient = recipe.getIngredients().get(i);
			int required;
			int matching = CountInputMatching(ingredient);
			if (ingredient instanceof StackedIngredient stacked)
				required = stacked.Count;
			else
				required = 1;

			int maxProduction = matching / required;
			if(maxProduction < lowestMatch)
				lowestMatch = maxProduction;
		}

		return Maths.Clamp(lowestMatch, 0, 999);
	}


	public int[] GetQuantityOfEachIngredientForRecipe(int recipeIndex)
	{
		if(recipeIndex == CRAFTING_NO_PART || recipeIndex >= GetAllRecipes().size())
			return new int[0];

		PartFabricationRecipe recipe = GetAllRecipes().get(recipeIndex);
		if(recipe == null)
			return new int[0];

		int[] matching = new int[recipe.getIngredients().size()];
		for (int i = 0; i < recipe.getIngredients().size(); i++)
		{
			Ingredient ingredient = recipe.getIngredients().get(i);
			matching[i] = CountInputMatching(ingredient);
		}
		return matching;
	}

	public int[] GetRequiredOfEachIngredientForRecipe(int recipeIndex)
	{
		if(recipeIndex == CRAFTING_NO_PART || recipeIndex >= GetAllRecipes().size())
			return new int[0];

		PartFabricationRecipe recipe = GetAllRecipes().get(recipeIndex);
		if(recipe == null)
			return new int[0];

		int[] required = new int[recipe.getIngredients().size()];
		for (int i = 0; i < recipe.getIngredients().size(); i++)
		{
			Ingredient ingredient = recipe.getIngredients().get(i);
			if(ingredient instanceof StackedIngredient stacked)
				required[i] = stacked.Count;
			else required[i] = 1;
		}
		return required;
	}

	public int GetOutputSlotToCraftPart()
	{
		if(CraftingPart[0] == CRAFTING_NO_PART)
			return Inventory.NOT_FOUND_INDEX;

		if(CraftingPart[0] >= GetAllRecipes().size())
			return Inventory.NOT_FOUND_INDEX;

		PartFabricationRecipe recipe = GetAllRecipes().get(CraftingPart[0]);
		if(recipe == null)
			return 0;

		ItemStack result = recipe.Result;
		// First pass, find a similar item to stack with
		for(int i = 0; i < PartCraftingOutputContainer.getContainerSize(); i++)
		{
			ItemStack stackInSlot = PartCraftingOutputContainer.getItem(i);
			if(ItemStack.isSameItem(result, stackInSlot))
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
		for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS; i++)
		{
			if (CraftingPart[i] == PlayerSelectedCraftingPart || CraftingPart[i] == CRAFTING_NO_PART)
			{
				CraftingPart[i] = PlayerSelectedCraftingPart;
				if (count == -1)
				{
					CraftQueueCount[i] = GetMaxPartsCraftableFromInput(PlayerSelectedCraftingPart);
					return;
				} else
				{
					CraftQueueCount[i] += count;
					return;
				}
			}
		}
	}

	public void CancelQueue(int index)
	{
		if(0 <= index && index < NUM_CRAFTING_QUEUE_SLOTS)
		{
			CraftQueueCount[index] = (index == 0 && CraftDuration > 0) ? 1 : 0;
			if(index > 0)
			{
				for(int i = index; i < NUM_CRAFTING_QUEUE_SLOTS - 1; i++)
				{
					CraftQueueCount[i] = CraftQueueCount[i + 1];
					CraftingPart[i] = CraftingPart[i + 1];
				}
				CraftingPart[NUM_CRAFTING_QUEUE_SLOTS - 1] = CRAFTING_NO_PART;
				CraftQueueCount[NUM_CRAFTING_QUEUE_SLOTS - 1] = 0;
			}
		}
	}

	public int CountInputMatching(Ingredient ingredient)
	{
		int count = 0;
		for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
		{
			ItemStack stack = PartCraftingInputContainer.getItem(i);
			if(ingredient instanceof StackedIngredient stackedIngredient)
			{
				count += stackedIngredient.Count(stack);
			}
			else if(ingredient.test(stack))
				count += stack.getCount();

		}
		return count;
	}

	private void ConsumeIngredient(Ingredient ingredient)
	{
		// We can do "refunds", pretty complicated
		if(ingredient instanceof TieredMaterialIngredient tiered)
		{
			int count = 0;
			for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
			{
				int matchCount = tiered.Count(PartCraftingInputContainer.getItem(i));
				if(matchCount > 0)
				{
					count += matchCount;
					PartCraftingInputContainer.setItem(i, ItemStack.EMPTY);
				}
			}
			int countAfterConsume = count - tiered.Count;
			List<Pair<MaterialSourceDefinition, Integer>> refundSplit = tiered.MaterialType().ResolveAmount(countAfterConsume);
			for(Pair<MaterialSourceDefinition, Integer> kvp : refundSplit)
			{
				if(kvp.getFirst().GetMatches().size() > 0)
				{
					ItemStack leftover = TryAddStackToCraftingInput(kvp.getFirst().GetMatches().get(0).copyWithCount(kvp.getSecond()));
					if (!leftover.isEmpty() && level != null)
					{
						ItemEntity leftoverEntity = new ItemEntity(
							level,
							getBlockPos().getCenter().x,
							getBlockPos().getCenter().y,
							getBlockPos().getCenter().z,
							leftover);
						level.addFreshEntity(leftoverEntity);
					}
				}
				else
					FlansMod.LOGGER.error("Could not refund " + kvp.getSecond() + "x " + kvp.getFirst());

			}
		}
	}

	private ItemStack TryAddStackToCraftingInput(ItemStack stack)
	{
		int maxStack = PartCraftingInputContainer.getMaxStackSize();
		for(int i = 0; i < PartCraftingInputContainer.getContainerSize(); i++)
		{
			ItemStack stackInSlot = PartCraftingInputContainer.getItem(i);
			if(stackInSlot.isEmpty())
			{
				if(stack.getCount() > maxStack)
				{
					PartCraftingInputContainer.setItem(i, stack.copyWithCount(maxStack));
					stack.setCount(stack.getCount() - maxStack);
				}
				else
				{
					PartCraftingInputContainer.setItem(i, stack.copy());
					stack.setCount(0);
				}
			}
			else if(ItemStack.isSameItem(stackInSlot, stack))
			{
				int countToAdd = Maths.Min(stack.getCount(), maxStack - stackInSlot.getCount());
				if(countToAdd > 0)
				{
					stackInSlot.setCount(stackInSlot.getCount() + countToAdd);
					stack.setCount(stack.getCount() - countToAdd);
				}
			}

			if(stack.isEmpty())
				return ItemStack.EMPTY;
		}

		return stack;
	}

	public boolean CraftOnePart()
	{
		int outputSlot = GetOutputSlotToCraftPart();
		if(outputSlot != Inventory.NOT_FOUND_INDEX)
		{
			PartFabricationRecipe recipe = GetAllRecipes().get(CraftingPart[0]);
			for(Ingredient ingredient : recipe.getIngredients())
			{
				ConsumeIngredient(ingredient);
			}

			CraftQueueCount[0]--;
			ItemStack result = recipe.Result;
			ItemStack existing = PartCraftingOutputContainer.getItem(outputSlot);
			if(existing.isEmpty())
				PartCraftingOutputContainer.setItem(outputSlot, result.copy());
			else if(ItemStack.isSameItem(existing, result))
				existing.setCount(existing.getCount() + result.getCount());
			else
				FlansMod.LOGGER.error("CraftOnePart tried to craft into invalid slot " + outputSlot);

			if(level != null)
				level.playSound(null, getBlockPos(), SoundEvents.IRON_GOLEM_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);

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
			List<MagazineDefinition> mags = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines();
			if (0 <= magIndex && magIndex < mags.size())
			{
				return gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap + mags.get(magIndex).upgradeCost;
			}
			return gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap;
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
			List<MagazineDefinition> mags = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines();
			if(magIndex >= mags.size())
				return false;

			MagazineDefinition mag = mags.get(magIndex);
			if(!mag.IsValid())
				return false;

			// Creative players get to skip the cost checks
			if (player.isCreative())
				return true;

			// Now we just need to check the cost
			int magCost = mag.upgradeCost + gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap;
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
				GunContext gunContext = GunContextCache.Get(player.level().isClientSide).Create(gunContainer, 0);
				if(gunContext.IsValid())
				{
					List<MagazineDefinition> mags = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines();
					if (0 <= magIndex && magIndex < mags.size())
					{
						int magCost = mags.get(magIndex).upgradeCost + gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap;
						magUpgradeContainer.getItem(0).setCount(magUpgradeContainer.getItem(0).getCount() - magCost);
						gunContext.GetActionGroupContext(Actions.DefaultPrimaryActionKey).SetMagazineType(0, mags.get(magIndex));
					} else
					{
						FlansMod.LOGGER.warn(player.getName().getString() + " tried to set mag index " + magIndex + " on gun (" + gunItem.Def().Location + ") with only " + mags.size() + " mag options");
					}
				}
			}
		}
	}

	public static class ItemCapabilityMultiContainer implements IItemHandler
	{
		public static final ItemCapabilityMultiContainer Invalid = new ItemCapabilityMultiContainer(new Container[] { }, false, false);

		private final Container[] Containers;
		private final boolean CanInsert;
		private final boolean CanExtract;
		public ItemCapabilityMultiContainer(Container[] containers, boolean canInsert, boolean canExtract)
		{
			Containers = containers;
			CanInsert = canInsert;
			CanExtract = canExtract;
		}
		@Override
		public int getSlots()
		{
			int count = 0;
			for (Container container : Containers)
				count += container.getContainerSize();
			return count;
		}
		private ItemStack PutStackInSlot(int slot, ItemStack stack, boolean simulate)
		{
			if(slot < 0)
				return stack;
			for(Container container : Containers)
			{
				if(slot < container.getContainerSize())
				{
					int maxCount = Maths.Min(container.getMaxStackSize(), stack.getCount());
					if(!simulate)
						container.setItem(slot, stack.copyWithCount(maxCount));
					return stack.copyWithCount(stack.getCount() - maxCount);
				}
				else
					slot -= container.getContainerSize();
			}
			return stack;
		}
		@Override
		@Nonnull
		public ItemStack getStackInSlot(int slot)
		{
			if(slot < 0)
				return ItemStack.EMPTY;
			for(Container container : Containers)
			{
				if(slot < container.getContainerSize())
					return container.getItem(slot);
				else
					slot -= container.getContainerSize();
			}
			return ItemStack.EMPTY;
		}
		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
		{
			if(!CanInsert)
				return stack;
			ItemStack existingStack = getStackInSlot(slot);
			if(existingStack.isEmpty())
			{
				return PutStackInSlot(slot, stack, simulate);
			}
			else if(ItemStack.isSameItem(existingStack, stack))
			{
				return PutStackInSlot(slot, existingStack.copyWithCount(existingStack.getCount() + stack.getCount()), simulate);
			}
			return stack;
		}
		@Override
		@Nonnull
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if(!CanExtract)
				return ItemStack.EMPTY;
			ItemStack existingStack = getStackInSlot(slot);
			if(!existingStack.isEmpty())
			{
				int amountToTake = Maths.Min(existingStack.getCount(), amount);
				ItemStack returnStack = existingStack.copyWithCount(amountToTake);
				if(!simulate)
					existingStack.setCount(existingStack.getCount() - amountToTake);
				return returnStack;
			}
			return ItemStack.EMPTY;
		}
		@Override
		public int getSlotLimit(int slot)
		{
			if(slot < 0)
				return 0;
			for(Container container : Containers)
			{
				if(slot < container.getContainerSize())
					return container.getMaxStackSize();
				else
					slot -= container.getContainerSize();
			}
			return 0;
		}
		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack)
		{
			if(slot < 0)
				return false;
			for(Container container : Containers)
			{
				if(slot < container.getContainerSize())
					return container.canPlaceItem(slot, stack);
				else
					slot -= container.getContainerSize();
			}
			return false;
		}
	}

	private Container[] GetContainers(EWorkbenchInventoryType type)
	{
		return switch (type)
		{
			case PartInput -> new Container[]{PartCraftingInputContainer};
			case PartOutput -> new Container[]{PartCraftingOutputContainer};
			case GunInput -> new Container[]{GunCraftingInputContainer};
			case GunOutput -> new Container[]{GunCraftingOutputContainer};
			case Material -> new Container[]{MaterialContainer};
			case Fuel -> new Container[]{FuelContainer};
			case Power -> new Container[]{BatteryContainer};
			case GunModification -> new Container[]{GunContainer};
			case Attachment -> new Container[]{GunContainer};
			case PaintCan -> new Container[]{PaintCanContainer};
			case MagUpgrade -> new Container[]{MagUpgradeContainer};
			case AllTypes -> new Container[]{
				PartCraftingInputContainer,
				PartCraftingOutputContainer,
				GunCraftingInputContainer,
				GunCraftingOutputContainer,
				MaterialContainer,
				FuelContainer,
				BatteryContainer,
				GunContainer,
				PaintCanContainer,
				MagUpgradeContainer,
			};
		};
	}

	@Nonnull
	public ItemCapabilityMultiContainer SupplyItemCapability(Direction worldSpaceDirection)
	{
		Direction frontFace = getBlockState().getValue(WorkbenchBlock.DIRECTION);
		Direction frontRelativeDirection = worldSpaceDirection;
		if(worldSpaceDirection == frontFace)
			frontRelativeDirection = Direction.SOUTH;
		else if(worldSpaceDirection == frontFace.getClockWise())
			frontRelativeDirection = Direction.WEST;
		else if(worldSpaceDirection == frontFace.getCounterClockWise())
			frontRelativeDirection = Direction.EAST;
		else if(worldSpaceDirection == frontFace.getOpposite())
			frontRelativeDirection = Direction.NORTH;

		// If there are custom side settings, use them
		if(Def.sides.length > 0)
		{
			WorkbenchSideDefinition sideDef = Def.GetSideDef(frontRelativeDirection);
			if(sideDef != null)
			{
				for(WorkbenchIOSettingDefinition ioSetting : sideDef.ioSettings)
				{
					return new ItemCapabilityMultiContainer(GetContainers(ioSetting.type), ioSetting.allowInput, ioSetting.allowExtract);
				}
			}
			return ItemCapabilityMultiContainer.Invalid;
		}

		// Otherwise, let's go with a few default setups based on which modules are added
		// Gun or Part crafting input and output
		if(Def.partCrafting.isActive || Def.gunCrafting.isActive)
		{
			switch(frontRelativeDirection)
			{
				case UP: 	{	return new ItemCapabilityMultiContainer(new Container[] { MaterialContainer }, true, true); }
				case DOWN: 	{ 	return new ItemCapabilityMultiContainer(new Container[] { FuelContainer }, true, true); }
				case NORTH: { 	return new ItemCapabilityMultiContainer(new Container[] { BatteryContainer }, true, true); }
				case EAST:  {	return new ItemCapabilityMultiContainer(new Container[] { PartCraftingOutputContainer, GunCraftingOutputContainer }, false, true); }
				case WEST:  { 	return new ItemCapabilityMultiContainer(new Container[] { PartCraftingInputContainer, GunCraftingInputContainer }, true, false); }
			}
		}
		// If we have ONLY item holding, show it on all sides
		else if(Def.itemHolding.slots.length > 0)
		{
			return new ItemCapabilityMultiContainer(new Container[] { MaterialContainer }, true, true);
		}

		return ItemCapabilityMultiContainer.Invalid;
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side)
	{
		if (cap == ForgeCapabilities.ITEM_HANDLER)
		{
			LazyOptional<IItemHandler> lazyOptional = DirectionalItemCapLazyOptionals.get(side.ordinal());
			if(lazyOptional != null)
				return lazyOptional.cast();
		}
		else if(cap == ForgeCapabilities.ENERGY)
		{
			return EnergyStorageLazyOptional.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		for(LazyOptional<IItemHandler> lazy : DirectionalItemCapLazyOptionals)
			lazy.invalidate();
	}
}
