package com.flansmod.common.entity.longdistance;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class LongDistanceVehicle extends LongDistanceEntity
{
	@Nonnull
	public final ResourceLocation DefLoc;

	public LongDistanceVehicle(@Nonnull EntityType<?> type, @Nonnull ResourceLocation defLoc)
	{
		super(type);
		DefLoc = defLoc;
	}

	@Override
	public boolean ShouldBeTrackedBy(@Nonnull Transform observerPosition, @Nonnull Vec3 observerVelocity)
	{
		return false;
	}
}
