package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.stats.StatAccumulator;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.formulae.FloatAccumulation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectApplyDamage implements IAbilityEffect
{
	private final StatHolder InstantDamage;

	public AbilityEffectApplyDamage(@Nonnull AbilityEffectDefinition def)
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
				triggerOn.hurt(level.damageSources().magic(), InstantDamage.Get(gun, stacks));
			});
		}
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
