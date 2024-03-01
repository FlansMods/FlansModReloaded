package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectDamageArmour implements IAbilityEffect
{
	private final StatHolder InstantDamage;

	public AbilityEffectDamageArmour(@Nonnull AbilityEffectDefinition def)
	{
		InstantDamage = new StatHolder(Constants.STAT_INSTANT_DAMAGE, def);
	}

	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
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
}
