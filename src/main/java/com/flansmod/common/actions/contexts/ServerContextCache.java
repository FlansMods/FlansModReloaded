package com.flansmod.common.actions.contexts;

import com.flansmod.util.MinecraftHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
}
