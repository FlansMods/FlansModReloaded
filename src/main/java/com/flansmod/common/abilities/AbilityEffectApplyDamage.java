package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectApplyDamage implements IAbilityEffect
{
	private final float BaseDamageAmount;

	public AbilityEffectApplyDamage(@Nonnull AbilityEffectDefinition effectDef)
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
				triggerOn.hurt(level.damageSources().magic(), DamageAmount(gun, stacks));
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
