package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.crafting.elements.WorkbenchIOSettingDefinition;
import com.flansmod.common.types.crafting.elements.WorkbenchSideDefinition;
import com.flansmod.physics.common.util.Maths;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkbenchBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, Clearable, ICapabilityProvider
{
	public final WorkbenchDefinition Def;
	public final AbstractWorkbench Workbench;

	private final LazyOptional<IEnergyStorage> EnergyStorageLazyOptional;
	private ItemCapabilityMultiContainer[] DirectionalItemCaps;
	private final List<LazyOptional<IItemHandler>> DirectionalItemCapLazyOptionals;

	private static final double INTERACT_RANGE_SQ = 5.0d * 5.0d;

	public final ContainerData DataAccess;

	public WorkbenchBlockEntity(ResourceLocation defLoc, BlockPos pos, BlockState state)
	{
		super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(defLoc), pos, state);
		Def = FlansMod.WORKBENCHES.Get(defLoc);
		Workbench = new AbstractWorkbench(Def, (player) -> {
			if(isRemoved() || getLevel().getBlockEntity(getBlockPos()) != this)
				return false;
			if(getBlockPos().distToCenterSqr(player.getPosition(0f)) > INTERACT_RANGE_SQ)
				return false;
			return true;
		});
		DataAccess = Workbench.GetDataAccess();

		DirectionalItemCapLazyOptionals = new ArrayList<>(6);
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.DOWN)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.UP)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.NORTH)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.SOUTH)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.WEST)));
		DirectionalItemCapLazyOptionals.add(LazyOptional.of(() -> SupplyItemCapability(Direction.EAST)));
		EnergyStorageLazyOptional = Workbench.GetLazyOptionalEnergy();
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
	public int getContainerSize() { return Workbench.getContainerSize(); }
	@Override
	public boolean isEmpty() { return Workbench.isEmpty(); }
	@Override
	public boolean stillValid(@Nonnull Player player) { return Workbench.stillValid(player); }
	@Nonnull
	public Pair<Container, Integer> GetSubContainer(int slot) { return Workbench.GetSubContainer(slot); }
	@Override @Nonnull
	public ItemStack getItem(int slot) { return Workbench.getItem(slot); }
	@Override @Nonnull
	public ItemStack removeItem(int slot, int count) { return Workbench.removeItem(slot, count); }
	@Override @Nonnull
	public ItemStack removeItemNoUpdate(int slot) { return Workbench.removeItemNoUpdate(slot); }
	@Override
	public void setItem(int slot, @Nonnull ItemStack stack) { Workbench.setItem(slot, stack); }

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
		Workbench.save(tags);
	}
	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);
		Workbench.load(tags);
	}

	private static final Component DISPLAY_NAME = Component.translatable("workbench.title");
	@Override
	@Nonnull
	public Component getDisplayName() { return DISPLAY_NAME; }
	@Override
	public void clearContent() { Workbench.clearContent(); }
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, @Nonnull Inventory inventory, @Nonnull Player player)
	{
		return Workbench.createMenu(containerID, inventory, player);
	}

	public static void serverTick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull WorkbenchBlockEntity workbench)
	{
		if(workbench.Workbench.serverTick(level, pos.getCenter()))
			setChanged(level, pos, state);
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
					int maxCount = Maths.min(container.getMaxStackSize(), stack.getCount());
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
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
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
				int amountToTake = Maths.min(existingStack.getCount(), amount);
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
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
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
					return new ItemCapabilityMultiContainer(Workbench.GetContainers(ioSetting.type), ioSetting.allowInput, ioSetting.allowExtract);
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
				case UP: 	{	return new ItemCapabilityMultiContainer(new Container[] { Workbench.MaterialContainer, Workbench.PartCraftingInputContainer, Workbench.GunCraftingInputContainer }, true, true); }
				case DOWN: 	{ 	return new ItemCapabilityMultiContainer(new Container[] { Workbench.MaterialContainer, Workbench.PartCraftingOutputContainer, Workbench.GunCraftingOutputContainer }, true, true); }
				case NORTH: { 	return new ItemCapabilityMultiContainer(new Container[] { Workbench.BatteryContainer }, true, true); }
				case SOUTH: { 	return new ItemCapabilityMultiContainer(new Container[] { Workbench.FuelContainer }, true, true); }
				case EAST:  {	return new ItemCapabilityMultiContainer(new Container[] { Workbench.PartCraftingOutputContainer, Workbench.GunCraftingOutputContainer }, false, true); }
				case WEST:  { 	return new ItemCapabilityMultiContainer(new Container[] { Workbench.PartCraftingInputContainer, Workbench.GunCraftingInputContainer }, true, false); }
			}
		}
		// If we have ONLY item holding, show it on all sides
		else if(Def.itemHolding.slots.length > 0)
		{
			return new ItemCapabilityMultiContainer(new Container[] { Workbench.MaterialContainer }, true, true);
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



	@Nullable
	public GunFabricationRecipe GetSelectedGunRecipe()
	{
		return level != null ? Workbench.GetSelectedGunRecipe(level) : null;
	}
	public void UpdateGunCraftingOutputSlot()
	{
		if(level != null)
			Workbench.UpdateGunCraftingOutputSlot(level);
	}
	public boolean IsGunCraftingFullyValid()
	{
		return level != null && Workbench.IsGunCraftingFullyValid(level);
	}
	public void ConsumeGunCraftingInputs()
	{
		if(level != null)
			Workbench.ConsumeGunCraftingInputs(level);
	}
	public void QueueCrafting(int count)
	{
		if(level != null)
			Workbench.QueueCrafting(level, count);
	}
}
