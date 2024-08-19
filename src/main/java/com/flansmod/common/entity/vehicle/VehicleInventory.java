package com.flansmod.common.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class VehicleInventory implements Container
{

	@Nonnull
	public final VehicleSubContainer GunStacks;
	@Nonnull
	public final VehicleSubContainer AmmoStacks;
	@Nonnull
	public final VehicleSubContainer FuelStacks;

	public VehicleInventory(int numGuns, int numAmmos, int numFuels)
	{
		GunStacks = new VehicleSubContainer(VehicleSubContainer.EType.Gun, numGuns);
		AmmoStacks = new VehicleSubContainer(VehicleSubContainer.EType.Ammo, numAmmos);
		FuelStacks = new VehicleSubContainer(VehicleSubContainer.EType.Fuel, numFuels);
	}

	@Nonnull public List<VehicleSubContainer> AllSubContainers() { return List.of(GunStacks, AmmoStacks, FuelStacks); }
	@Nonnull public VehicleSubContainer GetGuns() { return GunStacks; }
	@Nonnull public VehicleSubContainer GetAmmos() { return AmmoStacks; }
	@Nonnull public VehicleSubContainer GetFuels() { return FuelStacks; }
	@Nonnull
	public VehicleSubContainer Get(@Nonnull VehicleSubContainer.EType slotType)
	{
		return switch(slotType) {
			case Gun -> GunStacks;
			case Ammo -> AmmoStacks;
			case Fuel -> FuelStacks;
		};
	}
	@Nonnull
	public Pair<VehicleSubContainer, Integer> ContainerIndexToSubIndex(int containerIndex)
	{
		for(VehicleSubContainer subContainer : AllSubContainers())
		{
			if (containerIndex < subContainer.getContainerSize())
				return Pair.of(subContainer, containerIndex);
			else
				containerIndex -= GunStacks.getContainerSize();
		}
		return Pair.of(null, -1);
	}
	public int SubIndexToContainer(@Nonnull VehicleSubContainer.EType slotType, int subIndex)
	{
		for(VehicleSubContainer subContainer : AllSubContainers())
		{
			if(subContainer.Type == slotType)
				return subIndex;
			else
				subIndex += subContainer.getContainerSize();
		}
		return -1;
	}

	@Override
	public int getContainerSize()
	{
		return GunStacks.getContainerSize() + AmmoStacks.getContainerSize() + FuelStacks.getContainerSize();
	}
	@Override
	public boolean isEmpty()
	{
		for(VehicleSubContainer subContainer : AllSubContainers())
			if(!subContainer.isEmpty())
				return false;
		return true;
	}

	@Override
	@Nonnull
	public ItemStack getItem(int index)
	{
		Pair<VehicleSubContainer, Integer> kvp = ContainerIndexToSubIndex(index);
		if(kvp.getFirst() != null)
		{
			return kvp.getFirst().getItem(kvp.getSecond());
		}
		return ItemStack.EMPTY;
	}
	@Override
	@Nonnull
	public ItemStack removeItem(int index, int count)
	{
		Pair<VehicleSubContainer, Integer> kvp = ContainerIndexToSubIndex(index);
		if(kvp.getFirst() != null)
		{
			return kvp.getFirst().removeItem(kvp.getSecond(), count);
		}
		return ItemStack.EMPTY;
	}
	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int index)
	{
		Pair<VehicleSubContainer, Integer> kvp = ContainerIndexToSubIndex(index);
		if(kvp.getFirst() != null)
		{
			return kvp.getFirst().removeItemNoUpdate(kvp.getSecond());
		}
		return ItemStack.EMPTY;
	}
	@Override
	public void setItem(int index, @Nonnull ItemStack stack)
	{
		Pair<VehicleSubContainer, Integer> kvp = ContainerIndexToSubIndex(index);
		if(kvp.getFirst() != null)
		{
			kvp.getFirst().setItem(kvp.getSecond(), stack);
		}
	}

	@Override
	public void setChanged()
	{
		for(VehicleSubContainer subContainer : AllSubContainers())
			subContainer.setChanged();
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return true;
	}

	@Override
	public void clearContent()
	{
		for(VehicleSubContainer subContainer : AllSubContainers())
			subContainer.clearContent();
	}
}
