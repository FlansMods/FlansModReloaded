package com.flansmod.common.actions.contexts;

import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.effects.FlansMobEffect;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ShooterContextLiving extends ShooterContext implements Container
{
	@Nonnull
	protected final LivingEntity Shooter;

	public ShooterContextLiving(@Nonnull LivingEntity living)
	{
		Shooter = living;
	}

	@Override
	public int GetNumValidContexts() { return (GetMainHandGunID() != FlanItem.InvalidGunUUID ? 1 : 0) + (GetOffHandGunID() != FlanItem.InvalidGunUUID ? 1 : 0); }

	@Nonnull
	public GunContext GetGunContextForSlot(InteractionHand hand, boolean client)
	{
		UUID gunID = GetGunID(hand);
		if(gunID != FlanItem.InvalidGunUUID)
			return GunContext.of(this, gunID);
		return GunContext.INVALID;
	}
	@Override
	@Nonnull
	public UUID[] GetAllGunIDs()
	{
		return new UUID[] { GetMainHandGunID(), GetOffHandGunID() };
	}
	@Override
	@Nonnull
	public UUID GetGunIDForSlot(int gunSlotIndex)
	{
		return GetGunID(gunSlotIndex == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
	}
	@Nonnull
	public UUID GetMainHandGunID() { return GetGunID(InteractionHand.MAIN_HAND); }
	@Nonnull
	public UUID GetOffHandGunID() { return GetGunID(InteractionHand.OFF_HAND); }
	@Nonnull
	public UUID GetGunID(@Nonnull InteractionHand hand)
	{
		ItemStack handStack = Shooter.getItemInHand(hand);
		return FlanItem.GetGunID(handStack);
	}
	@Override
	@Nonnull
	public GunContext CreateContext(@Nonnull UUID gunID)
	{
		if(gunID.equals(GetMainHandGunID()))
			return new GunContextLiving(this, InteractionHand.MAIN_HAND);
		else if(gunID.equals(GetOffHandGunID()))
			return new GunContextLiving(this, InteractionHand.OFF_HAND);
		return GunContext.INVALID;
	}

	public boolean CanPerformTwoHandedAction()
	{
		// Weird idea, but two handed actions require exactly one hand to be full
		return Shooter.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() !=
				Shooter.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
	}

	@Override
	@Nonnull
	public Transform GetShootOrigin(float deltaTick)
	{
		return Transform.FromPosAndEuler(
			Shooter.getEyePosition(deltaTick),
			Shooter.getViewXRot(deltaTick),
			180f + Shooter.getViewYRot(deltaTick),
			0.0f,
			() -> "\"ShootOrigin\"");
	}

	@Override
	public boolean IsValid() { return !Shooter.isRemoved(); }
	@Override
	public boolean IsCreative() { return false; }
	@Override
	public Container GetAttachedInventory() { return this; }
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
	//
	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker)
	{
		CacheSlot(EquipmentSlot.HEAD);
		CacheSlot(EquipmentSlot.CHEST);
		CacheSlot(EquipmentSlot.LEGS);
		CacheSlot(EquipmentSlot.FEET);
		BakeMobEffects(baker);
	}
	private void CacheSlot(EquipmentSlot slot)
	{
		if(Shooter.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FlanItem flanItem)
		{
			// TODO: Cache armour stats
		}
	}
	private void BakeMobEffects(@Nonnull IModifierBaker baker)
	{
		for(MobEffectInstance effect : Shooter.getActiveEffects())
			if(effect.getEffect() instanceof FlansMobEffect flansEffect)
				for(AbilityDefinition abilityDef : flansEffect.Def().abilities)
					for(AbilityEffectDefinition effectDef : abilityDef.effects)
						for(ModifierDefinition modifierDef : effectDef.modifiers)
						{
							int traitLevel = 1;
							int stackCount = 0;
							if(abilityDef.IsStackable())
							{
								for(GunContext gunContext : GetAllGunContexts(GetSide() == EContextSide.Client))
								{
									AbilityStack stacks = gunContext.GetActionStack().GetStacks(abilityDef.stacking);
									if(stacks != null)
									{
										traitLevel = stacks.Level;
										stackCount = stacks.GetStackCount();
									}
								}
							}

							baker.Bake(modifierDef, traitLevel, stackCount);
						}
	}

	@Override
	public int getContainerSize() { return LivingEntity.ARMOR_SLOTS + LivingEntity.HAND_SLOTS; }
	@Override
	public boolean isEmpty() {
		for(EquipmentSlot slot : EquipmentSlot.values())
			if(!Shooter.getItemBySlot(slot).isEmpty())
				return false;
		return true;
	}
	private EquipmentSlot GetSlot(int slotIndex) {
		if(slotIndex >= LivingEntity.HAND_SLOTS)
			return EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex - LivingEntity.HAND_SLOTS);
		return EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.HAND, slotIndex);
	}
	@Override
	@Nonnull
	public ItemStack getItem(int slotIndex)
	{
		return Shooter.getItemBySlot(GetSlot(slotIndex));
	}
	@Override
	@Nonnull
	public ItemStack removeItem(int slotIndex, int count)
	{
		ItemStack stack = Shooter.getItemBySlot(GetSlot(slotIndex));
		stack.setCount(stack.getCount() - count);
		return stack.copyWithCount(count);
	}
	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int slotIndex)
	{
		ItemStack stack = Shooter.getItemBySlot(GetSlot(slotIndex));
		Shooter.setItemSlot(GetSlot(slotIndex), ItemStack.EMPTY);
		return stack;
	}
	@Override
	public void setItem(int slotIndex, @Nonnull ItemStack stack)
	{
		Shooter.setItemSlot(GetSlot(slotIndex), stack);
	}
	@Override
	public void setChanged() {}
	@Override
	public boolean stillValid(@Nonnull Player player) { return true; }
	@Override
	public void clearContent() {}


	@Override
	public String toString()
	{
		return "Living:'"+Shooter.getName().getString()+"'['"+Dimension().location().getPath() + "']";
	}
}
