package com.flansmod.common.entity.longdistance;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class LongDistanceEntitySystem
{
	private static Map<EntityType<?>, Function<EntityType<?>, LongDistanceEntity>> Factories = new HashMap<>();

	public static void RegisterLongDistanceEntityClass(@Nonnull EntityType<?> entityType,
													   @Nonnull Function<EntityType<?>, LongDistanceEntity> createFunc)
	{
		Factories.put(entityType, createFunc);
	}

	public static boolean IsSupportedEntityType(@Nonnull EntityType<?> entityType)
	{
		return Factories.containsKey(entityType);
	}
	public static boolean IsSupportedEntity(@Nonnull Entity entity)
	{
		return Factories.containsKey(entity.getType());
	}
	@Nullable
	public static LongDistanceEntity CreateFor(@Nonnull Entity entity) { return CreateFor(entity.getType()); }
	@Nullable
	public static LongDistanceEntity CreateFor(@Nonnull EntityType<?> entityType)
	{
		var func = Factories.get(entityType);
		return func != null ? func.apply(entityType) : null;
	}



	@Nonnull
	public abstract Collection<LongDistanceEntity> GetAllLongEntities(@Nonnull Level level);
	@Nullable
	public LongDistanceEntity GetFor(@Nonnull Entity entity) { return GetFor(entity.getUUID()); }
	@Nullable
	public abstract LongDistanceEntity GetFor(@Nonnull UUID entityID);
}
