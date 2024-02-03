package com.flansmod.common.actions.contexts;

import com.flansmod.util.MinecraftHelpers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public class ShooterContextHistory extends ContextHistory<ShooterContext>
{
	public static final ShooterContextHistory INVALID = new ShooterContextHistory(ShooterContext.INVALID.EntityUUID()) {
		@Override
		@Nonnull
		public ShooterContext GetOrCreate(@Nonnull Function<ShooterContext, Boolean> validatorFunc,
										  @Nonnull NonNullSupplier<ShooterContext> creatorFunc,
										  @Nonnull NonNullSupplier<Long> timeFunc)
		{
			return ShooterContext.INVALID;
		}
	};

	public ShooterContextHistory(@Nonnull UUID id)
	{
		super(id);
	}

	@Nonnull
	public ShooterContext ContextualizeWith(@Nonnull UUID ownerUUID, @Nonnull UUID shooterUUID, @Nonnull Function<UUID, Entity> entityResolveFunc)
	{
		ShooterContext mostRecentContext = ContextualizeWith(ownerUUID, shooterUUID, (Level)null);
		if(mostRecentContext instanceof ShooterContextUnresolvedEntity unresolvedContext)
		{
			Entity shooter = entityResolveFunc.apply(unresolvedContext.EntityUUID());
			if(shooter != null)
			{
				Entity owner = entityResolveFunc.apply(unresolvedContext.OwnerUUID());
				if(owner != null)
					return ContextualizeWith(shooter); // TODO: Better owner handling
				else
					return ContextualizeWith(shooter);
			}
		}
		return mostRecentContext;
	}

	@Nonnull
	public ShooterContext ContextualizeWith(@Nonnull UUID ownerUUID, @Nonnull UUID shooterUUID, @Nullable Level checkLevel)
	{
		return GetOrCreate(
			(check) -> check.EntityUUID().equals(shooterUUID)
				&& check.OwnerUUID().equals(ownerUUID)
				&& (checkLevel == null || check.Level() == checkLevel),
			() -> new ShooterContextUnresolvedEntity(ownerUUID, shooterUUID, MinecraftHelpers.GetLogicalSide()),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public ShooterContext ContextualizeWith(@Nonnull Entity entityShooter)
	{
		if(entityShooter instanceof LivingEntity livingShooter)
		{
			if(entityShooter instanceof Player playerShooter)
				return ContextualizeWith(playerShooter);
			else
				return ContextualizeWith(livingShooter);
		}
		return ContextualizeWith(entityShooter.getUUID(), entityShooter.getUUID(), entityShooter.level());
	}

	@Nonnull
	public ShooterContext ContextualizeWith(@Nonnull LivingEntity livingShooter)
	{
		return GetOrCreate(
			(check) -> check.Entity() == livingShooter,
			() -> new ShooterContextLiving(livingShooter),
			MinecraftHelpers::GetTick);
	}

	@Nonnull
	public ShooterContext ContextualizeWith(@Nonnull Player playerShooter)
	{
		return GetOrCreate(
			(check) -> check.Entity() == playerShooter,
			() -> new ShooterContextPlayer(playerShooter),
			MinecraftHelpers::GetTick);
	}

	@Override
	protected boolean BasicValidation(@Nonnull ShooterContext check)
	{
		return check.IsValid();
	}

	@Override
	@Nonnull
	protected ShooterContext GetInvalidContext(){ return ShooterContext.INVALID; }

	@Override
	protected void MarkContextAsOld(@Nonnull ShooterContext oldContext)
	{
		oldContext.MarkAsOld();
	}
}
