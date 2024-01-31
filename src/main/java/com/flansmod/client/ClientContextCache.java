package com.flansmod.client;

import com.flansmod.common.actions.contexts.*;
import com.flansmod.util.MinecraftHelpers;
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

	@Nullable
	@Override
	protected Entity TryFindEntity(@Nonnull UUID entityID)
	{
		if(Minecraft.getInstance().level != null)
		{
			for (Entity entity : Minecraft.getInstance().level.entitiesForRendering())
			{
				if (entity.getUUID().equals(entityID))
				{
					return entity;
				}
			}
		}
		return null;
	}
}
