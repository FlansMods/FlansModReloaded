package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectStartActionGroup implements IAbilityEffect
{
	public final String ActionGroupPath;

	public AbilityEffectStartActionGroup(@Nonnull AbilityEffectDefinition def)
	{
		ActionGroupPath = def.ModifyString(ModifierDefinition.KEY_ACTION_KEY, "");
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		ActionGroupContext actionGroup = gun.GetActionGroupContext(ActionGroupPath);
		if (actionGroup.IsValid())
		{
			if (gun.GetActionStack().IsClient)
			{
				// TODO: Create a "Shooter"Context with custom position
				gun.GetActionStack().Client_TryStartGroupInstance(actionGroup);
			} else
			{
				gun.GetActionStack().Server_TryStartGroupInstance(actionGroup);
			}
		}

		// TODO: Let this function on ANY target, so you can do taze / forced inputs
		//targets.ForEachShooter((shooterContext) -> { });
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
