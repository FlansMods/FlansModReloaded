package com.flansmod.client;

import com.flansmod.common.entity.longdistance.LongDistanceEntity;
import com.flansmod.common.entity.longdistance.LongDistanceEntitySystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientLongDistanceEntitySystem extends LongDistanceEntitySystem
{
	public final Map<UUID, LongDistanceEntity> AllEntities = new HashMap<>();

	@Nullable @Override
	public LongDistanceEntity GetFor(@Nonnull UUID entityID)
	{
		return AllEntities.get(entityID);
	}
	@Nonnull @Override
	public Collection<LongDistanceEntity> GetAllLongEntities(@Nonnull Level level)
	{
		// TODO: Check for level mismatch?
		return AllEntities.values();
	}
}
