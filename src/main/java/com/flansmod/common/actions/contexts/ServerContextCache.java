package com.flansmod.common.actions.contexts;

import com.flansmod.util.MinecraftHelpers;
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

	@Nonnull
	@Override
	protected ShooterContext CreateShooterContext(@Nonnull UUID shooterID, @Nonnull UUID ownerID)
	{
		ShooterContext resolved = ResolveInternal(shooterID, ownerID);
		return resolved != null ? resolved : new ShooterContextUnresolvedEntity(ownerID, shooterID, Side);
	}

	@Nonnull
	@Override
	protected ShooterContext TryResolve(@Nonnull ShooterContextUnresolvedEntity unresolvedContext)
	{
		ShooterContext resolved = ResolveInternal(unresolvedContext.EntityUUID, unresolvedContext.OwnerUUID);
		return resolved != null ? resolved : unresolvedContext;
	}

	@Nullable
	private ShooterContext ResolveInternal(@Nonnull UUID shooterID, @Nonnull UUID ownerID)
	{
		MinecraftServer server = MinecraftHelpers.GetServer();
		if(server != null && server.isRunning())
		{
			for(ServerPlayer player : server.getPlayerList().getPlayers())
			{
				if(player.getUUID().equals(shooterID))
					return new ShooterContextPlayer(player);
			}
			for(ServerLevel level : server.getAllLevels())
			{
				Entity shooter = level.getEntity(shooterID);
				if(shooter != null)
					if(shooter instanceof LivingEntity living)
						return new ShooterContextLiving(living);
			}
		}
		return null;
	}
}
