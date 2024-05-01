package com.flansmod.common.entity.vehicle.damage;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;

public class VehicleHitResult extends EntityHitResult
{
	@Nonnull
	public final String HitPart;

	public VehicleHitResult(@Nonnull VehicleEntity vehicle, @Nonnull String hitPart)
	{
		super(vehicle);
		HitPart = hitPart;
	}
}
