package com.flansmod.common.entity.vehicle;

import com.flansmod.common.entity.vehicle.hierarchy.VehiclePartPath;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;

public class VehicleHitResult extends EntityHitResult
{
	@Nonnull
	public final VehiclePartPath HitPart;

	public VehicleHitResult(@Nonnull VehicleEntity vehicle, @Nonnull VehiclePartPath hitPart)
	{
		super(vehicle);
		HitPart = hitPart;
	}
}
