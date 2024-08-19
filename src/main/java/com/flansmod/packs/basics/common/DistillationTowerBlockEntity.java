package com.flansmod.packs.basics.common;

import com.flansmod.common.FlansMod;
import com.flansmod.packs.basics.BasicPartsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;

public class DistillationTowerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider, Clearable
{
	public final boolean IsTop;

	public static final int MAX_DISTILLATION_STACK_HEIGHT = 3;
	public static final int INPUT_SLOT = 0;
	public static final int FUEL_SLOT = 1;
	// input and fuel at the top
	public static final int TOP_BLOCK_NUM_SLOTS = 2;

	// 1 per stack block
	public static final int OUTPUT_SLOT = 0;
	public static final int STACK_BLOCK_NUM_SLOTS = 1;


	private static final double INTERACT_RANGE = 5.0d;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_DISTILLING_PROGRESS = 2;
	public static final int DATA_DISTILLING_TOTAL_TIME = 3;

	public static final int DATA_HAS_VALID_RECIPE_AT_DEPTH_1 = 4;
	public static final int DATA_HAS_VALID_RECIPE_AT_DEPTH_2 = 5;
	public static final int DATA_HAS_VALID_RECIPE_AT_DEPTH_3 = 6;

	public static final int NUM_DATA_MEMBERS = 7;


	public int LitTime = 0;
	public int LitDuration = 0;
	public int DistillingProgress = 0;
	public int DistillingTotalTime = 0;
	public final ItemStack[] Slots;
	public final ItemStack[] RecipesInProgress = new ItemStack[MAX_DISTILLATION_STACK_HEIGHT];
	public final boolean[] RecipesValid = new boolean[MAX_DISTILLATION_STACK_HEIGHT];

	private final RecipeManager.CachedCheck<DistillationTowerBlockEntity, DistillationRecipe> RecipeQuickCheck;

	public final ContainerData DataAccess = new ContainerData()
	{
		@Override
		public int get(int id)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { return LitTime; }
				case DATA_LIT_DURATION -> { return LitDuration; }
				case DATA_DISTILLING_PROGRESS -> { return DistillingProgress; }
				case DATA_DISTILLING_TOTAL_TIME -> { return DistillingTotalTime; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_1 -> { return RecipesValid[0] ? 0 : 1; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_2 -> { return RecipesValid[1] ? 0 : 1; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_3 -> { return RecipesValid[2] ? 0 : 1; }
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
				case DATA_DISTILLING_PROGRESS -> { DistillingProgress = value; }
				case DATA_DISTILLING_TOTAL_TIME -> { DistillingTotalTime = value; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_1 -> { RecipesValid[0] = value != 0; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_2 -> { RecipesValid[1] = value != 0; }
				case DATA_HAS_VALID_RECIPE_AT_DEPTH_3 -> { RecipesValid[2] = value != 0; }
			}
		}

		@Override
		public int getCount() { return NUM_DATA_MEMBERS; }
	};

	public static class DistillationTowerBlockEntityTypeHolder implements BlockEntityType.BlockEntitySupplier<DistillationTowerBlockEntity>
	{
		public final boolean IsTop;

		public DistillationTowerBlockEntityTypeHolder(boolean isTop)
		{
			IsTop = isTop;
		}

		@Override @Nonnull
		public DistillationTowerBlockEntity create(@Nonnull BlockPos pos, @Nonnull BlockState state)
		{
			RegistryObject<BlockEntityType<DistillationTowerBlockEntity>> type = IsTop ? BasicPartsMod.DISTILLATION_TOWER_TOP_TILE_ENTITY : BasicPartsMod.DISTILLATION_TOWER_TILE_ENTITY;
			return new DistillationTowerBlockEntity(type.get(), IsTop, pos, state);
		}

		public BlockEntityType<DistillationTowerBlockEntity> CreateType()
		{
			return new BlockEntityType<DistillationTowerBlockEntity>(this, Set.of(), null)
			{
				@Override
				public boolean isValid(@Nonnull BlockState state) { return state.getBlock() instanceof DistillationTowerBlock; }
			};
		}
	}

	public DistillationTowerBlockEntity(BlockEntityType<?> type, boolean isTop, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		IsTop = isTop;

		Slots = IsTop ? new ItemStack[TOP_BLOCK_NUM_SLOTS] : new ItemStack[STACK_BLOCK_NUM_SLOTS];
		Arrays.fill(Slots, ItemStack.EMPTY);
		Arrays.fill(RecipesInProgress, ItemStack.EMPTY);
		RecipeQuickCheck = RecipeManager.createCheck(BasicPartsMod.DISTILLATION_RECIPE_TYPE.get());
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.putInt("lit_time", LitTime);
		tags.putInt("lit_duration", LitDuration);
		tags.putInt("distilling_progress", DistillingProgress);
		tags.putInt("distilling_total_time", DistillingTotalTime);

		for(int i = 0; i < Slots.length; i++)
			tags.put("slot_" + i, Slots[i].save(new CompoundTag()));

		for(int i = 0; i < MAX_DISTILLATION_STACK_HEIGHT; i++)
			tags.put("distill_" + i, RecipesInProgress[i].save(new CompoundTag()));
	}

	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);
		LitTime = tags.getInt("lit_time");
		LitDuration = tags.getInt("lit_duration");
		DistillingProgress = tags.getInt("distilling_progress");
		DistillingTotalTime = tags.getInt("distilling_total_time");

		for(int i = 0; i < Slots.length; i++)
			Slots[i] = ItemStack.of(tags.getCompound("slot_" + i));

		for(int i = 0; i < MAX_DISTILLATION_STACK_HEIGHT; i++)
			RecipesInProgress[i] = ItemStack.of(tags.getCompound("distill_" + i));
	}

	public static final Component DISTILLATION_TOP_MENU_DISPLAY_NAME = Component.translatable("menu.distillation.top");
	public static final Component DISTILLATION_MENU_DISPLAY_NAME = Component.translatable("menu.distillation.stack");
	public boolean IsLit()
	{
		if(IsTop)
			return LitTime > 0;
		DistillationTowerBlockEntity topTileEntity = GetTopDistillationTileEntity();
		return topTileEntity != null && topTileEntity != this && topTileEntity.IsLit();
	}
	public boolean IsDistillationInProgress()
	{
		if(IsTop)
			return DistillingTotalTime > 0;
		DistillationTowerBlockEntity topTileEntity = GetTopDistillationTileEntity();
		return topTileEntity != null && topTileEntity != this && topTileEntity.IsDistillationInProgress();
	}
	public boolean IsRecipeValid(int depth)
	{
		if(IsTop)
			return RecipesValid[depth] || !RecipesInProgress[depth].isEmpty();
		DistillationTowerBlockEntity topTileEntity = GetTopDistillationTileEntity();
		return topTileEntity != null && topTileEntity != this && topTileEntity.IsRecipeValid(depth);
	}

	@Override @Nonnull
	public Component getDisplayName()
	{
		return IsTop ? DISTILLATION_TOP_MENU_DISPLAY_NAME : DISTILLATION_MENU_DISPLAY_NAME;
	}
	@Override @Nonnull
	protected Component getDefaultName() { return getDisplayName(); }

	@Override
	public void clearContent()
	{
		Arrays.fill(Slots, ItemStack.EMPTY);
	}

	public int GetFractionDepth()
	{
		if(level != null)
		{
			BlockState blockState = level.getBlockState(getBlockPos());
			if (blockState.getBlock() instanceof DistillationTowerBlock distBlock)
			{
				BlockPos topPos = distBlock.GetTopBlock(level, getBlockPos(), blockState);
				return topPos.getY() - getBlockPos().getY();
			}
		}
		return 0;
	}

	public int GetTowerHeight()
	{
		if(level != null)
		{
			BlockState blockState = level.getBlockState(getBlockPos());
			if (blockState.getBlock() instanceof DistillationTowerBlock distBlock)
			{
				return distBlock.GetTowerHeight(level, getBlockPos(), blockState);
			}
		}
		return 0;
	}

	@Nullable
	public DistillationTowerBlockEntity GetTopDistillationTileEntity()
	{
		if(level != null)
		{
			BlockState blockState = level.getBlockState(getBlockPos());
			if (blockState.getBlock() instanceof DistillationTowerBlock distBlock)
			{
				BlockEntity blockEntity = level.getBlockEntity(distBlock.GetTopBlock(level, getBlockPos(), blockState));
				if (blockEntity instanceof DistillationTowerBlockEntity topDistEntity)
					return topDistEntity;
			}
		}
		return null;
	}
	@Nonnull
	public DistillationTowerBlockEntity[] GetStack()
	{
		DistillationTowerBlockEntity[] stack = new DistillationTowerBlockEntity[MAX_DISTILLATION_STACK_HEIGHT + 1];
		if(level != null)
		{
			BlockState blockState = level.getBlockState(getBlockPos());
			if (blockState.getBlock() instanceof DistillationTowerBlock distBlock)
			{
				BlockPos topBlock = distBlock.GetTopBlock(level, getBlockPos(), blockState);
				stack[0] = (DistillationTowerBlockEntity) level.getBlockEntity(topBlock);
				for(int i = 0; i <= MAX_DISTILLATION_STACK_HEIGHT; i++)
				{
					BlockEntity blockEntity = level.getBlockEntity(topBlock.below(i));
					if(blockEntity instanceof DistillationTowerBlockEntity distBlockEntity)
					{
						stack[i] = distBlockEntity;
					}
					else stack[i] = null;
				}
			}
		}
		return stack;
	}

	@Nonnull
	public BlockPos GetTopBlock()
	{
		if(level != null)
		{
			BlockState blockState = level.getBlockState(getBlockPos());
			if (blockState.getBlock() instanceof DistillationTowerBlock distBlock)
			{
				return distBlock.GetTopBlock(level, getBlockPos(), blockState);
			}
		}
		return getBlockPos();
	}

	@Nonnull
	@Override
	public AbstractContainerMenu createMenu(int containerID, @Nonnull Inventory inventory, @Nullable Player player)
	{
		DistillationTowerBlockEntity topDistiller = GetTopDistillationTileEntity();
		return new DistillationTowerMenu(containerID,
			inventory,
			IsTop,
			this,
			topDistiller == null ? DataAccess : topDistiller.DataAccess);
	}
	@Override @Nonnull
	protected AbstractContainerMenu createMenu(int containerID, @Nonnull Inventory inventory)
	{
		return createMenu(containerID, inventory, null);
	}
	@Override
	public boolean stillValid(Player player) { return player.distanceToSqr(getBlockPos().getCenter()) < INTERACT_RANGE * INTERACT_RANGE; }
	@Override
	public int getContainerSize() { return IsTop ? TOP_BLOCK_NUM_SLOTS : STACK_BLOCK_NUM_SLOTS; }
	@Override
	public boolean isEmpty()
	{
		for (ItemStack slot : Slots)
			if (!slot.isEmpty())
				return false;
		return true;
	}
	@Override
	@Nonnull
	public ItemStack getItem(int slot)
	{
		return Slots[slot];
	}
	@Override
	@Nonnull
	public ItemStack removeItem(int slot, int count)
	{
		ItemStack ret = Slots[slot].copyWithCount(count);
		Slots[slot].setCount(Slots[slot].getCount() - count);
		return ret;
	}
	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int slot)
	{
		ItemStack ret = Slots[slot].copy();
		Slots[slot] = ItemStack.EMPTY;
		return ret;
	}
	@Override
	public void setItem(int slot, @Nonnull ItemStack stack)
	{
		ItemStack existing = getItem(slot);
		boolean matching = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, existing);
		Slots[slot] = stack;

		// Check for new recipe
		if(!matching)
		{
			DistillationTowerBlockEntity topDistiller = GetTopDistillationTileEntity();
			if(topDistiller != null)
			{
				topDistiller.CheckForRecipe();
				topDistiller.setChanged();
			}
		}
	}
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack)
	{
		if(IsTop)
		{
			if(slot == INPUT_SLOT) return true;
			if(slot == FUEL_SLOT) return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
		}
		return false;
	}

	private void CheckForRecipe()
	{
		if(level == null || !IsTop)
			return;

		for(ItemStack stack : RecipesInProgress)
			if(!stack.isEmpty())
				return;

		// Get up to three matching recipes and see if we can run them all
		DistillationRecipe[] matches = new DistillationRecipe[MAX_DISTILLATION_STACK_HEIGHT];
		int numMatches = 0;
		boolean abort = false;
		int maxTimeRequired = 0;
		for(int y = 1; y <= MAX_DISTILLATION_STACK_HEIGHT; y++)
		{
			BlockEntity blockEntity = level.getBlockEntity(getBlockPos().below(y));
			if(blockEntity instanceof DistillationTowerBlockEntity stackAtLayer)
			{
				// Didn't expect another top block, exit the stack
				if(stackAtLayer.IsTop)
					break;

				matches[y-1] = RecipeQuickCheck.getRecipeFor(stackAtLayer, level).orElse(null);
				if(matches[y-1] != null)
				{
					// Also check that we can output this item
					ItemStack outputStack = stackAtLayer.getItem(OUTPUT_SLOT);
					if(outputStack.isEmpty()
					|| (ItemStack.isSameItem(outputStack, matches[y-1].Result)))
					{
						if(outputStack.getCount() + matches[y-1].Result.getCount() > stackAtLayer.getMaxStackSize())
							abort = true;
						numMatches++;
						if (matches[y - 1].DistillationTime > maxTimeRequired)
							maxTimeRequired = matches[y - 1].DistillationTime;
					}
				}
			}
		}

		// We found a match? Start the process
		if(numMatches > 0 && !abort)
		{
			DistillingProgress = 0;
			DistillingTotalTime = maxTimeRequired;
			for(int i = 0; i < MAX_DISTILLATION_STACK_HEIGHT; i++)
			{
				RecipesInProgress[i] = matches[i] == null ? ItemStack.EMPTY : matches[i].Result;
				RecipesValid[i] = matches[i] != null;
			}
		}
	}

	private void ConsumeInput()
	{
		if(IsTop)
		{
			Slots[INPUT_SLOT].setCount(Slots[INPUT_SLOT].getCount() - 1);
		}
		else
		{
			DistillationTowerBlockEntity top = GetTopDistillationTileEntity();
			if(top != null)
			{
				top.ConsumeInput();
			}
		}
	}



	public static void serverTick(Level level, BlockPos pos, BlockState state, DistillationTowerBlockEntity tower)
	{
		if(tower.IsTop)
		{
			if(!tower.IsDistillationInProgress())
			{
				tower.CheckForRecipe();
			}

			// If we are distilling and need more fuel, check for it
			if (tower.IsDistillationInProgress() && !tower.IsLit())
			{
				int burnTime = ForgeHooks.getBurnTime(tower.getItem(FUEL_SLOT), RecipeType.SMELTING);
				if (burnTime > 0)
				{
					tower.removeItem(FUEL_SLOT, 1);
					tower.LitDuration = burnTime;
					tower.LitTime = burnTime;
					setChanged(level, pos, state);
				}
			}

			// If we have a recipe in progress, continue progressing it
			if (tower.IsDistillationInProgress())
			{
				tower.DistillingProgress++;
				if (tower.DistillingProgress >= tower.DistillingTotalTime)
				{
					// Create all valid outputs
					boolean outputSuccessful = true;
					for (int i = 0; i < MAX_DISTILLATION_STACK_HEIGHT; i++)
					{
						if (!tower.RecipesInProgress[i].isEmpty())
						{
							// Find the block it should be output into
							BlockEntity blockEntity = level.getBlockEntity(pos.below(i + 1));
							if (blockEntity instanceof DistillationTowerBlockEntity stackAtLayer)
							{
								// Didn't expect another top block, exit the stack
								if (stackAtLayer.IsTop)
								{
									FlansMod.LOGGER.error("Trying to output distillation result into non-distillation block");
									break;
								}

								// Either put a new stack in an empty slot
								ItemStack outputContents = stackAtLayer.getItem(OUTPUT_SLOT);
								if (outputContents.isEmpty())
								{
									stackAtLayer.setItem(OUTPUT_SLOT, tower.RecipesInProgress[i].copy());
								}
								// Or add to an existing stack
								else if (outputContents.getItem() == tower.RecipesInProgress[i].getItem()
									&& stackAtLayer.getMaxStackSize() - outputContents.getCount() >= tower.RecipesInProgress[i].getCount())
								{
									outputContents.setCount(outputContents.getCount() + tower.RecipesInProgress[i].getCount());
								} else
								{
									outputSuccessful = false;
								}
							}
						}

						tower.RecipesInProgress[i] = ItemStack.EMPTY;
					}
					// Consume the input

					// We hit max progress, complete the recipe
					if(outputSuccessful)
					{
						tower.ConsumeInput();
						tower.DistillingProgress = 0;
						tower.DistillingTotalTime = 0;
						// Retrigger a recipe check
						tower.CheckForRecipe();
						tower.setChanged();
					}
				}
			}

			// If lit, decrease the lit time
			if (tower.IsLit())
			{
				tower.LitTime--;
				if (tower.LitTime <= 0)
				{
					tower.LitDuration = 0;
					setChanged(level, pos, state);
				}
			}
		}
	}


	@Override
	@Nonnull
	public int[] getSlotsForFace(@Nonnull Direction direction)
	{
		if(IsTop)
		{
			if (direction == Direction.UP)
				return new int[]{INPUT_SLOT};
			else
				return new int[]{FUEL_SLOT};
		}
		return new int[] { OUTPUT_SLOT };
	}
	@Override
	public boolean canPlaceItemThroughFace(int slot, @Nonnull ItemStack stack, @Nullable Direction direction)
	{
		return IsTop;
	}
	@Override
	public boolean canTakeItemThroughFace(int slot, @Nonnull ItemStack stack, @Nonnull Direction direction)
	{
		return !IsTop;
	}


	private LazyOptional<? extends IItemHandler>[] handlers =
		SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER) {
			if (facing == Direction.UP)
				return handlers[0].cast();
			else if (facing == Direction.DOWN)
				return handlers[1].cast();
			else
				return handlers[2].cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		for (int x = 0; x < handlers.length; x++)
			handlers[x].invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		handlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
	}


}
