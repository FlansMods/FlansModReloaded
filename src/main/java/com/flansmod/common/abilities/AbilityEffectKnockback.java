package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectKnockback implements IAbilityEffect
{
	private final StatHolder PushAmount;
	// TODO: boolean AlwaysKickUpwards

	public AbilityEffectKnockback(@Nonnull AbilityEffectDefinition def)
	{
		PushAmount = new StatHolder(Constants.STAT_IMPACT_KNOCKBACK, def);
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity((entity) -> {
			Vec3 pushDir = entity.position().subtract(actionGroup.Gun.GetShootOrigin().positionVec3());
			pushDir = pushDir.normalize();
			pushDir.scale(PushAmount.Get(actionGroup, stacks));

			//if(AlwaysKickUpwards && impactDirection.y < 0.0f)
			//	impactDirection = new Vec3(impactDirection.x, -impactDirection.y, impactDirection.z);

			entity.addDeltaMovement(pushDir);
		});
	}
}
