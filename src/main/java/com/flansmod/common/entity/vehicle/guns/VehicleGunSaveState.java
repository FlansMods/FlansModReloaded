package com.flansmod.common.entity.vehicle.guns;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.entity.vehicle.IVehicleSaveNode;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class VehicleGunSaveState implements IVehicleSaveNode
{
	public static final VehicleGunSaveState INVALID = new VehicleGunSaveState();

	public static final EntityDataSerializer<VehicleGunSaveState> SERIALIZER = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull VehicleGunSaveState data)
		{
			buf.writeItemStack(data.GunStack, false);
		}
		@Override
		@Nonnull
		public VehicleGunSaveState read(@Nonnull FriendlyByteBuf buf)
		{
			VehicleGunSaveState state = new VehicleGunSaveState();
			state.GunStack = buf.readItem();
			return state;
		}
	};

	@Nonnull
	public ItemStack GunStack;
	@Nullable
	public ActionStack Actions;

	@Nonnull
	public UUID GetGunID() { return GunItem.GetGunID(GunStack); }

	public VehicleGunSaveState()
	{
		GunStack = ItemStack.EMPTY;
	}

	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		if(tags.contains("item"))
			GunStack = ItemStack.of(tags.getCompound("item"));
	}

	@Nonnull
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		tags.put("item", GunStack.save(new CompoundTag()));
		return tags;
	}
}
