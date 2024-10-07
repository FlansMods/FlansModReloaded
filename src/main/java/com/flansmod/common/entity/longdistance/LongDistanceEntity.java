package com.flansmod.common.entity.longdistance;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public abstract class LongDistanceEntity
{
	@Nonnull
	public final EntityType<?> Type;
	@Nonnull
	public ResourceKey<Level> Dimension;
	@Nonnull
	public Transform RootTransformCurrent;
	@Nonnull
	public Transform RootTransformPrevious;

	public LongDistanceEntity(@Nonnull EntityType<?> type)
	{
		Type = type;
		Dimension = Level.OVERWORLD;
		RootTransformCurrent = Transform.Identity();
		RootTransformPrevious = Transform.Identity();
	}

	public void AfterEntityLoaded(@Nonnull Entity entity) { }
	public void BeforeEntityUnloaded(@Nonnull Entity entity) { }
	public void BeforeEntityDestroyed(@Nonnull Entity entity) { }

	public abstract boolean ShouldBeTrackedBy(@Nonnull Transform observerPosition, @Nonnull Vec3 observerVelocity);
	public boolean ShouldBeTrackedBy(@Nonnull Player player)
	{
		return ShouldBeTrackedBy(Transform.FromEntity(player), player.getDeltaMovement());
	}
}
