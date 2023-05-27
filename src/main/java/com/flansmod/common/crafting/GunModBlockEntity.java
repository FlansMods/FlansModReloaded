package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.item.PartItem;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class GunModBlockEntity extends BlockEntity implements MenuProvider, Clearable
{
	public final RestrictedContainer GunContainer;
	public final RestrictedContainer MaterialContainer;
	public final RestrictedContainer BatteryContainer;
	private static final double INTERACT_RANGE = 5.0d;
	private static final int NUM_MATERIAL_SLOTS = 8;

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
		MaterialContainer = new RestrictedContainer(
			this,
			INTERACT_RANGE,
			NUM_MATERIAL_SLOTS,
			64,
			(stack) -> { return stack.getItem() instanceof PartItem part && part.Def().canPlaceInModificationTable;	}
		);
		BatteryContainer = new RestrictedContainer(
			this,
			INTERACT_RANGE,
			1,
			64,
			(stack) -> { return stack.getCapability(ForgeCapabilities.ENERGY).isPresent(); }
		);
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("gun", GunContainer.save(new CompoundTag()));
		tags.put("materials", MaterialContainer.save(new CompoundTag()));
		tags.put("battery", BatteryContainer.save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags.getCompound("gun"));
		MaterialContainer.load(tags.getCompound("materials"));
		BatteryContainer.load(tags.getCompound("battery"));
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
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player)
	{
		return new GunModificationMenu(containerID, inventory, GunContainer, MaterialContainer, BatteryContainer);
	}
}
