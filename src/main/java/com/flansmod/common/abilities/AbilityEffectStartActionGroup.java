package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectStartActionGroup implements IAbilityEffect
{
	public final String ActionGroupPath;

	public AbilityEffectStartActionGroup(@Nonnull AbilityEffectDefinition def)
	{
		ActionGroupPath = def.ModifyString(Constants.KEY_ACTION_KEY, "");
	}

	@Override
	public void TriggerClient(@Nonnull GunContext gun, TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		ActionGroupContext actionGroup = gun.GetActionGroupContext(ActionGroupPath);
		if (actionGroup.IsValid())
		{
			gun.GetActionStack().Client_TryStartGroupInstance(actionGroup);
		}
	}
	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		ActionGroupContext actionGroup = gun.GetActionGroupContext(ActionGroupPath);
		if (actionGroup.IsValid())
		{
			gun.GetActionStack().Server_TryStartGroupInstance(actionGroup);
		}

		// TODO: Let this function on ANY target, so you can do taze / forced inputs
		//targets.ForEachShooter((shooterContext) -> { });
	}
}
