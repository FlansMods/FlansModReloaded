package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.FlanItem;
import com.flansmod.util.Transform;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
	public boolean IsCreative() { return false; }

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
	public ActionGroupContext[] GetPrioritisedActions(EActionInput action)
	{
		switch(GetNumValidContexts())
		{
			case 0: return new ActionGroupContext[0];
			case 1:
			{
				return new ActionGroupContext[]{
					MainHand.IsValid()
						? ActionGroupContext.CreateFrom(MainHand, action)
						: ActionGroupContext.CreateFrom(OffHand, action)
				};
			}
			case 2:
			{
				switch (action)
				{
					case SECONDARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(OffHand, EActionInput.PRIMARY),
							ActionGroupContext.CreateFrom(MainHand, EActionInput.PRIMARY)
						};
					}
					case PRIMARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(MainHand, EActionInput.PRIMARY),
							ActionGroupContext.CreateFrom(OffHand, EActionInput.PRIMARY)
						};
					}
					case RELOAD_PRIMARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(MainHand, EActionInput.RELOAD_PRIMARY),
							ActionGroupContext.CreateFrom(OffHand, EActionInput.RELOAD_PRIMARY)
						};
					}
					case RELOAD_SECONDARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(OffHand, EActionInput.RELOAD_PRIMARY),
							ActionGroupContext.CreateFrom(MainHand, EActionInput.RELOAD_PRIMARY)
						};
					}
					default -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(MainHand, action),
							ActionGroupContext.CreateFrom(OffHand, action)
						};
					}
				}
			}
			default: return new ActionGroupContext[0];
		}
	}
	@Override
	public Container GetAttachedInventory() { return null; }
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

	//
	@Override
	public int HashModifierSources()
	{
		int hash = 0;

		hash ^= HashSlot(EquipmentSlot.HEAD);
		hash ^= HashSlot(EquipmentSlot.CHEST);
		hash ^= HashSlot(EquipmentSlot.LEGS);
		hash ^= HashSlot(EquipmentSlot.FEET);

		return hash;
	}
	private int HashSlot(EquipmentSlot slot)
	{
		if(Shooter.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FlanItem flanItem)
		{
			int defHash = flanItem.DefinitionLocation.hashCode();
			return ((defHash << 16) | (defHash >> 16));
		}
		return 0;
	}
	@Override
	public void RecalculateModifierCache()
	{
		CacheSlot(EquipmentSlot.HEAD);
		CacheSlot(EquipmentSlot.CHEST);
		CacheSlot(EquipmentSlot.LEGS);
		CacheSlot(EquipmentSlot.FEET);
	}
	private void CacheSlot(EquipmentSlot slot)
	{
		if(Shooter.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FlanItem flanItem)
		{
			// TODO: Cache armour stats
		}
	}

}
