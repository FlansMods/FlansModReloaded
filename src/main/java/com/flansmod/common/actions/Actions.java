package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import net.minecraft.world.InteractionHand;

public class Actions
{
	public static Action CreateAction(ActionStack stack, ActionDefinition def, InteractionHand hand)
	{
		switch(def.actionType)
		{
			case Animation			-> { return new AnimationAction(stack, def, hand); }
			case Shoot 				-> { return new ShootAction(stack, def, hand); }
			case Scope				-> { return new ScopeAction(stack, def, hand); }
			case AimDownSights 		-> { return new AimDownSightAction(stack, def, hand); }
			default					-> { return null; }
		}
	}
}
