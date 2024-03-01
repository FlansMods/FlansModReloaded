package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectApplyDamage implements IAbilityEffect
{
	private final StatHolder InstantDamage;
	private final boolean PreventDamageCooldown;

	public AbilityEffectApplyDamage(@Nonnull AbilityEffectDefinition def)
	{
		InstantDamage = new StatHolder(Constants.STAT_INSTANT_DAMAGE, def);
		PreventDamageCooldown = def.ModifyBoolean(Constants.STAT_PREVENT_DAMAGE_COOLDOWN, true);
	}

	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		DamageSource dmgSource = gun.CreateDamageSource();
		targets.ForEachEntity((triggerOn) -> {

			// TODO: Headshot multipliers


			triggerOn.hurt(dmgSource, DamageAmount(gun, stacks));
			if(PreventDamageCooldown && triggerOn instanceof LivingEntity living)
			{
				living.hurtTime = 0;
				living.hurtDuration = 0;
				living.invulnerableTime = 0;
			}
		});
	}

	public float DamageAmount(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		return InstantDamage.Get(gun, stacks);
	}
}
