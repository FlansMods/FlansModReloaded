package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

public class VehicleItem extends Item
{
	@Nonnull
	public final LazyDefinition<VehicleDefinition> DefRef;

	public VehicleItem(@Nonnull ResourceLocation defLoc, @Nonnull Item.Properties props)
	{
		super(props);
		DefRef = LazyDefinition.of(defLoc, FlansMod.VEHICLES);
	}


}
