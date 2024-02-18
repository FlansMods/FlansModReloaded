package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectHealEntity implements IAbilityEffect
{
	private final StatHolder HealAmount;

	public AbilityEffectHealEntity(@Nonnull AbilityEffectDefinition def)
	{
		HealAmount = new StatHolder(ModifierDefinition.STAT_HEAL_AMOUNT, def);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity((entity) ->
		{
			if(entity instanceof LivingEntity living)
			{
				living.heal(HealAmount.Get(gun, stacks));
			}
		});
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
