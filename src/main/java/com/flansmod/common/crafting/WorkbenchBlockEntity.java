package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.item.PartItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.mojang.datafixers.types.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
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
	private EnergyStorage InternalEnergyStorage;
	private LazyOptional<IEnergyStorage> LazyEnergyStorage;
	private static final double INTERACT_RANGE = 5.0d;

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
		Def = FlansMod.WORKBENCHES.get(defLoc);

		if(Def.gunCrafting.isActive)
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
				1,
				64,
				(stack) -> { return stack.getBurnTime(RecipeType.SMELTING) > 0.0f; }
			);
		}
		else
		{
			BatteryContainer = new RestrictedContainer(this);
			FuelContainer = new RestrictedContainer(this);
		}

		LazyEnergyStorage = LazyOptional.of(() -> InternalEnergyStorage);

	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("gun", GunContainer.save(new CompoundTag()));
		tags.put("materials", MaterialContainer.save(new CompoundTag()));
		tags.put("battery", BatteryContainer.save(new CompoundTag()));
		tags.put("fuel", FuelContainer.save(new CompoundTag()));
		tags.put("energy", InternalEnergyStorage.serializeNBT());
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags.getCompound("gun"));
		MaterialContainer.load(tags.getCompound("materials"));
		BatteryContainer.load(tags.getCompound("battery"));
		FuelContainer.load(tags.getCompound("fuel"));
		InternalEnergyStorage.deserializeNBT(tags.getCompound("energy"));
		LazyEnergyStorage = LazyOptional.of(() -> InternalEnergyStorage);
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
		return new WorkbenchMenu(containerID, inventory, Def, GunContainer, MaterialContainer, BatteryContainer, FuelContainer);
	}
}
