package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectDamageArmour implements IAbilityEffect
{
	private final StatHolder InstantDamage;

	public AbilityEffectDamageArmour(@Nonnull AbilityEffectDefinition def)
	{
		InstantDamage = new StatHolder(ModifierDefinition.STAT_INSTANT_DAMAGE, def);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
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
}
