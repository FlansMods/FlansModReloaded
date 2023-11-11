package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ActionGroupOverrideDefinition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Actions
{
	public static List<ActionDefinition> EvaluateActionOverrides(@Nonnull ActionGroupDefinition groupDef, EActionInput inputType, List<ActionGroupOverrideDefinition> overrides)
	{
		List<ActionDefinition> defs = new ArrayList<>();
		boolean addBaseActions = true;
		for(ActionGroupOverrideDefinition overrideDef : overrides)
		{
			if(overrideDef.override)
				addBaseActions = false;
			defs.addAll(Arrays.asList(overrideDef.actionGroup.actions));
		}
		if(addBaseActions)
		{
			defs.addAll(Arrays.asList(groupDef.actions));
		}
		return defs;
	}

	public static ActionGroup CreateActionGroup(@Nonnull ActionGroupDefinition groupDef, EActionInput inputType, List<ActionGroupOverrideDefinition> overrides)
	{
		ActionGroup group = new ActionGroup(groupDef, inputType);
		boolean addBaseActions = true;
		for(ActionGroupOverrideDefinition overrideDef : overrides)
		{
			if(overrideDef.override)
				addBaseActions = false;
			for(ActionDefinition actionDef : overrideDef.actionGroup.actions)
			{
				Action action = CreateAction(group, actionDef);
				if(action != null)
					group.AddAction(action);
			}
		}
		if(addBaseActions)
		{
			for (ActionDefinition actionDef : groupDef.actions)
			{
				Action action = CreateAction(group, actionDef);
				if (action != null)
					group.AddAction(action);
			}
		}
		return group;
	}

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
			case PlaceBlock 		-> { return new PlaceBlockAction(group, def); }
			case BreakBlock 		-> { return new BreakBlockAction(group, def); }
			case CookGrenade 		-> { return new CookGrenadeAction(group, def); }
			case Heal				-> { return new HealAction(group, def); }
			case SpawnEntity		-> { return new SpawnEntityAction(group, def); }
			case SummonNpc 			-> { return new SummonNpcAction(group, def); }
			case DeleteEntity 		-> { return new DeleteEntityAction(group, def); }
			case Drop 				-> { return new DropAction(group, def); }
			case Feed				-> { return new FeedAction(group, def); }
			case Axe, Pickaxe, Hoe, Shovel, Shear, Flatten, Strip, Till, Shield -> {
				// These are handled with vanilla logic
				return null;
			}
			default					-> { return null; }
		}
		// TODO: Add a registration event for other mods
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
