package com.flansmod.common.actions;

import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class Actions
{
	public static ActionInstance InstanceAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
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

	public static ActionInstance.NetData CreateEmptyNetData(int type)
	{
		switch(type)
		{
			case ActionInstance.NetData.INVALID_ID -> { return ActionInstance.NetData.Invalid; }
			case ShootAction.ShootNetData.ID -> { return new ShootAction.ShootNetData(); }
		}
		return ActionInstance.NetData.Invalid;
	}
}
