package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.util.Transform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ShooterContextLiving extends ShooterContext
{
	@Nonnull
	protected final LivingEntity Shooter;
	private final GunContext MainHand;
	private final GunContext OffHand;

	public ShooterContextLiving(@Nonnull LivingEntity living)
	{
		Shooter = living;
		MainHand = GunContext.CreateFrom(this, InteractionHand.MAIN_HAND);
		OffHand = GunContext.CreateFrom(this, InteractionHand.OFF_HAND);
	}

	public GunContext GetContext(InteractionHand hand) { return hand == InteractionHand.MAIN_HAND ? MainHand : OffHand; }
	public GunContext GetMainHandContext() { return MainHand; }
	public GunContext GetOffHandContext() { return OffHand; }

	public boolean CanPerformTwoHandedAction()
	{
		// Weird idea, but two handed actions require exactly one hand to be full
		return Shooter.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() !=
				Shooter.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
	}

	@Override
	public Transform GetShootOrigin()
	{
		Transform transform = new Transform(Shooter.getEyePosition());
		transform = transform.RotateLocalEuler(Shooter.getXRot(), Shooter.getYRot(), 0.0f);
		return transform;
	}

	@Override
	public boolean IsValid() { return true; }

	@Override
	public int GetNumValidContexts()
	{
		return (MainHand.IsValid() ? 1 : 0) + (OffHand.IsValid() ? 1 : 0);
	}

	@Override
	public GunContext[] GetAllGunContexts()
	{
		return new GunContext[] { MainHand, OffHand };
	}

	@Override
	public ActionContext[] GetPrioritisedActions(EActionInput action)
	{
		switch(GetNumValidContexts())
		{
			case 0: return new ActionContext[0];
			case 1:
			{
				return new ActionContext[]{
					MainHand.IsValid()
						? ActionContext.CreateFrom(MainHand, action)
						: ActionContext.CreateFrom(OffHand, action)
				};
			}
			case 2:
			{
				switch (action)
				{
					case SECONDARY -> {
						return new ActionContext[]{
							ActionContext.CreateFrom(OffHand, EActionInput.PRIMARY),
							ActionContext.CreateFrom(MainHand, EActionInput.PRIMARY)
						};
					}
					case PRIMARY -> {
						return new ActionContext[]{
							ActionContext.CreateFrom(MainHand, EActionInput.PRIMARY),
							ActionContext.CreateFrom(OffHand, EActionInput.PRIMARY)
						};
					}
					default -> {
						return new ActionContext[]{
							ActionContext.CreateFrom(MainHand, action),
							ActionContext.CreateFrom(OffHand, action)
						};
					}
				}
			}
			default: return new ActionContext[0];
		}
	}

	@Override
	public Entity Entity()
	{
		return Shooter;
	}
	@Override
	public Entity Owner()
	{
		return Shooter;
	}
	@Override
	public int hashCode()
	{
		return Objects.hash(Shooter.getId(), Shooter.level.dimension());
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof ShooterContextLiving otherContext)
		{
			return otherContext.Shooter == Shooter;
		}
		return false;
	}
}
