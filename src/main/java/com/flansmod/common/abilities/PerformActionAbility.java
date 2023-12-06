package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PerformActionAbility extends InstantAbility
{
	public PerformActionAbility(AbilityDefinition def, int level)
	{
		super(def, level);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		super.Trigger(gun, hit);

		for(String param : Def.effectParameters)
		{
			ActionGroupContext actionGroup = gun.GetActionGroupContext(param);
			if (actionGroup.IsValid())
			{
				if (gun.GetActionStack().IsClient)
				{
					gun.GetActionStack().Client_TryStartGroupInstance(actionGroup);
				}
				else
				{
					gun.GetActionStack().Server_TryStartGroupInstance(actionGroup);
				}
			}
		}
	}
}
