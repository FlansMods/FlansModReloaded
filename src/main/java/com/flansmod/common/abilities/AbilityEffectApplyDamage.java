package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AbilityEffectApplyDamage implements IAbilityEffect
{
	private final ModifierDefinition[] BaseDamageMods;

	public AbilityEffectApplyDamage(@Nonnull AbilityEffectDefinition effectDef)
	{
		BaseDamageMods = effectDef.MatchModifiers(ModifierDefinition.STAT_INSTANT_DAMAGE);
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
		FloatModifier baseDamage = FloatModifier.of(StatCalculationContext.of(gun, stacks), BaseDamageMods);
		FloatModifier gunModifier = gun.GetFloatModifier(ModifierDefinition.STAT_INSTANT_DAMAGE);
		return FloatModifier.of(baseDamage, gunModifier).GetValue();
	}
}
