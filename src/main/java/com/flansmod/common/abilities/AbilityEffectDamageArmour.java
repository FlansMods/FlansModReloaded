package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectDamageArmour implements IAbilityEffect
{
	public final float BaseDamageAmount;
	public AbilityEffectDamageArmour(@Nonnull AbilityEffectDefinition effectDef)
	{
		BaseDamageAmount = effectDef.ModifyFloat(ModifierDefinition.STAT_INSTANT_DAMAGE, 1.0f);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		Level level = gun.GetLevel();
		if(level != null)
		{
			targets.ForEachEntity((triggerOn) -> {
				if(triggerOn instanceof LivingEntity living)
				{
					// TODO
					//living.getArmorSlots();
					//triggerOn.armor(level.damageSources().magic(), DamageAmount(gun, stacks));
				}
			});
		}
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	private float DamageAmount(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		float amount = gun.ModifyFloat(ModifierDefinition.STAT_INSTANT_DAMAGE, BaseDamageAmount);
		if(stacks != null)
		{
			amount *= stacks.GetIntensity();
		}
		return amount;
	}
}
