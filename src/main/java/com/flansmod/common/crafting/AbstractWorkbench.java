package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.crafting.ingredients.StackedIngredient;
import com.flansmod.common.crafting.ingredients.TieredMaterialIngredient;
import com.flansmod.common.crafting.menus.*;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.crafting.recipes.PartFabricationRecipe;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.crafting.EWorkbenchInventoryType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.elements.MaterialSourceDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.physics.common.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AbstractWorkbench implements Container, Clearable, MenuProvider
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
	public LazyOptional<IEnergyStorage> GetLazyOptionalEnergy()
	{
		return LazyOptional.of(() -> {
			EnergyStorage = new EnergyStorage(Def.energy.maxFE);
			return EnergyStorage;
		} );
	}

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

	public static final int CRAFTING_NOTHING = -1;

	public int LitTime = 0;
	public int LitDuration = 0;

	public int CraftTime = 0;
	public int CraftDuration = 0;

	public int PlayerSelectedCraftingGun = CRAFTING_NOTHING;
	public int PlayerSelectedCraftingPart = CRAFTING_NOTHING;
	public int[] CraftQueueCount = new int[NUM_CRAFTING_QUEUE_SLOTS];
	public int[] CraftingPart = new int[] {CRAFTING_NOTHING, CRAFTING_NOTHING, CRAFTING_NOTHING, CRAFTING_NOTHING}; // new int[NUM_CRAFTING_QUEUE_SLOTS];


	public AbstractWorkbench(@Nonnull WorkbenchDefinition def, @Nonnull Function<Player, Boolean> stillValidFunc)
	{
		Def = def;
		if(Def.gunModifying.isActive)
		{
			GunContainer = new RestrictedContainer(
				1, 1, (stack) -> stack.getItem() instanceof GunItem, stillValidFunc);
			PaintCanContainer = new RestrictedContainer(
				1, 64, (stack) -> stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get(), stillValidFunc);
			MagUpgradeContainer = new RestrictedContainer(
				1, 64, (stack) -> stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get(), stillValidFunc);
		}
		else
		{
			GunContainer = new RestrictedContainer();
			PaintCanContainer = new RestrictedContainer();
			MagUpgradeContainer = new RestrictedContainer();
		}

		if(Def.gunCrafting.isActive)
		{
			GunCraftingInputContainer = new RestrictedContainer(
				Def.gunCrafting.maxSlots, 64, (stack) -> true, stillValidFunc);
			GunCraftingOutputContainer = new RestrictedContainer(
				1, 64, ItemStack::isEmpty, stillValidFunc);
		}
		else
		{
			GunCraftingInputContainer = new RestrictedContainer();
			GunCraftingOutputContainer = new RestrictedContainer();
		}

		if(Def.partCrafting.isActive)
		{
			PartCraftingInputContainer = new RestrictedContainer(
				Def.partCrafting.inputSlots, 64, (stack) -> true, stillValidFunc);
			PartCraftingOutputContainer = new RestrictedContainer(
				Def.partCrafting.outputSlots, 64, ItemStack::isEmpty, stillValidFunc);
		}
		else
		{
			PartCraftingInputContainer = new RestrictedContainer();
			PartCraftingOutputContainer = new RestrictedContainer();
		}

		if(Def.itemHolding.slots.length > 0)
		{
			MaterialContainer = new RestrictedContainer(
				Def.itemHolding.slots.length, Def.itemHolding.maxStackSize, (stack) -> true, stillValidFunc);
		}
		else MaterialContainer = new RestrictedContainer();

		if(Def.energy.maxFE > 0)
		{
			EnergyStorage = new EnergyStorage(Def.energy.maxFE, Def.energy.acceptFEPerTick, Def.energy.disperseFEPerTick);
			BatteryContainer = new RestrictedContainer(
				Def.energy.numBatterySlots, Def.energy.batterySlotStackSize,
				(stack) -> stack.getCapability(ForgeCapabilities.ENERGY).isPresent(),
				stillValidFunc);
			FuelContainer = new RestrictedContainer(
				Def.energy.numSolidFuelSlots, 64,
				(stack) -> ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0f,
				stillValidFunc);
		}
		else
		{
			EnergyStorage = null;
			BatteryContainer = new RestrictedContainer();
			FuelContainer = new RestrictedContainer();
		}
	}

	@Nonnull
	public ContainerData GetDataAccess()
	{
		return new ContainerData()
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
	private static final Component DISPLAY_NAME = Component.translatable("workbench.title");
	@Override
	@Nonnull
	public Component getDisplayName() { return DISPLAY_NAME; }

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
	@Override
	public void setChanged() { }
	@Override
	public void clearContent()
	{
		for(Container container : GetContainers(EWorkbenchInventoryType.AllTypes))
			container.clearContent();
	}
	@Nonnull
	public Container[] GetContainers(@Nonnull EWorkbenchInventoryType type)
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
	public void save(@Nonnull CompoundTag tags)
	{
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
	public void load(@Nonnull CompoundTag tags)
	{
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
				CraftingPart[i] = CRAFTING_NOTHING;
		}
	}

	public boolean serverTick(@Nullable Level level, @Nullable Vec3 pos)
	{
		boolean changed = false;
		if(EnergyStorage != null)
		{
			if (LitTime > 0)
			{
				LitTime--;
				EnergyStorage.receiveEnergy(Def.energy.solidFEPerFuelTime, false);
				if (LitTime <= 0)
				{
					LitDuration = 0;
					changed = true;
				}
			}

			if (LitTime <= 0 && EnergyStorage.getEnergyStored() < EnergyStorage.getMaxEnergyStored())
			{
				if (FuelContainer.getContainerSize() > 0)
				{
					int burnTime = ForgeHooks.getBurnTime(FuelContainer.getItem(0), RecipeType.SMELTING);
					if (burnTime > 0)
					{
						FuelContainer.removeItem(0, 1);
						LitDuration = burnTime;
						LitTime = burnTime;
						changed = true;
					}
				}
			}
		}

		if(level != null && PartCraftingOutputContainer.getContainerSize() > 0)
		{
			if(CraftTime > 0)
			{
				CraftTime--;
				if(CraftTime <= 0)
				{
					CraftDuration = 0;
					boolean success = CraftOnePart(level, pos != null ? pos : Vec3.ZERO);
					if(success)
						changed = true;
				}
			}

			// Then try find a new craft
			if(CraftTime <= 0)
			{
				// If the thing in slot 0 is done, advance the stack
				if(CraftQueueCount[0] == 0)
				{
					for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS - 1; i++)
					{
						CraftQueueCount[i] = CraftQueueCount[i + 1];
						CraftingPart[i] = CraftingPart[i+1];
					}
					CraftQueueCount[NUM_CRAFTING_QUEUE_SLOTS - 1] = 0;
					CraftingPart[NUM_CRAFTING_QUEUE_SLOTS - 1] = CRAFTING_NOTHING;
				}

				// Then, if there's something to craft in slot 0, start crafting one
				if(CraftQueueCount[0] != 0)
				{
					int inputCount = GetMaxPartsCraftableFromInput(level, CraftingPart[0]);
					boolean canOutput = GetOutputSlotToCraftPart(level) != Inventory.NOT_FOUND_INDEX;
					boolean canPower = true;
					if (Def.partCrafting.FECostPerCraft > 0)
					{
						canPower = EnergyStorage.getEnergyStored() >= Def.partCrafting.FECostPerCraft;
					}
					if (inputCount > 0 && canOutput && canPower)
					{
						CraftTime = Maths.Floor(Def.partCrafting.timePerCraft * 20);
						CraftDuration = CraftTime;
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	// ----------------------------------------------------------------------------------------------
	// --------------------------- GUN CRAFTING -----------------------------------------------------
	// ----------------------------------------------------------------------------------------------
	public boolean GunRecipeCanBeCraftedInThisWorkbench(@Nonnull ItemStack output)
	{
		// Server config hook
		if(!FlansModConfig.AllowGunCrafting.get())
			return false;
		// ------------------

		if(Def.gunCrafting.isActive)
			for(ItemStack stack : Def.gunCrafting.GetAllOutputs())
				if(ItemStack.isSameItem(stack, output))
					return true;
		return false;
	}
	public int GetSelectedGunRecipeIndex() { return PlayerSelectedCraftingGun; }
	public void SelectGunCraftingRecipe(@Nonnull Level level, int recipeIndex)
	{
		if(0 <= recipeIndex)
			PlayerSelectedCraftingGun = recipeIndex;
		else
			PlayerSelectedCraftingGun = CRAFTING_NOTHING;

		UpdateGunCraftingOutputSlot(level);
	}
	@Nullable
	public GunFabricationRecipe GetSelectedGunRecipe(@Nonnull Level level)
	{
		List<GunFabricationRecipe> recipes = GetAllGunRecipes(level);
		int recipeIndex = GetSelectedGunRecipeIndex();
		if(recipeIndex == CRAFTING_NOTHING || recipeIndex >= recipes.size())
			return null;
		return recipes.get(recipeIndex);
	}
	@Nonnull
	public List<GunFabricationRecipe> GetAllGunRecipes(@Nonnull Level level)
	{
		return Def.gunCrafting.GetAllRecipes(level);
	}
	public boolean MatchesGunRecipe(@Nonnull Level level, int recipeIndex)
	{
		List<GunFabricationRecipe> recipes = GetAllGunRecipes(level);
		if(recipeIndex == CRAFTING_NOTHING || recipeIndex >= recipes.size())
			return false;

		return recipes.get(recipeIndex).matches(this, level);
	}
	public boolean MatchesGunRecipe(@Nonnull Level level, @Nonnull GunFabricationRecipe recipe)
	{
		return recipe.matches(this, level);
	}
	public void AutoFillGunCraftingInputSlot(@Nonnull Player player, int ingredientIndex)
	{
		GunFabricationRecipe currentRecipe = GetSelectedGunRecipe(player.level());
		if(currentRecipe != null)
		{
			if(0 <= ingredientIndex && ingredientIndex < currentRecipe.InputIngredients.size())
			{
				// Get the existing stack
				ItemStack stackInSlot = GunCraftingInputContainer.getItem(ingredientIndex);

				// See if we can auto-populate for this specific ingredient
				Ingredient ingredient = currentRecipe.InputIngredients.get(ingredientIndex);
				boolean creative = player.isCreative();
				if(creative)
				{
					ItemStack[] possibleItems = ingredient.getItems();
					if(possibleItems.length > 0)
					{
						int existingIndex = -1;
						for (int i = 0; i < possibleItems.length; i++)
						{
							if (ItemStack.isSameItemSameTags(stackInSlot, possibleItems[i]))
							{
								existingIndex = i;
							}
						}
						GunCraftingInputContainer.setItem(ingredientIndex, possibleItems[(existingIndex + 1) % possibleItems.length].copy());
						UpdateGunCraftingOutputSlot(player.level());
					}
				}
				else // Non-creative
				{
					int foundMatch = -1;
					for(int i = 0; i < player.getInventory().getContainerSize(); i++)
					{
						if(ingredient.test(player.getInventory().getItem(i)))
						{
							foundMatch = i;
							break;
						}
					}

					if(foundMatch != -1)
					{
						// We are adding to an empty slot, just place it
						if(stackInSlot.isEmpty())
						{
							GunCraftingInputContainer.setItem(ingredientIndex, player.getInventory().getItem(foundMatch).copyWithCount(1));
							player.getInventory().removeItem(foundMatch, 1);
						}
						else // There was already something there, only swap if there is space on the player
						{
							if(player.getInventory().add(stackInSlot.copy()))
							{
								GunCraftingInputContainer.setItem(ingredientIndex, player.getInventory().getItem(foundMatch));
								UpdateGunCraftingOutputSlot(player.level());
							}
						}
					}
				}
			}
		}
	}
	@Nonnull
	public List<GunFabricationRecipe> GetMatchingGunRecipes(@Nonnull Level level)
	{
		List<GunFabricationRecipe> all = GetAllGunRecipes(level);
		List<GunFabricationRecipe> matching = new ArrayList<>();
		for (GunFabricationRecipe gunFabricationRecipe : all)
			if (MatchesGunRecipe(level, gunFabricationRecipe))
				matching.add(gunFabricationRecipe);
		return matching;
	}
	public boolean IsGunCraftingSlotValid(@Nonnull Level level, int ingredientIndex)
	{
		if(ingredientIndex < 0)
			return false;

		GunFabricationRecipe recipe = GetSelectedGunRecipe(level);
		if(recipe == null)
			return false;

		if(ingredientIndex >= recipe.InputIngredients.size() || ingredientIndex >= GunCraftingInputContainer.getContainerSize())
			return false;

		return recipe.InputIngredients.get(ingredientIndex).test(GunCraftingInputContainer.getItem(ingredientIndex));
	}
	public boolean IsGunCraftingFullyValid(@Nonnull Level level)
	{
		// Server config hook
		if(!FlansModConfig.AllowGunCrafting.get())
			return false;
		// ------------------

		GunFabricationRecipe recipe = GetSelectedGunRecipe(level);
		if(recipe == null)
			return false;

		for(int i = 0; i < recipe.InputIngredients.size(); i++)
		{
			if(!IsGunCraftingSlotValid(level, i))
				return false;
		}

		return true;
	}
	public void ConsumeGunCraftingInputs(@Nonnull Level level)
	{
		GunFabricationRecipe recipe = GetSelectedGunRecipe(level);
		if(recipe != null)
		{
			for (int i = 0; i < recipe.InputIngredients.size(); i++)
			{
				ItemStack stack = GunCraftingInputContainer.getItem(i);
				stack.setCount(stack.getCount() - 1);
			}
		}
	}
	public void UpdateGunCraftingOutputSlot(@Nonnull Level level)
	{
		if(IsGunCraftingFullyValid(level))
		{
			GunFabricationRecipe recipe = GetSelectedGunRecipe(level);
			if(recipe != null)
			{
				ItemStack output = recipe.assemble(this, RegistryAccess.EMPTY);
				GunCraftingOutputContainer.setItem(0, output);

			}
			else
				FlansMod.LOGGER.error("We think gun crafting is valid, but no recipe selection exists");
		}
		else
			GunCraftingOutputContainer.setItem(0, ItemStack.EMPTY);
	}

	// ----------------------------------------------------------------------------------------------
	// --------------------------- PART CRAFTING ----------------------------------------------------
	// ----------------------------------------------------------------------------------------------
	public boolean PartRecipeCanBeCraftedInThisWorkbench(@Nonnull ItemStack output)
	{
		if(Def.partCrafting.isActive)
			for(ItemStack stack : Def.partCrafting.GetAllOutputs())
				if(ItemStack.isSameItem(stack, output))
					return true;
		return false;
	}
	@Nonnull
	public List<PartFabricationRecipe> GetAllPartRecipes(@Nonnull Level level)
	{
		return Def.partCrafting.GetAllRecipes(level);
	}
	public void SelectPartCraftingRecipe(@Nonnull Level level, int index)
	{
		if(0 <= index && index < GetAllPartRecipes(level).size())
			PlayerSelectedCraftingPart = index;
		else
			PlayerSelectedCraftingPart = CRAFTING_NOTHING;
	}
	public int GetMaxPartsCraftableFromInput(@Nonnull Level level, int recipeIndex)
	{
		// Server config hook
		if(!FlansModConfig.AllowPartCrafting.get())
			return 0;
		// ------------------

		if(recipeIndex == CRAFTING_NOTHING)
			return 0;

		if(recipeIndex >= GetAllPartRecipes(level).size())
			return 0;

		PartFabricationRecipe recipe = GetAllPartRecipes(level).get(recipeIndex);
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


	public int[] GetQuantityOfEachIngredientForRecipe(@Nonnull Level level, int recipeIndex)
	{
		if(recipeIndex == CRAFTING_NOTHING || recipeIndex >= GetAllPartRecipes(level).size())
			return new int[0];

		PartFabricationRecipe recipe = GetAllPartRecipes(level).get(recipeIndex);
		return recipe == null ? new int[0] : recipe.GetMatchingOfEachIngredient(PartCraftingInputContainer);
	}

	public int[] GetRequiredOfEachIngredientForRecipe(@Nonnull Level level, int recipeIndex)
	{
		if(recipeIndex == CRAFTING_NOTHING || recipeIndex >= GetAllPartRecipes(level).size())
			return new int[0];

		PartFabricationRecipe recipe = GetAllPartRecipes(level).get(recipeIndex);
		return recipe == null ? new int[0] : recipe.GetRequiredOfEachIngredient();
	}

	public int GetOutputSlotToCraftPart(@Nonnull Level level)
	{
		if(CraftingPart[0] == CRAFTING_NOTHING)
			return Inventory.NOT_FOUND_INDEX;

		if(CraftingPart[0] >= GetAllPartRecipes(level).size())
			return Inventory.NOT_FOUND_INDEX;

		PartFabricationRecipe recipe = GetAllPartRecipes(level).get(CraftingPart[0]);
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

	public void QueueCrafting(@Nonnull Level level, int count)
	{
		for(int i = 0; i < NUM_CRAFTING_QUEUE_SLOTS; i++)
		{
			if (CraftingPart[i] == PlayerSelectedCraftingPart || CraftingPart[i] == CRAFTING_NOTHING)
			{
				CraftingPart[i] = PlayerSelectedCraftingPart;
				if (count == -1)
				{
					CraftQueueCount[i] = -1;
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
				CraftingPart[NUM_CRAFTING_QUEUE_SLOTS - 1] = CRAFTING_NOTHING;
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

	private void ConsumeIngredient(@Nonnull Level level, @Nonnull Vec3 pos, Ingredient ingredient)
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
					if (!leftover.isEmpty())
					{
						ItemEntity leftoverEntity = new ItemEntity(
							level,
							pos.x,
							pos.y,
							pos.z,
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

	public boolean CraftOnePart(@Nonnull Level level, @Nonnull Vec3 pos)
	{
		// Server config hook
		if(!FlansModConfig.AllowPartCrafting.get())
			return false;
		// ------------------

		int outputSlot = GetOutputSlotToCraftPart(level);
		if(outputSlot != Inventory.NOT_FOUND_INDEX)
		{
			PartFabricationRecipe recipe = GetAllPartRecipes(level).get(CraftingPart[0]);
			for(Ingredient ingredient : recipe.getIngredients())
			{
				ConsumeIngredient(level, pos, ingredient);
			}

			if(CraftQueueCount[0] > 0)
				CraftQueueCount[0]--;
			ItemStack result = recipe.Result;
			ItemStack existing = PartCraftingOutputContainer.getItem(outputSlot);
			if(existing.isEmpty())
				PartCraftingOutputContainer.setItem(outputSlot, result.copy());
			else if(ItemStack.isSameItem(existing, result))
				existing.setCount(existing.getCount() + result.getCount());
			else
				FlansMod.LOGGER.error("CraftOnePart tried to craft into invalid slot " + outputSlot);

			level.playSound(null, BlockPos.containing(pos), SoundEvents.IRON_GOLEM_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);

			return true;
		}
		return false;
	}

	public static int GetPaintUpgradeCost(Container gunContainer, int paintIndex)
	{
		// Server config hook
		int additionalCost = FlansModConfig.AdditionalMagazineModifyCost.get();
		// ------------------

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
					return paintableDefinition.paintjobs[paintIndex - 1].paintBucketsRequired + additionalCost;
				}
			}
		}
		return 0;
	}
	public static boolean CanPaintGun(Player player, Container gunContainer, Container paintCanContainer, int skinIndex)
	{
		// Server config hook
		if(!FlansModConfig.AllowPainting.get())
			return false;

		int additionalCost = FlansModConfig.AdditionalPaintCanCost.get();
		// ------------------

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
			int paintCost = paintjobDefinition.paintBucketsRequired + additionalCost;
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
			if (gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof FlanItem)
			{
				GunContext gunContext = GunContext.of(gunContainer, 0, player.level().isClientSide);
				if(gunContext.IsValid())
				{
					PaintableDefinition paintableDefinition = gunContext.Def.paints;
					if (paintableDefinition.IsValid())
					{
						if (skinIndex == 0)
						{
							gunContext.SetPaintjobName("default");
						} else
						{
							PaintjobDefinition paintjobDefinition = paintableDefinition.paintjobs[skinIndex - 1];
							paintCanContainer.getItem(0).setCount(paintCanContainer.getItem(0).getCount() - paintjobDefinition.paintBucketsRequired);
							gunContext.SetPaintjobName(paintjobDefinition.textureName);
						}
					}
				}
			}
		}
	}

	public static int GetMagUpgradeCost(@Nonnull Container gunContainer, int magIndex)
	{
		// Server config hook
		int additionalCost = FlansModConfig.AdditionalMagazineModifyCost.get();
		// ------------------

		if (gunContainer.getContainerSize() <= 0)
			return 0;

		if (gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
		{
			List<MagazineDefinition> mags = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines();
			if (0 <= magIndex && magIndex < mags.size())
			{
				return gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap + mags.get(magIndex).upgradeCost + additionalCost;
			}
			return gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap + additionalCost;
		}
		return 0;
	}
	public static boolean CanSelectMagazine(@Nonnull Player player, @Nonnull Container gunContainer, @Nonnull Container magUpgradeContainer, int magIndex)
	{
		// Server config hook
		if(!FlansModConfig.AllowMagazineModifying.get())
			return false;

		int additionalCost = FlansModConfig.AdditionalMagazineModifyCost.get();
		// ------------------

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
			int magCost = additionalCost
				+ mag.upgradeCost
				+ gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey).baseCostToSwap;
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
				GunContext gunContext = GunContext.of(gunContainer, 0, player.level().isClientSide);
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
}
