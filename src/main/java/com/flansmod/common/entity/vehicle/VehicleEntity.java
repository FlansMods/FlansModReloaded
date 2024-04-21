package com.flansmod.common.entity.vehicle;

import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class VehicleEntity extends Entity
{
	@Nonnull
	private final LazyDefinition<VehicleDefinition> DefRef;
	@Nonnull
	public final VehicleSaveState SaveState;

	@Nonnull
	public VehicleDefinition Def() { return DefRef.DefGetter().get(); }
	@Nonnull
	public ResourceLocation Loc() { return DefRef.Loc(); }


	public VehicleEntity(@Nonnull EntityType<? extends VehicleEntity> type, @Nonnull Level world)
	{
		super(type, world);
		DefRef = LazyDefinition.of(new ResourceLocation("TODO"), FlansMod.VEHICLES);
		SaveState = new VehicleSaveState();
	}





}
