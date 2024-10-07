package com.flansmod.common.entity.longdistance;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class LongDistancePlayer extends LongDistanceEntity
{
	public LongDistancePlayer()
	{
		super(EntityType.PLAYER);
	}

	@Override
	public boolean ShouldBeTrackedBy(@Nonnull Transform observerPosition, @Nonnull Vec3 observerVelocity)
	{
		return false;
	}
}
