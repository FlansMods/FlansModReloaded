package com.flansmod.common.actions.contexts;

import com.flansmod.common.item.FlanItem;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Function;

public class GunContextHistory extends ContextHistory<GunContext>
{
	public static final GunContextHistory INVALID = new GunContextHistory(GunContext.INVALID.GetUUID()) {
		@Override
		@Nonnull
		public GunContext GetOrCreate(@Nonnull Function<GunContext, Boolean> validatorFunc,
									  @Nonnull NonNullSupplier<GunContext> creatorFunc,
									  @Nonnull NonNullSupplier<Long> timeFunc)
		{
			return GunContext.INVALID;
		}
	};

	public GunContextHistory(@Nonnull UUID id)
	{
		super(id);
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull ShooterContext shooter, int slotIndex)
	{
		GunContext gunContext = GetOrCreate(
			(check) -> check.GetShooter() == shooter
					&& check.GetInventorySlotIndex() == slotIndex,
			() -> shooter.CreateContext(ID),
			MinecraftHelpers::GetTick);

		gunContext.UpdateFromItemStack();
		return gunContext;
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull ShooterContext shooter)
	{
		GunContext gunContext = GetOrCreate(
			(check) -> check.GetShooter() == shooter
					&& IsGunStillPresentIn(check, shooter),
			() -> shooter.CreateContext(ID),
			MinecraftHelpers::GetTick);

		gunContext.UpdateFromItemStack();
		return gunContext;
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull ItemEntity itemEntity)
	{
		return GetOrCreate(
			(check) -> check instanceof GunContextItemEntity checkItemEntity
					&& checkItemEntity.Holder == itemEntity,
			() -> new GunContextItemEntity(itemEntity),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull BlockEntity blockEntity, @Nonnull Container container, int slot)
	{
		return GetOrCreate(
			(check) -> {
				return check instanceof GunContextTileEntity teContext
					&& teContext.TileEntity == blockEntity
					&& check.GetAttachedInventory() == container
					&& check.GetInventorySlotIndex() == slot;
			},
			() -> new GunContextTileEntity(blockEntity, container, slot),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull Container container, int slot)
	{
		final int slotIndex = slot;
		return GetOrCreate(
			(check) -> {
				return check.GetAttachedInventory() == container
					&& check.GetInventorySlotIndex() == slotIndex;
			},
			() -> new GunContextInventoryItem(container, slotIndex),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public GunContext ContextualizeWith(@Nonnull ItemStack stack)
	{
		return GetOrCreate(
			(check) -> FlanItem.GetGunID(check.Stack).equals(ID),
			() -> new GunContextItem(stack),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public GunContext WithoutContext()
	{
		GunContext mostRecent = GetMostRecentContext();
		return mostRecent == null ? GunContext.INVALID : mostRecent;
	}

	@Override
	protected boolean BasicValidation(@Nonnull GunContext check)
	{
		return check.IsValid();
	}

	@Override
	protected void MarkContextAsOld(@Nonnull GunContext oldContext)
	{

	}

	@Override
	@Nonnull
	protected GunContext GetInvalidContext(){ return GunContext.INVALID; }

	private boolean IsGunStillPresentIn(@Nonnull GunContext gun, @Nonnull ShooterContext in)
	{
		int expectedSlot = gun.GetInventorySlotIndex();
		Container inContainer = in.GetAttachedInventory();
		if(inContainer != null)
		{
			if(0 <= expectedSlot && expectedSlot < inContainer.getContainerSize())
			{
				ItemStack foundStack = inContainer.getItem(expectedSlot);
				return GunContext.CompareGunStacks(foundStack, gun.Stack).IsValid();
			}
		}
		return false;
	}
}
