package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.FlanItem;
import com.flansmod.util.Transform;
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

	public ShooterContextLiving(@Nonnull LivingEntity living)
	{
		Shooter = living;
	}

	@Override
	public int GetNumValidContexts() { return (GetMainHandContext().IsValid() ? 1 : 0) + (GetOffHandContext().IsValid() ? 1 : 0); }
	@Override
	public GunContext[] GetAllActiveGunContexts()
	{
		return new GunContext[] { GetMainHandContext(), GetOffHandContext() };
	}
	@Override
	public GunContext CreateForGunIndex(int gunSlotIndex) { return new GunContextLiving(this, gunSlotIndex == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND); }
	@Override
	public GunContext CreateForSpecificStack(int gunSlotIndex, ItemStack stack) { return new GunContextLiving(this, gunSlotIndex == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, stack); }
	public GunContext GetContext(InteractionHand hand) { return GunContext.GetOrCreate(this, hand); }
	public GunContext GetMainHandContext() { return GunContext.GetOrCreate(this, InteractionHand.MAIN_HAND); }
	public GunContext GetOffHandContext() { return GunContext.GetOrCreate(this, InteractionHand.OFF_HAND); }

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
	public ActionGroupContext[] GetPrioritisedActions(EActionInput action)
	{
		GunContext mainHand = GetMainHandContext();
		GunContext offHand = GetOffHandContext();
		int numValid = (mainHand.IsValid() ? 1 : 0) + (offHand.IsValid() ? 1 : 0);

		switch(numValid)
		{
			case 0: return new ActionGroupContext[0];
			case 1:
			{
				return new ActionGroupContext[]{
					mainHand.IsValid()
						? ActionGroupContext.CreateFrom(mainHand, action)
						: ActionGroupContext.CreateFrom(offHand, action)
				};
			}
			case 2:
			{
				switch (action)
				{
					case SECONDARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(offHand, EActionInput.PRIMARY),
							ActionGroupContext.CreateFrom(mainHand, EActionInput.PRIMARY)
						};
					}
					case PRIMARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(mainHand, EActionInput.PRIMARY),
							ActionGroupContext.CreateFrom(offHand, EActionInput.PRIMARY)
						};
					}
					case RELOAD_PRIMARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(mainHand, EActionInput.RELOAD_PRIMARY),
							ActionGroupContext.CreateFrom(offHand, EActionInput.RELOAD_PRIMARY)
						};
					}
					case RELOAD_SECONDARY -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(offHand, EActionInput.RELOAD_PRIMARY),
							ActionGroupContext.CreateFrom(mainHand, EActionInput.RELOAD_PRIMARY)
						};
					}
					default -> {
						return new ActionGroupContext[]{
							ActionGroupContext.CreateFrom(mainHand, action),
							ActionGroupContext.CreateFrom(offHand, action)
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
