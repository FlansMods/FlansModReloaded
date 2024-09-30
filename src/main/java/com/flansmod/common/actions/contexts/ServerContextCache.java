package com.flansmod.common.actions.contexts;

import com.flansmod.util.MinecraftHelpers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ServerContextCache extends ContextCache
{
	public ServerContextCache() { super(EContextSide.Server); }

	@Nullable
	@Override
	protected Entity TryFindEntity(@Nonnull UUID entityID)
	{
		MinecraftServer server = MinecraftHelpers.GetServer();
		if(server != null && server.isRunning())
		{
			for(ServerPlayer player : server.getPlayerList().getPlayers())
			{
				if(player.getUUID().equals(entityID))
					return player;
			}
			for(ServerLevel level : server.getAllLevels())
			{
				Entity shooter = level.getEntity(entityID);
				if(shooter != null)
					return shooter;
			}
		}
		return null;
	}

	@Override @Nonnull
	protected Optional<ShooterBlockEntity> TryFindBlockEntity(@Nonnull UUID blockEntityID)
	{
		Pair<Integer, BlockPos> pair = ShooterContextBlockEntity.ConvertShooterIDToCoords(blockEntityID);
		MinecraftServer server = MinecraftHelpers.GetServer();
		if(server != null && server.isRunning())
		{
			for (ServerLevel level : server.getAllLevels())
			{
				if(level.dimension().location().hashCode() == pair.getFirst())
				{
					BlockEntity blockEntity = level.getBlockEntity(pair.getSecond());
					if(blockEntity instanceof ShooterBlockEntity shooterBlockEntity)
					{
						return Optional.of(shooterBlockEntity);
					}
				}
			}
		}
		return Optional.empty();
	}
}
