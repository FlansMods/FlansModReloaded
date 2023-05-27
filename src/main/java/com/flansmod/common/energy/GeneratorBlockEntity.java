package com.flansmod.common.energy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.GunModificationMenu;
import com.flansmod.common.crafting.RestrictedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider, Clearable
{
	public final RestrictedContainer FuelContainer;
	private EnergyStorage InternalEnergyStorage;
	private LazyOptional<IEnergyStorage> LazyEnergyStorage;
	private static final double INTERACT_RANGE = 5.0d;

	public GeneratorBlockEntity(BlockPos pos, BlockState state)
	{
		super(FlansMod.DIESEL_GENERATOR_TILE_ENTITY.get(), pos, state);
		FuelContainer = new RestrictedContainer(
			this,
			INTERACT_RANGE,
			1,
			64,
			(stack) -> { return stack.getBurnTime(RecipeType.SMELTING) > 0.0f; }
		);

		LazyEnergyStorage = LazyOptional.of(() -> InternalEnergyStorage);
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap)
	{
		if(cap == ForgeCapabilities.ENERGY)
			return LazyEnergyStorage.cast();
		return super.getCapability(cap);
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		LazyEnergyStorage.invalidate();
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("fuel", FuelContainer.save(new CompoundTag()));
		tags.put("energy", InternalEnergyStorage.serializeNBT());
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
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
		FuelContainer.clearContent();
	}



	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player)
	{
		return new GeneratorMenu(containerID, inventory, FuelContainer);
	}
}
