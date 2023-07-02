package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.GunItem;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ShooterContext
{
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override
		public int GetNumValidContexts() { return 0; }
		@Override
		public GunContext[] GetAllGunContexts() { return new GunContext[0]; }
		@Override
		public ActionContext[] GetPrioritisedActions(EActionInput action) { return new ActionContext[0]; }
		@Override
		public Entity Entity() { return null; }
		@Override
		public Entity Owner() { return null; }
		@Override
		public Transform GetShootOrigin() { return null; }
		@Override
		public boolean IsValid() { return false; }
		@Override
		public int hashCode() { return 0; }
	};

	@Nonnull
	public static ShooterContext CreateFrom(Entity entity)
	{
		if(entity instanceof LivingEntity living)
			return new ShooterContextLiving(living);
		return INVALID;
	}
	@Nonnull
	public static ShooterContext CreateFrom(UseOnContext useOnContext)
	{
		if(useOnContext.getPlayer() != null && useOnContext.getItemInHand().getItem() instanceof GunItem gun)
		{
			return new ShooterContextLiving(useOnContext.getPlayer());
		}
		return INVALID;
	}

	public boolean IsLocalPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player && player.isLocalPlayer();
	}

	@Nullable
	public Level Level()
	{
		return Entity() != null ? Entity().level : null;
	}

	public abstract int GetNumValidContexts();
	public abstract GunContext[] GetAllGunContexts();
	public abstract ActionContext[] GetPrioritisedActions(EActionInput action);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Transform GetShootOrigin();
	public abstract boolean IsValid();


}
