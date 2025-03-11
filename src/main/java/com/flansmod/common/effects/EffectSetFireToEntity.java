package com.flansmod.common.effects;

import com.flansmod.common.FlansModConfig;
import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.physics.common.util.Maths;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectSetFireToEntity implements IEffect
{
	private final StatHolder FireTime;

	public EffectSetFireToEntity(@Nonnull AbilityEffectDefinition def)
	{
		FireTime = new StatHolder(Constants.STAT_IMPACT_SET_FIRE_TO_TARGET, def);
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		// Server config hook
		float globalFireMulti = FlansModConfig.GlobalFireDurationMultiplier.get().floatValue();
		// ------------------

		targets.ForEachEntity((triggerOn) -> {
			triggerOn.setSecondsOnFire(Maths.ceil(globalFireMulti * FireTime.Get(actionGroup, stacks)));
		});
	}
}
