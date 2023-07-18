package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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

import java.util.Set;

public class WorkbenchBlockEntity extends BlockEntity implements MenuProvider, Clearable
{
	public final WorkbenchDefinition Def;
	public final RestrictedContainer GunContainer;
	public final RestrictedContainer MaterialContainer;
	public final RestrictedContainer BatteryContainer;
	public final RestrictedContainer FuelContainer;
	private EnergyStorage EnergyStorage;
	private static final double INTERACT_RANGE = 5.0d;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_FORGE_ENERGY = 2;
	public int LitTime = 0;
	public int LitDuration = 0;

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
				default -> { return 0; }
			}
		}

		@Override
		public void set(int id, int value)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { LitTime = value; }
				case DATA_LIT_DURATION -> { LitDuration = value; }
				case DATA_FORGE_ENERGY -> { if(EnergyStorage != null) EnergyStorage.receiveEnergy(value - EnergyStorage.getEnergyStored(), false); }
			}
		}

		@Override
		public int getCount() { return 3; }
	};

	public static class WorkbenchBlockEntityTypeHolder implements BlockEntityType.BlockEntitySupplier<WorkbenchBlockEntity>
	{
		private final ResourceLocation DefLoc;

		public WorkbenchBlockEntityTypeHolder(ResourceLocation defLoc)
		{
			DefLoc = defLoc;
		}

		@Override
		public WorkbenchBlockEntity create(BlockPos pos, BlockState state)
		{
			return new WorkbenchBlockEntity(DefLoc, pos, state);
		}

		public BlockEntityType<WorkbenchBlockEntity> CreateType()
		{
			return new BlockEntityType<WorkbenchBlockEntity>(this, Set.of(), null)
			{
				@Override
				public boolean isValid(BlockState state) { return state.getBlock() instanceof WorkbenchBlock; }
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
		}
		else GunContainer = new RestrictedContainer(this);

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
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("gun", GunContainer.save(new CompoundTag()));
		tags.put("materials", MaterialContainer.save(new CompoundTag()));
		tags.put("battery", BatteryContainer.save(new CompoundTag()));
		tags.put("fuel", FuelContainer.save(new CompoundTag()));
		if(EnergyStorage != null)
			tags.put("energy", EnergyStorage.serializeNBT());
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags.getCompound("gun"));
		MaterialContainer.load(tags.getCompound("materials"));
		BatteryContainer.load(tags.getCompound("battery"));
		FuelContainer.load(tags.getCompound("fuel"));
		if(EnergyStorage != null)
			EnergyStorage.deserializeNBT(tags.getCompound("energy"));
	}

	@Override
	public Component getDisplayName()
	{
		return null;
	}

	@Override
	public void clearContent()
	{
		GunContainer.clearContent();
		MaterialContainer.clearContent();
		BatteryContainer.clearContent();
		FuelContainer.clearContent();
	}


	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player)
	{
		return new WorkbenchMenu(containerID, inventory, Def, GunContainer, MaterialContainer, BatteryContainer, FuelContainer, DataAccess);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, WorkbenchBlockEntity workbench)
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
	}
}
