package com.flansmod.client;

import com.flansmod.common.actions.contexts.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ClientContextCache extends ContextCache
{
	public ClientContextCache() { super(EContextSide.Client); }

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
		if(Minecraft.getInstance().level != null)
		{
			for (Entity entity : Minecraft.getInstance().level.entitiesForRendering())
			{
				if(entity.getUUID().equals(shooterID))
				{
					if (entity instanceof Player player)
						return new ShooterContextPlayer(player);
					if (entity instanceof LivingEntity living)
						return new ShooterContextLiving(living);
				}
			}
		}
		return null;
	}
}
