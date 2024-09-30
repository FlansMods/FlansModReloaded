package com.flansmod.common.actions.contexts;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum EContextSide
{
	Client,
	Server,
	Unknown;

	@Nonnull
	public static EContextSide of(@Nullable Level level)
	{
		if(level == null)
			return Unknown;
		return level.isClientSide ? Client : Server;
	}
	@Nonnull
	public static EContextSide of(@Nullable Entity entity)
	{
		if(entity == null)
			return Unknown;
		return entity.level().isClientSide ? Client : Server;
	}
	@Nonnull
	public static EContextSide of(@Nullable BlockEntity blockEntity)
	{
		if(blockEntity == null || blockEntity.getLevel() == null)
			return Unknown;
		return blockEntity.getLevel().isClientSide ? Client : Server;
	}

	public boolean IsClient()
	{
		return this == Client;
	}
	public boolean IsServer()
	{
		return this == Server;
	}
}
