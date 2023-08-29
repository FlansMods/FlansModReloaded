package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;

import javax.annotation.Nonnull;

public class Actions
{
	public static ActionGroup CreateActionGroup(@Nonnull ActionGroupDefinition groupDef, EActionInput inputType)
	{
		ActionGroup group = new ActionGroup(groupDef, inputType);
		for(ActionDefinition actionDef : groupDef.actions)
		{
			Action action = CreateAction(group, actionDef);
			if(action != null)
				group.AddAction(action);
		}
		return group;
	}

	private static Action CreateAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		switch(def.actionType)
		{
			case Animation			-> { return new AnimationAction(group, def); }
			case Shoot 				-> { return new ShootAction(group, def); }
			case Scope				-> { return new ScopeAction(group, def); }
			case AimDownSights 		-> { return new AimDownSightAction(group, def); }
			case PlaySound  		-> { return new PlaySoundAction(group, def); }
			default					-> { return null; }
		}
	}

	public static Action.NetData CreateEmptyNetData(int type)
	{
		switch(type)
		{
			case Action.NetData.INVALID_ID -> { return Action.NetData.Invalid; }
			case ShootAction.ShootNetData.ID -> { return new ShootAction.ShootNetData(); }
		}
		return Action.NetData.Invalid;
	}
}
