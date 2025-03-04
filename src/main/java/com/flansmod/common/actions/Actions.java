package com.flansmod.common.actions;

import com.flansmod.common.actions.nodes.*;
import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class Actions
{
	public static final String DefaultPrimaryActionKey = "primary_fire";

	public static ActionInstance InstanceAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		switch(def.actionType)
		{
			case Animation			-> { return new AnimationAction(group, def); }
			case Shoot 				-> { return new ShootAction(group, def); }
			case Scope				-> { return new ScopeAction(group, def); }
			case AimDownSights 		-> { return new AimDownSightAction(group, def); }
			case PlaySound  		-> { return new PlaySoundAction(group, def); }
			case CookGrenade 		-> { return new CookGrenadeAction(group, def); }
			case Raycast 			-> { return new RaycastAction(group, def); }
			case Drop 				-> { return new DropAction(group, def); }
			case Axe, Pickaxe, Hoe, Shovel, Shear, Flatten, Strip, Till, Shield -> {
				// These are handled with vanilla logic
				return null;
			}
			case AttachEffect 		-> { return new AttachEffectAction(group, def); }
			case SpawnParticle 		-> { return new SpawnParticleAction(group, def); }
			case EjectCasing 				-> { return new EjectCasingAction(group, def); }
			case Laser 				-> { return new LaserAction(group, def); }
			case SwitchMode			-> { return new SwitchModeAction(group, def); }
			case AbilityOnSelf 		-> { return new AbilityOnSelfAction(group, def); }
			default					-> { return null; }


			// MOVED TO ABILITIES
			// case BreakBlock 		-> { return new BreakBlockAbility(group, def); }
			// case SpawnEntity		-> { return new SpawnEntityAction(group, def); }
			// case Heal				-> { return new AbilityEffectHeal(group, def); }
			// case SummonNpc 			-> { return new SummonNpcAction(group, def); }
			// case Feed				-> { return new FeedAction(group, def); }
			// case PlaceBlock 		-> { return new AbilityEffectPlaceBlock(group, def); }
		}
		// TODO: Add a registration event for other mods
	}

	public static ActionInstance.NetData CreateEmptyNetData(int type)
	{
		switch(type)
		{
			case ActionInstance.NetData.INVALID_ID -> { return ActionInstance.NetData.Invalid; }
			case ShootAction.ShootNetData.ID -> { return new ShootAction.ShootNetData(); }
			case EjectCasingAction.CasingNetData.ID -> { return new EjectCasingAction.CasingNetData(); }
		}
		return ActionInstance.NetData.Invalid;
	}
}
