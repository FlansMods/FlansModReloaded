package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectHealEntity implements IAbilityEffect
{
	private final StatHolder HealAmount;

	public AbilityEffectHealEntity(@Nonnull AbilityEffectDefinition def)
	{
		HealAmount = new StatHolder(Constants.STAT_HEAL_AMOUNT, def);
	}

	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity((entity) ->
		{
			if(entity instanceof LivingEntity living)
			{
				living.heal(HealAmount.Get(gun, stacks));
			}
		});
	}
}
