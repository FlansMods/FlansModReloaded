package com.flansmod.client;

import com.flansmod.common.actions.contexts.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
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

	@Override
	protected @Nonnull Optional<ShooterBlockEntity> TryFindBlockEntity(@Nonnull UUID blockEntityID)
	{
		if(Minecraft.getInstance().level != null)
		{
			Pair<Integer, BlockPos> pair = ShooterContextBlockEntity.ConvertShooterIDToCoords(blockEntityID);
			if(Minecraft.getInstance().level.dimension().location().hashCode() == pair.getFirst())
			{
				BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pair.getSecond());
				if(blockEntity instanceof ShooterBlockEntity shooterBlockEntity)
				{
					return Optional.of(shooterBlockEntity);
				}
			}
		}
		return Optional.empty();
	}

}
