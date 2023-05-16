package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GunModBlockEntity extends BlockEntity implements MenuProvider, Clearable
{
	private final RestrictedContainer GunContainer;
	private static final double INTERACT_RANGE = 5.0d;

	public GunModBlockEntity(BlockPos pos, BlockState state)
	{
		super(FlansMod.GUN_MOD_TILE_ENTITY.get(), pos, state);
		GunContainer = new RestrictedContainer(
			this,
			INTERACT_RANGE,
			1,
			1,
			(stack) -> { return stack.getItem() instanceof GunItem; }
		);
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		GunContainer.save(tags);
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags);
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
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player)
	{
		return new GunModificationMenu(containerID, GunContainer);
	}
}
