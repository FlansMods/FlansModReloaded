package com.flansmod.common.actions.contexts;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TargetsContext
{
	@Nonnull
	public final Map<BlockPos, BlockState> BlockTargets = new HashMap<>();
	@Nonnull
	public final List<Entity> EntityTargets = new ArrayList<>();
	@Nonnull
	public final List<Vec3> PositionTargets = new ArrayList<>();

	public TargetsContext() {}

	public boolean IsEmpty() { return BlockTargets.isEmpty() && EntityTargets.isEmpty() && PositionTargets.isEmpty(); }

	public TargetsContext With(@Nullable Entity entity) { Add(entity); return this; }
	public TargetsContext With(@Nullable Vec3 pos) { Add(pos); return this; }
	public TargetsContext With(@Nullable BlockPos pos, @Nullable BlockState state) { Add(pos, state); return this; }
	public TargetsContext Add(@Nullable Entity entity)
	{
		if(entity != null && !EntityTargets.contains(entity))
			EntityTargets.add(entity);
		return this;
	}
	public TargetsContext Add(@Nullable Vec3 pos)
	{
		if(pos != null)
			PositionTargets.add(pos);
		return this;
	}
	public TargetsContext Add(@Nullable BlockPos pos, @Nullable BlockState state)
	{
		if(pos != null && state != null && !BlockTargets.containsKey(pos))
			BlockTargets.put(pos, state);
		return this;
	}

	public void ForEachShooter(@Nonnull Consumer<ShooterContext> func)
	{
		for(Entity entity : EntityTargets)
		{
			ShooterContext shooterContext = ShooterContext.of(entity);
			if(shooterContext.IsValid())
			{
				func.accept(shooterContext);
			}
		}
	}
	public void ForEachGun(@Nonnull Consumer<GunContext> func)
	{
		for(Entity entity : EntityTargets)
		{
			ShooterContext shooterContext = ShooterContext.of(entity);
			if(shooterContext.IsValid())
			{
				for(GunContext gunContext : shooterContext.GetAllGunContexts(entity.level().isClientSide))
				{
					func.accept(gunContext);
				}
			}
		}
	}
	public void ForEachEntity(@Nonnull Consumer<Entity> func)
	{
		for(Entity entity : EntityTargets)
			func.accept(entity);
	}
	public void ForEachBlock(@Nonnull BiConsumer<BlockPos, BlockState> func)
	{
		for(var kvp : BlockTargets.entrySet())
			func.accept(kvp.getKey(), kvp.getValue());
	}
	public void ForOnePosition(@Nonnull Consumer<Vec3> func)
	{
		for(Entity entity : EntityTargets)
		{
			func.accept(entity.position());
			return;
		}
		for(var kvp : BlockTargets.entrySet())
		{
			func.accept(kvp.getKey().getCenter());
			return;
		}
		for(Vec3 pos : PositionTargets)
		{
			func.accept(pos);
			return;
		}
	}
	public void ForEachPosition(@Nonnull Consumer<Vec3> func)
	{
		for(Entity entity : EntityTargets)
			func.accept(entity.position());
		for(var kvp : BlockTargets.entrySet())
			func.accept(kvp.getKey().getCenter());
		for(Vec3 pos : PositionTargets)
			func.accept(pos);
	}
}
