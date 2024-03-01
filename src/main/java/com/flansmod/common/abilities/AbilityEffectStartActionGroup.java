package com.flansmod.common.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.ActionGroupContext;
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
	public void TriggerClient(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(!actionGroup.GroupPath.equals(ActionGroupPath))
		{
			ActionGroupContext otherActionGroup = actionGroup.Gun.GetActionGroupContext(ActionGroupPath);
			if (otherActionGroup.IsValid())
			{
				actionGroup.Gun.GetActionStack().Client_TryStartGroupInstance(otherActionGroup);
			}
		}
		else FlansMod.LOGGER.error("ActionGroup " + ActionGroupPath + " trying to retrigger itself");
	}
	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(!actionGroup.GroupPath.equals(ActionGroupPath))
		{
			ActionGroupContext otherActionGroup = actionGroup.Gun.GetActionGroupContext(ActionGroupPath);
			if (otherActionGroup.IsValid())
			{
				actionGroup.Gun.GetActionStack().Server_TryStartGroupInstance(otherActionGroup);
			}

			// TODO: Let this function on ANY target, so you can do taze / forced inputs
			//targets.ForEachShooter((shooterContext) -> { });
		}
		else FlansMod.LOGGER.error("ActionGroup " + ActionGroupPath + " trying to retrigger itself");
	}
}
